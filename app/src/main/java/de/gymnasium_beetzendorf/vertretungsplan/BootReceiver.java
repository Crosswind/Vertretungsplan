package de.gymnasium_beetzendorf.vertretungsplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    public static final int alarmManagerRequestCode = 0;
    private long mFirstRefresh = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i(MainActivity.TAG, "boot received");
            Calendar calendar = Calendar.getInstance();
            String date = MainActivity.dateFormatter.format(calendar.getTime());

            try {
                mFirstRefresh = MainActivity.dateTimeFormatter.parse(date + " 6:00").getTime();
            } catch (ParseException e) {
                Log.i(MainActivity.TAG, "ParseException in RefreshService", e);
            }

            if (System.currentTimeMillis() > mFirstRefresh) {
                mFirstRefresh = System.currentTimeMillis();
            }

            // assign RefreshService class
            Intent alarmIntent = new Intent(context, RefreshService.class);
            PendingIntent alarmPendingIntent = PendingIntent.getService(context, alarmManagerRequestCode, alarmIntent, 0);
            // set the alarm
            // it starts at 6 am and repeats once an hour
            // elapsed_realtime is used to save ressources
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(
                    MainActivity.ALARM_TYPE,
                    mFirstRefresh,
                    MainActivity.ALARM_INTERVAL,
                    alarmPendingIntent);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(MainActivity.ALARM_REGISTERED, true).apply();
    }
}