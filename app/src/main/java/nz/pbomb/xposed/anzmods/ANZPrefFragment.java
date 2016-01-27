package nz.pbomb.xposed.anzmods;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.TextView;

import java.util.Map;

import common.PACKAGES;
import common.PREFERENCES;

@SuppressWarnings("unchecked")
public class ANZPrefFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private SharedPreferences sharedPreferences;
    private Map<String, Boolean> oldPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_anz);
        this.sharedPreferences = getActivity().getSharedPreferences(PREFERENCES.SHARED_PREFS_FILE_NAME, Context.MODE_WORLD_READABLE);
        oldPreferences = (Map<String, Boolean>) sharedPreferences.getAll();

        //Find all preferences
        getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.ROOT_DETECTION).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals(PREFERENCES.KEYS.ANZ.ROOT_DETECTION)) {
            return onRootDetectionPreferenceChange(preference, newValue);
        } else if(preference.getKey().equals(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE)) {
            return onSpoofDevicePreferenceChange(preference, newValue);
        } else if(preference.getKey().equals(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED)) {
            return onScreenshotEnabledTogglePreferenceChange(preference, newValue);
        } else {
            return false;
        }
    }

    private boolean onScreenshotEnabledTogglePreferenceChange(Preference preference, Object newValue) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        if (!((CheckBoxPreference) preference).isChecked()) {
            sharedPreferencesEditor.putBoolean(preference.getKey(), true).apply();
            displayScreenshotsEnabledCheckedDialog();

        } else {
            sharedPreferencesEditor.putBoolean(preference.getKey(), false).apply();
        }

        ActivityManager mActivityManager = (ActivityManager) getActivity().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        mActivityManager.killBackgroundProcesses(PACKAGES.ANZ_GOMONEY);

        return true;
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

    private boolean onSpoofDevicePreferenceChange(Preference preference, Object newValue) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        if (!((CheckBoxPreference) preference).isChecked()) {
            sharedPreferencesEditor.putBoolean(preference.getKey(), true).apply();
            displaySpoofDeviceCheckedAlertDialog();
        } else {
            sharedPreferencesEditor.putBoolean(preference.getKey(), false).apply();
        }
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


    /**
     * Displays the Alert Dialog when leaving the application
     */
    public void displaySpoofDeviceCheckedAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.ANZPrefActivity_spoofDeviceChecked_message));
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

    /**
     * Displays the Alert Dialog when leaving the application
     */
    public void displayScreenshotsEnabledCheckedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Enabling this feature can be a very dangerous operation. It's recommended if you need to copy your account deposit number use the copy-paste features within the application instead. Use this feature at your own risk!");
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
