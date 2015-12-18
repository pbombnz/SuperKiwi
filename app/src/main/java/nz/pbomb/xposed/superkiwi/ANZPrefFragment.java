package nz.pbomb.xposed.superkiwi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.TextView;

import java.util.Map;

import common.PREFERENCES;

@SuppressWarnings("unchecked")
public class ANZPrefFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private SharedPreferences sharedPreferences;
    private Map<String, Boolean> oldPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure default values are applied.  In a real app, you would
        // want this in a shared function that is used to retrieve the
        // SharedPreferences wherever they are needed.
        //PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences_anz, false);
        addPreferencesFromResource(R.xml.preferences_anz);

        this.sharedPreferences = getActivity().getSharedPreferences(PREFERENCES.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        oldPreferences = (Map<String, Boolean>) sharedPreferences.getAll();

        //Find all preferences
        getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.ROOT_DETECTION).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE).setOnPreferenceChangeListener(this);

        getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.HELP).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals(PREFERENCES.KEYS.ANZ.ROOT_DETECTION)) {
            return onRootDetectionPreferenceChange(preference, newValue);
        } else /*if(preference.getKey().equals(PREFERENCES.KEYS.SPOOF_DEVICE))*/ {
            return onSpoofDevicePreferenceChange(preference, newValue);
        } /*else {
            return false;
        }*/
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

    private boolean onSpoofDevicePreferenceChange(Preference preference, Object newValue) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        if (!((CheckBoxPreference) preference).isChecked()) {
            sharedPreferencesEditor.putBoolean(preference.getKey(), true).apply();
            displaypoofDeviceCheckedAlertDialog();
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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Intent goToNextActivity = new Intent(getActivity().getApplicationContext(), ANZPrefHelpActivity.class);
        startActivity(goToNextActivity);
        return true;
    }

    /**
     * Displays the Alert Dialog when leaving the application
     */
    public void displaypoofDeviceCheckedAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.spoofDeviceChecked_message));
        builder.setCancelable(false);
        builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
