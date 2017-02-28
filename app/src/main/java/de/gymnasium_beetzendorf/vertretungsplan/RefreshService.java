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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.activity.MainActivity;
import de.gymnasium_beetzendorf.vertretungsplan.data.Class;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data.School;
import de.gymnasium_beetzendorf.vertretungsplan.data.SubstitutionDay;

public class RefreshService extends IntentService implements Constants {

    private static final String TAG = RefreshService.class.getSimpleName();

    public static final String INSTRUCTION = "instruction";
    public static final int NO_REFRESH = -1;
    public static final int SUBSTITUTION_REFRESH = 0;
    public static final int SCHEDULE_REFRESH = 1;
    public static final int CLASSLIST_REFRESH = 2;
    public static final int SET_ALARM = 3;

    private SharedPreferences mSharedPreferences;
    private int mSchool;
    private int mClassYear;
    private String mCLassLetter;

    public RefreshService() {
        super("RefreshService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "RefreshService started");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSchool = mSharedPreferences.getInt(PREFERENCE_SCHOOL, 0);
        String classYearLetter = mSharedPreferences.getString(PREFERENCE_CLASS_YEAR_LETTER, "");
        if (!classYearLetter.equalsIgnoreCase("")) {
            mClassYear = Integer.parseInt(classYearLetter.substring(0, 2));
            mCLassLetter = classYearLetter.substring(3);
        }

        int instructions = intent.getIntExtra(INSTRUCTION, -1);

        String substitutionUrl;
        try {
            substitutionUrl = School.findSchoolById(mSchool).getSubstitutionUrl();
        } catch (IllegalAccessException e) {
            // Just set this as a default to avoid weird things to happen. might change this in the future
            substitutionUrl = School.Gymnasium_Beetzendorf.getSubstitutionUrl();
            mSchool = School.Gymnasium_Beetzendorf.getId();
        }

        long lastRefreshOfDay;
        try {
            lastRefreshOfDay = dateTimeFormatter.parse(dateFormatter.format(Calendar.getInstance().getTime()) + " 16:00").getTime();
        } catch (ParseException e) {
            Log.i(TAG, "Date could not be parsed in RefreshService (lastRefreshOfDay): ", e);
            lastRefreshOfDay = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() >= lastRefreshOfDay) {
            setNextDayAlarm(lastRefreshOfDay);
        }


        switch (instructions) {
            case NO_REFRESH:
                break;
            case SUBSTITUTION_REFRESH:
                downloadFile(substitutionUrl, XmlParser.SUBSTITUTION, "", mSchool);
                break;
            case CLASSLIST_REFRESH:
                updateClassList();
                break;
            case SET_ALARM:
                setNextDayAlarm(lastRefreshOfDay);
                break;
            default:
                break;
        }
    }

    private boolean notifyMainActivityReturnResult(boolean newUpdate) {
        Intent messageIntent = new Intent("refresh_message");
        messageIntent.putExtra("new_update", newUpdate);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        return localBroadcastManager.sendBroadcast(messageIntent);
    }

    private boolean newUpdate(List<SubstitutionDay> xmlResults, List<SubstitutionDay> databaseResults) {
        long databaseLastUpdated, xmlLastUpdated;
        try {
            xmlLastUpdated = xmlResults.get(0).getUpdated();
        } catch (IndexOutOfBoundsException e) {
            Log.i(TAG, "IndexOutOfBoundsException: ", e);
            return false;
        }

        try {
            databaseLastUpdated = databaseResults.get(0).getUpdated();
        } catch (IndexOutOfBoundsException e) {
            Log.i(TAG, "IndexOutOfBoundsException: ", e);
            return true;
        }
        return xmlLastUpdated > databaseLastUpdated;
    }

    private void downloadFile(String url, String fileType, String schedClass, int school) {
        new DownloadXml(this, fileType, url, schedClass, school).execute();
    }

    private void setNextDayAlarm(long lastRefreshOfDay) {
        Intent alarmIntent = new Intent(this, RefreshService.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent alarmPendingIntent = PendingIntent.getService(this, BootReceiver.alarmManagerRequestCode, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmPendingIntent);

        // adds 14 hours (in milliseconds to the last refresh) which represents 6 am the next morning
        long nextDayRefresh = lastRefreshOfDay + (1000 * 60 * 60 * 14);
        // set the new alarm which will start firing alarms every fifteen minutes until 8 pm
        alarmManager.setInexactRepeating(
                ALARM_TYPE,
                nextDayRefresh,
                ALARM_INTERVAL,
                alarmPendingIntent);

        Log.i(TAG, "Alarm set for next day at 6 am");
        mSharedPreferences.edit().putBoolean(PREFERENCE_ALARM_REGISTERED, true).apply();
    }

    public void callBackSubstitution() {
        XmlParser parser = new XmlParser(this, XmlParser.SUBSTITUTION);
        List<SubstitutionDay> xmlResults = parser.parseReturnSubstitution();

        DatabaseHandler databaseHandler = new DatabaseHandler(this, DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
        List<SubstitutionDay> databaseResults = databaseHandler.getSubstitutionDayList(mSchool, mClassYear, mCLassLetter);

        boolean notifications_enabled = mSharedPreferences.getBoolean("enable_notifications", false);

        if (newUpdate(xmlResults, databaseResults)) {
            databaseHandler.insertSubstitutionResults(mSchool, xmlResults);
            mSharedPreferences.edit().putLong("last_substitution_plan_refresh", xmlResults.get(0).getUpdated()).apply();

            // fire notification if user is not in app
            if (!notifyMainActivityReturnResult(true) && notifications_enabled) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(xmlResults.get(0).getUpdated()); // use xml results because database hasn't been updated
                String updated = dateTimeFormatter.format(calendar.getTime());

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

    public void updateClassList() {
        Log.i(TAG, "start refreshing classlist");
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

            Intent messageIntent = new Intent("classlist_updated");
            messageIntent.putExtra("number_of_classes", classes.size());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);

            Log.i(TAG, "done refreshing classlist");
        } catch (IOException e) {
            Log.e(TAG, "IOException when getting html", e);
        }
    }

    public void callBackSchedule(int school, String schedClass) {
        XmlParser parser = new XmlParser(getApplicationContext(), XmlParser.SCHEDULE);
    }
}