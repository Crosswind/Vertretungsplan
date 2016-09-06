package de.gymnasium_beetzendorf.vertretungsplan.data;


import android.app.AlarmManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {

    // string used for debugging as a filter
    public static final String TAG = "Vertretungsplan";


    // identifiers used with SharedPreferences
    public static final String SCHOOL = "school";
    // boolean, if false then use CLASS_TO_SHOW
    public static final String SHOW_WHOLE_PLAN = "show_whole_plan";
    // string that represents the class the user wants to see
    public static final String CLASS_TO_SHOW = "class_to_show";

    // boolean to determine wheter the view should update itsself after leaving the preference screen
    public static final String PREFERENCES_CHANGED = "preferences_changed";

    // boolean, if false it immediately registers a new alarm to handle refreshing the data
    public static final String ALARM_REGISTERED = "alarm_registered";


    // date formatting variables - used all over the project
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    public static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
    public static final SimpleDateFormat weekdayFormatter = new SimpleDateFormat("EE", Locale.GERMANY);


    // alarm constants
    public static final long ALARM_INTERVAL = AlarmManager.INTERVAL_HALF_HOUR;
    public static final int ALARM_TYPE = AlarmManager.RTC;
}
