package de.gymnasium_beetzendorf.vertretungsplan.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Calendar;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.DatabaseHandler;
import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.RefreshService;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data.School;

public class PreferenceFragment extends PreferenceFragmentCompat
        implements Constants, SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        DatabaseHandler databaseHandler = new DatabaseHandler(getActivity(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);

        ListPreference schoolListPreference = findPreference("schoolName");
        // disable this as long as multiple school support has not been implemented
        schoolListPreference.setEnabled(false);

        List<String> schoolList = School.schoolListNames();
        String[] schoolListArray = new String[schoolList.size()];
        schoolListPreference.setEntries(schoolList.toArray(schoolListArray));
        schoolListPreference.setEntryValues(schoolList.toArray(schoolListArray));
        schoolListPreference.setEnabled(true);

        // set the correct school in the list preference.
        schoolListPreference.setValueIndex(sharedPreferences.getInt(Constants.PREFERENCE_SCHOOL, 0));
        String school;
        try {
            school = School.findSchoolById(sharedPreferences.getInt(Constants.PREFERENCE_SCHOOL, 0)).getName();
        } catch (IllegalAccessException e) {
            school = String.valueOf(sharedPreferences.getInt(Constants.PREFERENCE_SCHOOL, 0));
        }
        schoolListPreference.setSummary(school);

        // classes to chose from
        ListPreference classListPreference = findPreference("class_year_letter");

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
        refreshClassListPreferenceButton.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), RefreshService.class);
            intent.putExtra(RefreshService.INSTRUCTION, RefreshService.CLASSLIST_REFRESH);
            getActivity().startService(intent);
            return true;
        });

        // refresh substitution plan
        Preference refreshSubstitutionPreferenceButton = findPreference("refresh_substitution_plan");

        long last_substitution_plan_refresh = sharedPreferences.getLong("last_substitution_plan_refresh", 0);
        calendar.setTimeInMillis(last_substitution_plan_refresh);

        refreshSubstitutionPreferenceButton.setSummary("Letzter Plan vom: " + dateTimeFormatter.format(calendar.getTime()));
        refreshSubstitutionPreferenceButton.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), RefreshService.class);
            intent.putExtra(RefreshService.INSTRUCTION, RefreshService.SUBSTITUTION_REFRESH);
            getActivity().startService(intent);
            return true;
        });
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        sharedPreferences.edit().putBoolean("preferences_changed", true).apply();
        Calendar calendar = Calendar.getInstance();

        Log.i(TAG, "key: " + key);
        switch (key) {
            case "schoolName":
                ListPreference schoolList = (ListPreference) findPreference(key);
                schoolList.setSummary(schoolList.getEntry());

                // change the actual settings value
                sharedPreferences.edit().putInt(Constants.PREFERENCE_SCHOOL, School.findSchoolIdByName(schoolList.getValue())).apply();
                break;
            case "class_year_letter":
                ListPreference classList = (ListPreference) findPreference("class_year_letter");
                classList.setTitle("Klasse - " + classList.getEntry());
                break;
            case "last_class_list_refresh":
                Preference refreshClassListPreferenceButton = findPreference("refresh_class_list");
                calendar.setTimeInMillis(sharedPreferences.getLong("last_class_list_refresh", 0));
                refreshClassListPreferenceButton.setSummary("Zuletzt aktualisiert: " + dateTimeFormatter.format(calendar.getTime()));
                break;
            case "refresh_substitution_plan":
                Preference refreshSubstitutionPreferenceButton = findPreference(key);
                calendar.setTimeInMillis(sharedPreferences.getLong("last_substitution_plan_refresh", 0));
                refreshSubstitutionPreferenceButton.setSummary("Letzter Plan vom: " + dateTimeFormatter.format(calendar.getTime()));
                break;

        }
    }
}
