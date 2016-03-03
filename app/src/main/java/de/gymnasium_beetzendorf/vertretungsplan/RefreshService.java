package de.gymnasium_beetzendorf.vertretungsplan;


import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import java.util.List;

public class RefreshService extends IntentService {

    public RefreshService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Calendar calendar = Calendar.getInstance();
        String date = MainActivity.dateFormatter.format(calendar.getTime());
        long lastRefresh = 0;
        try {
            lastRefresh = MainActivity.dateTimeFormatter.parse(date + "20:00").getTime();

        } catch (ParseException e) {
            Log.i(MainActivity.TAG, "Problem while parsing in service class", e);
        }

        if (System.currentTimeMillis() < lastRefresh) {
            // download file from server
            downloadFile(MainActivity.SERVER_URL + MainActivity.SUBSTITUTION_QUERY_FILE, "substitution");

            // parse the results so we can use it
            List<Schoolday> xmlResults = XMLParser.parseSubstitutionXml(this);

            // instantiate the db handler
            DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);

            //
            List<Schoolday> databaseResults = databaseHandler.getAllSubstitutions();


            if (xmlResults.size() > 0 && databaseResults.size() > 0) {
                if (xmlResults.get(0).getLastUpdated() > databaseResults.get(0).getLastUpdated()) {
                    databaseHandler.insertSubstitutionXmlResults(xmlResults);

                    // intent that opens the main activity when notification is clicked
                    Intent notificationIntent = new Intent(this, MainActivity.class);
                    PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), notificationIntent, 0);

                    // build the notification and fire it to the user
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    Notification notification = new Notification.Builder(this)
                            .setContentTitle("Neue Vertretungsstunden!")
                            .setContentText("Hier klicken um App zu öffnen.")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setAutoCancel(true)
                            .setContentIntent(notificationPendingIntent)
                            .build();

                    notificationManager.notify(0, notification);
                }
            }
            databaseHandler.insertSubstitutionXmlResults(xmlResults);

        } else {
            Intent alarmIntent = new Intent(this, RefreshService.class);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, BootReceiver.alarmManagerRequestCode, alarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(alarmPendingIntent);

            // adds 10 hours (in milliseconds to the last refresh) which represents 6 am the next morning
            long nextDayRefresh = lastRefresh + (1000 * 60 * 60 * 10);
            // set the new alarm which will start firing alarms every hour until 6 pm
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, nextDayRefresh, AlarmManager.INTERVAL_HOUR, alarmPendingIntent);
        }
    }

    public boolean checkIfAppIsRunning () {
        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> runningTaskInfos = manager.getRunningAppProcesses();
        return true;
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
                case "substituion":
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
            Log.i(Helper.TAG, "Datei konnte nicht gefunden werden", e);
        } catch (MalformedURLException e) {
            Log.i(Helper.TAG, "URL inkorrekt", e);
        } catch (IOException e) {
            Log.i(Helper.TAG, "IOException", e);
        }

    }
}