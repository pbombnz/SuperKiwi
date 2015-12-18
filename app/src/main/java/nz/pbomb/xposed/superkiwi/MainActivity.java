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

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor x = sharedPreferences.edit();

        PreferenceFragment preferenceFragment = (PreferenceFragment) getFragmentManager().findFragmentById(R.id.mainPrefFragment);

        if(!isANZGoMoneyInstalled()) {
            x.putBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, false);
            x.putBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE,false);
            x.apply();
            preferenceFragment.getPreferenceManager().findPreference(PREFERENCES.KEYS.MAIN.ANZ).setEnabled(false);
        }

        if(!isSembleInstalled()) {
            x.putBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, false);
            x.apply();
            preferenceFragment.getPreferenceManager().findPreference(PREFERENCES.KEYS.MAIN.SEMBLE).setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        switch (id) {
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


    private boolean isANZGoMoneyInstalled() {
        return isApplicationInstalled(PACKAGES.ANZ_GOMONEY);
    }

    private boolean isSembleInstalled() {
        return isApplicationInstalled(PACKAGES.SEMBLE_2DEGREES) || isApplicationInstalled(PACKAGES.SEMBLE_SPARK) || isApplicationInstalled(PACKAGES.SEMBLE_VODAFONE);
    }

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
