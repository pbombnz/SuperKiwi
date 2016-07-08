package nz.pbomb.xposed.anzmods.fragments;

import android.app.ActivityManager;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.PACKAGES;
import common.SpoofDevice;
import common.SpoofDevices;
import nz.pbomb.xposed.anzmods.preferences.PREFERENCES;
import nz.pbomb.xposed.anzmods.R;

@SuppressWarnings("unchecked")
public class PrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
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
                    getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE_CHOOSER).setOnPreferenceClickListener(this);
                    getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED).setOnPreferenceChangeListener(this);
                } else if(getArguments().getString("preference").equals(PREFERENCES.KEYS.MAIN.SEMBLE)) {
                    getPreferenceManager().findPreference(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE_CHOOSER).setOnPreferenceClickListener(this);
                }
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        displaySpoofDeviceChooserDialog();
        return true;
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
                ActivityManager mActivityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
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
        builder.setTitle("Device Spoofing");
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

    public void displaySpoofDeviceChooserDialog() {
        List<String> templateList = new ArrayList<>();
        for(SpoofDevice spoofDevice : SpoofDevices.getDevices()) {
            templateList.add(spoofDevice.getHumanReadableName());
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.textview_dialog_item);
        arrayAdapter.addAll(templateList);

        final String intentPreference = getArguments().getString("preference");

        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setTitle("Pick a Spoof Device")
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String deviceName = arrayAdapter.getItem(which);
                        SharedPreferences.Editor sharedPrefEditor = getPreferenceManager().getSharedPreferences().edit();
                        if(intentPreference.equals(PREFERENCES.KEYS.MAIN.ANZ)) {
                            sharedPrefEditor.putString(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE_CHOOSER, deviceName);
                        } else if(intentPreference.equals(PREFERENCES.KEYS.MAIN.SEMBLE)) {
                            sharedPrefEditor.putString(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE_CHOOSER, deviceName);
                        }
                        sharedPrefEditor.apply();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
}
