package nz.pbomb.xposed.anzmods;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

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

        this.sharedPreferences = getActivity().getSharedPreferences(PREFERENCES.SHARED_PREFS_FILE_NAME, Context.MODE_WORLD_READABLE);
        oldPreferences = (Map<String, Boolean>) sharedPreferences.getAll();

        //Find all preferences
        getPreferenceManager().findPreference(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(PREFERENCES.KEYS.SEMBLE.MM_SUPPORT).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION)) {
            return onRootDetectionPreferenceChange(preference, newValue);
        } else if(preference.getKey().equals(PREFERENCES.KEYS.SEMBLE.MM_SUPPORT)) {
            Toast.makeText(getActivity().getApplicationContext(), "Always active. Cannot be disabled.", Toast.LENGTH_SHORT).show();
           return false;
        } else {
            return false;
        }
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
