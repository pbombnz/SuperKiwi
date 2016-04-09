package nz.pbomb.xposed.anzmods;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import common.PACKAGES;
import common.PREFERENCES;

@SuppressWarnings("unchecked")
public class PrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
    private Map<String, ?> oldPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(getArguments().getInt("id"));

        //this.sharedPreferences = getActivity().getSharedPreferences(PREFERENCES.SHARED_PREFS_FILE_NAME, Context.MODE_WORLD_READABLE);
        //oldPreferences = (Map<String, Boolean>) sharedPreferences.getAll();
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        oldPreferences = getPreferenceManager().getSharedPreferences().getAll();

        if(getArguments() != null) {
            if(getArguments().containsKey("preference")) {
                if (getArguments().getString("preference").equals(PREFERENCES.KEYS.MAIN.ANZ)) {
                    //getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.ROOT_DETECTION).setOnPreferenceChangeListener(this);
                    getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE).setOnPreferenceChangeListener(this);
                    getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED).setOnPreferenceChangeListener(this);
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        CheckBoxPreference cp = ((CheckBoxPreference) preference);
        String key = cp.getKey();

        if(getArguments().getString("preference").equals(PREFERENCES.KEYS.MAIN.ANZ)) {
            if(key.equals(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE)) {
                if (Boolean.valueOf(newValue.toString())) {
                    displaySpoofDeviceCheckedDialog();
                }
            } else if(key.equals(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED)) {
                // The primary way of killing the ANZ GoMoney application
                ActivityManager mActivityManager = (ActivityManager) getActivity().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                mActivityManager.killBackgroundProcesses(PACKAGES.ANZ_GOMONEY);

                // The secondary way of killing the ANZ GoMoney application used as a backup.
                // This is mainly for lollipop and TouchWiz ROMs where the primary way may not always work.
                try {
                    Process process = Runtime.getRuntime().exec(new String[] { "am force-stop " + PACKAGES.ANZ_GOMONEY });
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    in.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
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
        Map<String, ?> currentPreferences = getPreferenceManager().getSharedPreferences().getAll();
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
    public void displaySpoofDeviceCheckedDialog() {
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
    /*public void displayScreenshotsEnabledCheckedDialog() {
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
    }*/
}
