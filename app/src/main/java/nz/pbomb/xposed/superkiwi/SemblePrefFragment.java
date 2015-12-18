package nz.pbomb.xposed.superkiwi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.TextView;

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

        TextView subMessageTextView = (TextView) (getActivity().findViewById(R.id.compatibility_submessage));

        if (!((CheckBoxPreference) preference).isChecked()) {
            sharedPreferencesEditor.putBoolean(preference.getKey(), true).apply();

            if(subMessageTextView.getCurrentTextColor() != getResources().getColor(android.R.color.holo_red_dark)) {
                subMessageTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                subMessageTextView.setText(getResources().getString(R.string.ANZPrefActivity_compatibility_submessage_supported));
            }
        } else {
            sharedPreferencesEditor.putBoolean(preference.getKey(), false).apply();

            if(subMessageTextView.getCurrentTextColor() != getResources().getColor(android.R.color.holo_red_dark)) {
                subMessageTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                subMessageTextView.setText(getResources().getString(R.string.ANZPrefActivity_compatibility_submessage_rootdetectiondisabled));
            }
        }

        sharedPreferencesEditor.apply();

        //ActivityManager activityManager = (ActivityManager) this.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        //activityManager.killBackgroundProcesses(PACKAGES.ANZ_GOMONEY);

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
