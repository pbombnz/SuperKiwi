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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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


        getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.ROOT_DETECTION).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE).setOnPreferenceChangeListener(this);
        getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        if (!((CheckBoxPreference) preference).isChecked()) {
            sharedPreferencesEditor.putBoolean(key, true).apply();

            if(key.equals(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED)) {
                displayScreenshotsEnabledCheckedDialog();
            } else if(key.equals(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE)) {
                displaySpoofDeviceCheckedAlertDialog();
            }

        } else {
            sharedPreferencesEditor.putBoolean(key, false).apply();
        }

        // If the Screenshot feature was toggled, we need to end any ANZ process in order for the setting to be changed in real-time
        if(key.equals(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED)) {
            // The primary way of killing the ANZ GoMoney application
            ActivityManager mActivityManager = (ActivityManager) getActivity().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            mActivityManager.killBackgroundProcesses(PACKAGES.ANZ_GOMONEY);

            // The secondary way of killing the ANZ GoMoney application used as a backup.
            // This is mainly for lollipop and TouchWiz ROMs
            try {
                Process process = Runtime.getRuntime().exec(new String[] { "am force-stop " + PACKAGES.ANZ_GOMONEY });
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Checks if the SharedPreferences preferences have changed
     * @return true, if the values are now different, otherwise return false
     */
    public boolean hasValuesChanged() {
        // Retrieve the latest SharedPreferences
        Map<String, Boolean> currentPreferences = (Map<String, Boolean>) sharedPreferences.getAll();
        // Loop through all keys and compare the old preferences with the new preferences to detect
        // a change
        for(String key: oldPreferences.keySet()) {
            // Ignore the screenshot enabled preferences as this preference doesn't require any clearing for data or cache
            if(key.equals(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED)) {
                continue;
            }

            if(!oldPreferences.get(key).equals(currentPreferences.get(key))) {
                return true;
            }
        }
        // No change was detected
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
        builder.setTitle("ANZ GoMoney Application needs to be relaunched");
        //builder.setMessage("Enabling this feature can be a very dangerous operation. It's recommended if you need to copy your account deposit number use the copy-paste features within the application instead. Use this feature at your own risk!");
        builder.setMessage("If you have opened ANZ GoMoney NZ previously and it is still in the recent apps list, then simply clear it from the list. Next time you open the application, the screenshot feature will be activated or deactivated (depending on your actions). No system restart is required.");
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
