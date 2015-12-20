package nz.pbomb.xposed.anzmods;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.Map;

import common.PREFERENCES;

@SuppressWarnings("unchecked")
public class SemblePrefFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private SharedPreferences sharedPreferences;
    private Map<String, Boolean> oldPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_semble);

        this.sharedPreferences = getActivity().getSharedPreferences(PREFERENCES.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        oldPreferences = (Map<String, Boolean>) sharedPreferences.getAll();

        //Find all preferences
        getPreferenceManager().findPreference(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return onRootDetectionPreferenceChange(preference, newValue);
    }



    private boolean onRootDetectionPreferenceChange(Preference preference, Object newValue) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        if (!((CheckBoxPreference) preference).isChecked()) {
            sharedPreferencesEditor.putBoolean(preference.getKey(), true).apply();
        } else {
            sharedPreferencesEditor.putBoolean(preference.getKey(), false).apply();
        }
        sharedPreferencesEditor.apply();
        return true;
    }


    public boolean hasValuesChanged() {
        Map<String, Boolean> currentPreferences = (Map<String, Boolean>) sharedPreferences.getAll();
        for(String key: oldPreferences.keySet()) {
            if(!oldPreferences.get(key).equals(currentPreferences.get(key))) {
                return true;
            }
        }
        return false;
    }

}
