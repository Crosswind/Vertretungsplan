package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

public class PreferenceFragment extends android.preference.PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        // assign the values of the ListPreference
        // so far it's just a static array defined in MainActivity
        ListPreference classList = (ListPreference) findPreference("class_to_show");
        classList.setEntries(MainActivity.getClasses());
        classList.setEntryValues(MainActivity.getClasses());
        classList.setDefaultValue(1);
        classList.setTitle("Klasse - " + classList.getEntry());

        /*ListPreference notificationInterval = (ListPreference) findPreference("notification_interval");
        notificationInterval.setEntries(R.array.notification_types_entries);
        notificationInterval.setEntryValues(R.array.notification_types_values);
        notificationInterval.setDefaultValue(2);*/
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ListPreference classList = (ListPreference) findPreference("class_to_show");
        classList.setTitle("Klasse - " + classList.getEntry());

        sharedPreferences.edit().putBoolean("preferences_changed", true).apply();
    }
}
