package nz.pbomb.xposed.anzmods.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import nz.pbomb.xposed.anzmods.Common;
import nz.pbomb.xposed.anzmods.preferences.PREFERENCES;
import nz.pbomb.xposed.anzmods.R;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences mSharedPreferences;


    protected PreferenceFragment preferenceFragment;

    @Override
    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(Common.getInstance().SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        preferenceFragment = (PreferenceFragment) getFragmentManager().findFragmentById(R.id.mainPrefFragment);

        new ValidationASyncTask().execute();
    }

    /**
     * Validates operations when the application is created. Firstly check that the
     * SharedPreferences exist and if it doesn't then create the SharedPreferences accordingly.
     * Also checks whether either ANZ or Semble (any version) is installed
     */
    protected class ValidationASyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences.Editor sharedPrefEditor = mSharedPreferences.edit();

            // Create the SharedPreferences and set the defaults if they aren't already created
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.ASB.ROOT_DETECTION)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ASB.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ASB.ROOT_DETECTION);
            }
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.ANZ.ROOT_DETECTION)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION);
            }
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE);
            }
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE_CHOOSER)) {
                sharedPrefEditor.putString(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE_CHOOSER, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE_CHOOSER);
            }
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED, PREFERENCES.DEFAULT_VALUES.ANZ.SCREENSHOT_ENABLED);
            }
            /*if (!mSharedPreferences.contains(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION);
            }
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SEMBLE.SPOOF_DEVICE);
            }
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE_CHOOSER)) {
                sharedPrefEditor.putString(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE_CHOOSER, PREFERENCES.DEFAULT_VALUES.SEMBLE.SPOOF_DEVICE_CHOOSER);
            }
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.SEMBLE.MM_SUPPORT)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.SEMBLE.MM_SUPPORT, PREFERENCES.DEFAULT_VALUES.SEMBLE.MM_SUPPORT);
            }*/
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.TVNZ.ROOT_DETECTION)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.TVNZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TVNZ.ROOT_DETECTION);
            }
            /*if (!mSharedPreferences.contains(PREFERENCES.KEYS.TVNZ.HDMI_DETECTION)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.TVNZ.HDMI_DETECTION, PREFERENCES.DEFAULT_VALUES.TVNZ.HDMI_DETECTION);
            }*/
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION);
            }
            if (!mSharedPreferences.contains(PREFERENCES.KEYS.MAIN.DEBUG)) {
                sharedPrefEditor.putBoolean(PREFERENCES.KEYS.MAIN.DEBUG, PREFERENCES.DEFAULT_VALUES.MAIN.DEBUG);
            }
            if (mSharedPreferences.getBoolean(PREFERENCES.KEYS.MAIN.DEBUG, PREFERENCES.DEFAULT_VALUES.MAIN.DEBUG) || Common.getInstance().DEBUG) {
                setTitle(getTitle() + " (Debug Mode)");
            }
            sharedPrefEditor.apply();

            if(Common.getInstance().DEBUG) {
                preferenceFragment.findPreference(PREFERENCES.KEYS.MAIN.DEBUG).setEnabled(false);
            }

            // Checks if ASB is installed and if its not disable the preference option in the
            // fragment
            if (!isApplicationInstalled(Common.getInstance().PACKAGE_ASB_MOBILE)) {
                preferenceFragment.findPreference(PREFERENCES.KEYS.MAIN.ASB).setEnabled(false);
            }

            // Checks if ANZ GoMoney is installed and if its not disable the preference option in the
            // fragment
            if (!isApplicationInstalled(Common.getInstance().PACKAGE_ANZ_GOMONEY)) {
                preferenceFragment.findPreference(PREFERENCES.KEYS.MAIN.ANZ).setEnabled(false);
            }

            /*
            // Checks if Semble is installed and if its not disable the preference option in the
            // fragment
            if (!(isApplicationInstalled(Common.getInstance().PACKAGE_SEMBLE_2DEGREES) ||
                    isApplicationInstalled(Common.getInstance().PACKAGE_SEMBLE_SPARK) ||
                    isApplicationInstalled(Common.getInstance().PACKAGE_SEMBLE_VODAFONE))) {
                preferenceFragment.findPreference(PREFERENCES.KEYS.MAIN.SEMBLE).setEnabled(false);
            }*/

            // Checks if TVNZ is installed and if its not disable the preference option in the
            // fragment
            if (!isApplicationInstalled(Common.getInstance().PACKAGE_TVNZ_ONDEMAND)) {
                preferenceFragment.findPreference(PREFERENCES.KEYS.MAIN.TVNZ).setEnabled(false);
            }

            // Checks if 3NOW is installed and if its not disable the preference option in the
            // fragment
            if (!isApplicationInstalled(Common.getInstance().PACKAGE_TV3NOW)) {
                preferenceFragment.findPreference(PREFERENCES.KEYS.MAIN.TVNZ).setEnabled(false);
            }
            return null;
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
        Intent intent = null;
        switch (item.getItemId()) {
            //case R.id.action_help:
            //    intent = new Intent(getApplicationContext(), HelpActivity.class);
            //    break;
            case R.id.action_donate:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=QNQDESEMGWDPY"));
                break;
            case R.id.action_contact:
                intent = new Intent(getApplicationContext(), ContactActivity.class);
                break;
            case R.id.action_sourceCode:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pbombnz/SuperKiwi"));
                break;
            case R.id.action_xda:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/xposed-anz-gomoney-zealand-mods-bypass-t3270623"));
                break;
            case R.id.action_about:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                break;
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.nothing);
        return true;
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

    /*@Override
    protected void onDestroy() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true,false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true,false);
        super.onPause();
    }*/
}
