package de.gymnasium_beetzendorf.vertretungsplan;


import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RefreshService extends IntentService {

    private SharedPreferences mSharedPreferences;

    public RefreshService() {
        super("RefreshService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(MainActivity.TAG, "service call received");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Calendar calendar = Calendar.getInstance();
        String date = MainActivity.dateFormatter.format(calendar.getTime());
        long lastRefresh = 0;
        try {
            lastRefresh = MainActivity.dateTimeFormatter.parse(date + " 16:00").getTime();

        } catch (ParseException e) {
            Log.i(MainActivity.TAG, "Problem while parsing in service class", e);
        }

        // download file from server
        downloadFile(MainActivity.SERVER_URL + MainActivity.SUBSTITUTION_QUERY_FILE, "substitution");

        // parse the results so we can use it
        List<Schoolday> xmlResults = XMLParser.parseSubstitutionXml(this);

        // instantiate the db handler
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
        List<Schoolday> databaseResults = databaseHandler.getAllSubstitutions();

        if (System.currentTimeMillis() <= lastRefresh) {



            if (!isUpToDate(xmlResults, databaseResults) && mSharedPreferences.getBoolean("enable_notifications", true)) {
                databaseHandler.insertSubstitutionXmlResults(xmlResults);

                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(xmlResults.get(0).getLastUpdated());

                String updated = MainActivity.dateTimeFormatter.format(calendar.getTime());

                // intent that opens the main activity when notification is clicked
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), notificationIntent, 0);

                // build the notification and fire it to the user
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new Notification.Builder(this)
                        .setContentTitle("Neue Vertretungsstunden!")
                        .setContentText("Aktualisiert: " + updated)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent)
                        .build();

                notificationManager.notify(0, notification);
            }

        } else {
            Intent alarmIntent = new Intent(this, RefreshService.class);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent alarmPendingIntent = PendingIntent.getService(this, BootReceiver.alarmManagerRequestCode, alarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(alarmPendingIntent);

            // adds 14 hours (in milliseconds to the last refresh) which represents 6 am the next morning
            long nextDayRefresh = lastRefresh + (1000 * 60 * 60 * 14);
            // set the new alarm which will start firing alarms every fifteen minutes until 8 pm
            alarmManager.setInexactRepeating(
                    MainActivity.ALARM_TYPE,
                    nextDayRefresh,
                    MainActivity.ALARM_INTERVAL,
                    alarmPendingIntent);

            Log.i(MainActivity.TAG, "Alarm set");

            mSharedPreferences.edit().putBoolean(MainActivity.ALARM_REGISTERED, true).apply();
        }
    }


    // not yet implemented
    public boolean checkIfAppIsRunning() {
        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> runningTaskInfos = manager.getRunningAppProcesses();
        return true;
    }

    private boolean isUpToDate(List<Schoolday> xmlResults, List<Schoolday> databaseResults) {
        long databaseLastUpdated, xmlLastUpdated;
        try {
            databaseLastUpdated = databaseResults.get(0).getLastUpdated();
        } catch (IndexOutOfBoundsException e) {
            Log.i(MainActivity.TAG, "IndexOutOfBoundsException: ", e);
            return false;
        }

        try {
            xmlLastUpdated = xmlResults.get(0).getLastUpdated();
        } catch (IndexOutOfBoundsException e) {
            Log.i(MainActivity.TAG, "IndexOutOfBoundsException: ", e);
            return true;
        }

        Log.i(MainActivity.TAG, "db: " + MainActivity.dateTimeFormatter.format(new Date(databaseLastUpdated)) +
                "\nxml: " + MainActivity.dateTimeFormatter.format(new Date(xmlLastUpdated)));

        return databaseLastUpdated > xmlLastUpdated;
    }

    private void downloadFile(String QUERY_URL, String fileType) {
        try {
            // open connection and streams for writing the file
            URL url = new URL(QUERY_URL);
            URLConnection urlConnection = url.openConnection();

            InputStream is = urlConnection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            String fileName;
            switch (fileType) {
                case "substitution":
                    fileName = "substitution.xml";
                    break;
                case "schedule":
                    fileName = "schedule_" + "class" + ".xml";
                    break;
                default:
                    fileName = "temp.xml";
                    break;
            }

            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            // writing to file
            byte data[] = new byte[1024];
            int count;
            while ((count = bis.read(data)) != -1) {
                bos.write(data, 0, count);
            }

            // close streams so the file does not get corrupted
            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            Log.i(MainActivity.TAG, "Datei konnte nicht gefunden werden", e);
        } catch (MalformedURLException e) {
            Log.i(MainActivity.TAG, "URL inkorrekt", e);
        } catch (IOException e) {
            Log.i(MainActivity.TAG, "IOException", e);
        }

    }


    // interface to check if app is running in foreground or went to background
    // needed to either push notification or don't do it because the user is in the app
    /*public interface CheckIfAppIsOpened {
        boolean isAppInForeground();
    }*/

}