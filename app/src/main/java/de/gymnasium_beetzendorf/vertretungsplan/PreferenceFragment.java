package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

public class PreferenceFragment extends android.preference.PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        ListPreference classList = (ListPreference) findPreference("class_to_show");
        classList.setEntries(MainActivity.getClasses());
        classList.setEntryValues(MainActivity.getClasses());
        classList.setDefaultValue(MainActivity.getClasses()[0]);
        // Log.i(MainActivity.TAG, "value of the first class: " + MainActivity.getClasses()[0]);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.edit().putBoolean("preferences_changed", true).apply();
    }
}
