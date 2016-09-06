package de.gymnasium_beetzendorf.vertretungsplan;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.activity.MainActivity;
import de.gymnasium_beetzendorf.vertretungsplan.data.Class;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data.Schoolday;

public class RefreshService extends IntentService {

    public RefreshService() {
        super("RefreshService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(Constants.TAG, "RefreshService started");

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Calendar calendar = Calendar.getInstance();
        String date = Constants.dateFormatter.format(calendar.getTime());
        long lastRefresh = 0;
        try {
            lastRefresh = Constants.dateTimeFormatter.parse(date + " 16:00").getTime();

        } catch (ParseException e) {
            Log.i(Constants.TAG, "Problem while parsing in service class", e);
        }

        boolean manualRefresh = intent.getBooleanExtra("manual_refresh", false);

        // only run if it's been send from the MainActivity or if it's earlier than the lastRefresh (4pm of today)
        if (manualRefresh || System.currentTimeMillis() <= lastRefresh) {
            // download file from server
            downloadFile(MainActivity.SERVER_URL + MainActivity.SUBSTITUTION_QUERY_FILE, "substitution", "");

        } else {
            // cancel the refreshing animation
            notifyMainActivityReturnResult(false);

            Intent alarmIntent = new Intent(this, RefreshService.class);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent alarmPendingIntent = PendingIntent.getService(this, BootReceiver.alarmManagerRequestCode, alarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(alarmPendingIntent);

            // adds 14 hours (in milliseconds to the last refresh) which represents 6 am the next morning
            long nextDayRefresh = lastRefresh + (1000 * 60 * 60 * 14);
            // set the new alarm which will start firing alarms every fifteen minutes until 8 pm
            alarmManager.setInexactRepeating(
                    Constants.ALARM_TYPE,
                    nextDayRefresh,
                    Constants.ALARM_INTERVAL,
                    alarmPendingIntent);

            Log.i(Constants.TAG, "Alarm set");

            mSharedPreferences.edit().putBoolean(Constants.ALARM_REGISTERED, true).apply();
        }


        if (intent.getBooleanExtra("refresh_class_list", false)) {

            updateClassList();
        }

    }

    private boolean notifyMainActivityReturnResult(boolean newUpdate) {
        Intent messageIntent = new Intent("refresh_message");

        messageIntent.putExtra("new_update", newUpdate);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        return localBroadcastManager.sendBroadcast(messageIntent);
    }

    private boolean newUpdate(List<Schoolday> xmlResults, List<Schoolday> databaseResults) {

        long databaseLastUpdated, xmlLastUpdated;

        try {
            xmlLastUpdated = xmlResults.get(0).getLastUpdated();
            Log.i(Constants.TAG, "xml: " + Constants.dateTimeFormatter.format(new Date(xmlLastUpdated)));
        } catch (IndexOutOfBoundsException e) {
            Log.i(Constants.TAG, "IndexOutOfBoundsException: ", e);
            return false;
        }

        try {
            databaseLastUpdated = databaseResults.get(0).getLastUpdated();
            Log.i(Constants.TAG, "db: " + Constants.dateTimeFormatter.format(new Date(databaseLastUpdated)));
        } catch (IndexOutOfBoundsException e) {
            Log.i(Constants.TAG, "IndexOutOfBoundsException: ", e);
            return true;
        }

        return xmlLastUpdated > databaseLastUpdated;

    }

    private void downloadFile(String url, String fileType, String schedClass) {
        new DownloadXml(this, fileType, url, schedClass).execute();
    }

    public void callBackSubstitution() {

        List<Schoolday> xmlResults = XMLParser.parseSubstitutionXml(this);

        DatabaseHandler databaseHandler = new DatabaseHandler(this, DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
        List<Schoolday> databaseResults = databaseHandler.getAllSubstitutions();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean notifications_enabled = sharedPreferences.getBoolean("enable_notifications", false);

        if (newUpdate(xmlResults, databaseResults)) {
            databaseHandler.insertSubstitutionXmlResults(xmlResults);

            Log.i(Constants.TAG, "xml: " + xmlResults.size() + " database: " + databaseResults.size());

            //Log.i(Constants.TAG, String.valueOf(xmlResults.get(0).getLastUpdated()));
            sharedPreferences.edit().putLong("last_substitution_plan_refresh", databaseResults.get(0).getLastUpdated()).apply();

            // check if user is currently in the app
            // if not - fire notification

            boolean isUserInApp = notifyMainActivityReturnResult(true);

            if (!isUserInApp && notifications_enabled) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(xmlResults.get(0).getLastUpdated()); // use xml results because database hasn't been updated

                String updated = Constants.dateTimeFormatter.format(calendar.getTime());

                // intent that opens the main activity when notification is clicked
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), notificationIntent, 0);

                // build the notification and fire it to the user
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new Notification.Builder(this)
                        .setContentTitle("Neue Vertretung!")
                        .setContentText("Aktualisiert: " + updated)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent)
                        .build();

                notificationManager.notify(0, notification);
            }

        } else {
            notifyMainActivityReturnResult(false);
        }


    }

    public void callBackSchedule(String schedClass) {
    }


    public void updateClassList() {
        String url = "http://gymnasium-beetzendorf.de/stundenkl/default.html";

        String username = "beetzendorf";
        String password = "tafel";

        String authorizationString = username + ":" + password;

        String encodedString = Base64.encodeToString(authorizationString.getBytes(), Base64.DEFAULT);

        try {
            Document document = Jsoup.connect(url).header("Authorization", "Basic " + encodedString).post();

            Element div = document.getElementById("content");
            Elements li = div.select("li");

            List<Class> classes = new ArrayList<>();
            Class currentClass = new Class();

            for (int i = 0; i < li.size(); i++) {
                Elements a = li.get(i).select("a");
                if (a.get(0) != null) {
                    currentClass = new Class();
                    if (a.get(0).text().length() < 4) {
                        currentClass.setName("0" + a.get(0).text());
                    } else {
                        currentClass.setName(a.get(0).text());
                    }
                    currentClass.setUrl(a.get(0).attr("href"));
                }
                classes.add(currentClass);
            }

            DatabaseHandler handler = new DatabaseHandler(getApplicationContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
            handler.updateClassList(classes);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            sharedPreferences.edit().putLong("last_class_list_refresh", System.currentTimeMillis()).apply();

            Toast.makeText(getApplicationContext(), "Klassen geupdated", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(Constants.TAG, "IOException when getting html", e);
        }

    }
}