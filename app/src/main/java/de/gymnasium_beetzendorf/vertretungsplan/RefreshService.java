package de.gymnasium_beetzendorf.vertretungsplan;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.text.ParseException;
import java.util.Calendar;

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

        if (System.currentTimeMillis() >= lastRefresh) {
            Intent alarmIntent = new Intent(this, RefreshService.class);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, BootReceiver.alarmManagerRequestCode, alarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(alarmPendingIntent);

            // adds 10 hours (in milliseconds to the last refresh) which represents 6 am the next morning
            long nextDayRefresh = lastRefresh + (1000 * 60 * 60 * 10);
            // set the new alarm which will start firing alarms every hour until 6 pm
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, nextDayRefresh, AlarmManager.INTERVAL_HOUR, alarmPendingIntent);
        }

        // instantiate download
        class DownloadFiles extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }
        }
    }
}
