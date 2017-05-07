package de.gymnasium_beetzendorf.vertretungsplan.data;


import android.app.AlarmManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

public interface Constants {

    // string used for debugging as a filter
    String TAG = "Vertretungsplan";

    // identifiers used with SharedPreferences
    String PREFERENCE_SCHOOL = "school";
    String PREFERENCE_CLASS_YEAR_LETTER = "class_year_letter";
    String PREFERENCE_SHOW_WHOLE_PLAN = "show_whole_plan";
    String PREFERENCE_CURRENT_VERSION = "current_or_old_version";
    String PREFERENCE_NOTIFICATIONS_ENABLED = "enable_notifications";
    String PREFERENCE_LAST_SUBSTITUTION_REFRESH = "last_substitution_plan_refresh";

    // boolean to determine wheter the view should update itsself after leaving the preference screen
    String PREFERENCES_CHANGED = "preferences_changed";

    // boolean, if false it immediately registers a new alarm to handle refreshing the data
    String PREFERENCE_ALARM_REGISTERED = "alarm_registered";


    // date formatting variables - used all over the project
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
    SimpleDateFormat dateTimeFormatterKomma = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.GERMANY);
    SimpleDateFormat weekdayFormatter = new SimpleDateFormat("EE", Locale.GERMANY);
    SimpleDateFormat dateFormatterMonthName = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMANY);


    // alarm constants
    long ALARM_INTERVAL = AlarmManager.INTERVAL_HALF_HOUR;
    int ALARM_TYPE = AlarmManager.RTC;

    // server stuff
    String SERVER_URL = "http://vplankl.gymnasium-beetzendorf.de";
    String SUBSTITUTION_QUERY_FILE = "/Vertretungsplan_Klassen.xml";

}
