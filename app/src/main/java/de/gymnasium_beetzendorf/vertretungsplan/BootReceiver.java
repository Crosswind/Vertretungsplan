package de.gymnasium_beetzendorf.vertretungsplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.util.Calendar;

import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;

public class BootReceiver extends BroadcastReceiver implements Constants {

    public static final int alarmManagerRequestCode = 0;
    private long mFirstRefresh = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i(TAG, "boot received");
            Calendar calendar = Calendar.getInstance();
            String date = dateFormatter.format(calendar.getTime());

            try {
                mFirstRefresh = dateTimeFormatter.parse(date + " 6:00").getTime();
            } catch (ParseException e) {
                Log.i(TAG, "ParseException in BootReceiver", e);
            }

            if (System.currentTimeMillis() > mFirstRefresh) {
                mFirstRefresh = System.currentTimeMillis();
            }

            // assign RefreshService class
            Intent alarmIntent = new Intent(context, RefreshService.class);
            PendingIntent alarmPendingIntent = PendingIntent.getService(context, alarmManagerRequestCode, alarmIntent, 0);
            // set the alarm
            // it starts at 6 am and repeats every 30 min

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(
                    ALARM_TYPE,
                    mFirstRefresh,
                    ALARM_INTERVAL,
                    alarmPendingIntent);
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(ALARM_REGISTERED, true).apply();
    }
}