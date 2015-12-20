package nz.pbomb.xposed.superkiwi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import common.PACKAGES;
import common.PREFERENCES;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onCreateValidation();
    }

    /**
     * Validates operations when the application is created. Firstly check that the
     * SharedPreferences exist and if it doesn't then create the SharedPreferences accordingly.
     * Also checks whether either ANZ or Semble (any version) is installed
     */
    private void onCreateValidation() {
        // Get the preference fragment displayed on this activity
        PreferenceFragment preferenceFragment = (PreferenceFragment) getFragmentManager().findFragmentById(R.id.mainPrefFragment);
        // Get the SharedPreferences for this module (and produce and editor as well)
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCES.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();

        // Create the SharedPreferences and set the defaults if they aren't already created
        if(!sharedPref.contains(PREFERENCES.KEYS.ANZ.ROOT_DETECTION)) {
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION);
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE);
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION);

            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.OTHER.ANZ_INSTALLED, PREFERENCES.DEFAULT_VALUES.OTHER.ANZ_INSTALLED);
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.OTHER.SEMBLE_INSTALLED, PREFERENCES.DEFAULT_VALUES.OTHER.SEMBLE_INSTALLED);

            sharedPrefEditor.apply();
        }

        // Checks if ANZ GoMoney is installed and if its not disable the preference option in the
        // fragment and disable any related modifications
        if(!isANZGoMoneyInstalled()) {
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, false);
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, false);
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.OTHER.ANZ_INSTALLED, false);
            sharedPrefEditor.apply();

            preferenceFragment.getPreferenceManager().findPreference(PREFERENCES.KEYS.MAIN.ANZ).setEnabled(false);
        } else {
            // If the ANZ GoMoney application has been installed recently, re-enable the default preferences
            if(!sharedPref.getBoolean(PREFERENCES.KEYS.OTHER.ANZ_INSTALLED, PREFERENCES.DEFAULT_VALUES.OTHER.ANZ_INSTALLED)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.OTHER.ANZ_INSTALLED, true);

                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION);
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE);
            }

        }

        // Checks if Semble is installed and if its not disable the preference option in the
        // fragment and disable any related modifications
        if(!isSembleInstalled()) {
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, false);
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.OTHER.SEMBLE_INSTALLED, false);
            sharedPrefEditor.apply();

            preferenceFragment.getPreferenceManager().findPreference(PREFERENCES.KEYS.MAIN.SEMBLE).setEnabled(false);
        } else {
            // If the Semble application has been installed recently, re-enable the default preferences
            if(!sharedPref.getBoolean(PREFERENCES.KEYS.OTHER.SEMBLE_INSTALLED, PREFERENCES.DEFAULT_VALUES.OTHER.SEMBLE_INSTALLED)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.OTHER.SEMBLE_INSTALLED, true);

                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);  // Creates menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Determine which MenuItem was pressed and act accordingly based on the button pressed
        int id = item.getItemId();
        Intent intent = null;
        switch (id) {
            case R.id.action_help:
                intent = new Intent(getApplicationContext(), HelpActivity.class);
                break;
            case R.id.action_donate:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=QNQDESEMGWDPY"));
                break;
            case R.id.action_contact:
                intent = new Intent(getApplicationContext(), ContactActivity.class);
                break;
            case R.id.action_sourceCode:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pbombnz/ANZGoMoneyNZMods/"));
                break;
            case R.id.action_xda:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/xposed-anz-gomoney-zealand-mods-bypass-t3270623"));
                break;
        }
        startActivity(intent);
        return true;
    }

    /**
     * Checks if the ANZ GoMoney Application is installed.
     *
     * @return True if it is installed, otherwise return false.
     */
    private boolean isANZGoMoneyInstalled() {
        return isApplicationInstalled(PACKAGES.ANZ_GOMONEY);
    }

    /**
     *  Checks if any of the Semble Application variants (from either 2Degrees, Spark or Vodafone
     *  are installed.
     *
     * @return True if it is installed, otherwise return false.
     */
    private boolean isSembleInstalled() {
        return isApplicationInstalled(PACKAGES.SEMBLE_2DEGREES) || isApplicationInstalled(PACKAGES.SEMBLE_SPARK) || isApplicationInstalled(PACKAGES.SEMBLE_VODAFONE);
    }

    /**
     * Determines if an application is installed or not
     *
     * @param uri The package name of the application
     * @return True, if the package is installed, otherwise false
     */
    private boolean isApplicationInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean appInstalled;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }
        return appInstalled;
    }
}
