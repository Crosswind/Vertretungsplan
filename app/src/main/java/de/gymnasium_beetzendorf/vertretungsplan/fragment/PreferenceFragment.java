package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.DatabaseHandler;
import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.RefreshService;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data.School;

public class PreferenceFragment extends android.preference.PreferenceFragment
        implements Constants, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        DatabaseHandler databaseHandler = new DatabaseHandler(getActivity(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);

        ListPreference schoolListPreference = (ListPreference) findPreference("school");
        // disable this as long as multiple school support has not been implemented
        schoolListPreference.setEnabled(false);

        List<String> schoolList = School.schoolListNames();
        String[] schoolListArray = new String[schoolList.size()];
        schoolListPreference.setEntries(schoolList.toArray(schoolListArray));
        schoolListPreference.setEntryValues(schoolList.toArray(schoolListArray));
        schoolListPreference.setEnabled(true);


        if (schoolList.size() <= 1) {
            schoolListPreference.setDefaultValue("Gymnasium Beetzendorf");
            schoolListPreference.setTitle(schoolListPreference.getEntry());
        }

        // classes to chose from
        ListPreference classListPreference = (ListPreference) findPreference("class_to_show");

        List<String> classList = databaseHandler.getClassList();

        String[] classListArray = new String[classList.size()];
        classListArray = classList.toArray(classListArray);

        classListPreference.setEntries(classListArray);
        classListPreference.setEntryValues(classListArray);
        classListPreference.setDefaultValue(1);
        classListPreference.setTitle("Klasse - " + classListPreference.getEntry());

        // refresh class list
        Preference refreshClassListPreferenceButton = findPreference("refresh_class_list");

        long last_class_list_refresh = sharedPreferences.getLong("last_class_list_refresh", 0);
        Calendar calendar = Calendar.getInstance();

        String summary = "Zuletzt aktualisiert: ";
        if (last_class_list_refresh == 0) {
            summary += "nie";
        } else {
            calendar.setTimeInMillis(last_class_list_refresh);
            summary += dateTimeFormatter.format(calendar.getTime());
        }
        refreshClassListPreferenceButton.setSummary(summary);
        refreshClassListPreferenceButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), RefreshService.class);
                intent.putExtra("refresh_class_list", true);
                getActivity().startService(intent);
                return true;
            }
        });

        // refresh substitution plan
        Preference refreshSubstitutionPreferenceButton = findPreference("refresh_substitution_plan");

        long last_substitution_plan_refresh = sharedPreferences.getLong("last_substitution_plan_refresh", 0);
        calendar.setTimeInMillis(last_substitution_plan_refresh);

        refreshSubstitutionPreferenceButton.setSummary("Letzter Plan vom: " + dateTimeFormatter.format(calendar.getTime()));
        refreshSubstitutionPreferenceButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), RefreshService.class);
                intent.putExtra("manual_refresh", true);
                getActivity().startService(intent);
                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ListPreference classList = (ListPreference) findPreference("class_to_show");
        classList.setTitle("Klasse - " + classList.getEntry());

        sharedPreferences.edit().putBoolean("preferences_changed", true).apply();

        Preference refreshClassListPreferenceButton = findPreference("refresh_class_list");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sharedPreferences.getLong("last_class_list_refresh", 0));
        refreshClassListPreferenceButton.setSummary("Zuletzt aktualisiert: " + dateTimeFormatter.format(calendar.getTime()));

        Preference refreshSubstitutionPreferenceButton = findPreference("refresh_substitution_plan");
        calendar.setTimeInMillis(sharedPreferences.getLong("last_substitution_plan_refresh", 0));
        refreshSubstitutionPreferenceButton.setSummary("Letzter Plan vom: " + dateTimeFormatter.format(calendar.getTime()));

    }
}
