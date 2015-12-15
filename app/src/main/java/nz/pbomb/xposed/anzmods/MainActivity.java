package nz.pbomb.xposed.anzmods;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.content.pm.PackageInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import common.PACKAGES;
import common.SETTINGS;


/**
 * A UI for the xposed module that allows the users to toggle preferences and features
 *
 * @author Prashant Bhikhu (PBombNZ)
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkNfcExists();
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
            case R.id.action_help:
                intent = new Intent(getApplicationContext(), HelpActivity.class);
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

    @Override
    protected void onStart() {
        super.onStart();
        if(!getPackageManager().hasSystemFeature("android.hardware.nfc.hce")) {
            return;
        }
        // Check if the ANZ GoMoney application is compatible with this xposed module

        // Get information from the GoMoney application
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(PACKAGES.ANZ_GOMONEY, 0);
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            e.printStackTrace();
            return;
        }
        // Get current version of ANZ GoMoney application
        String anzCurrentVerName = pInfo.versionName;
        // Retrieve the version numbers that this module can support
        String[] anzSupportedVersions = getResources().getStringArray(R.array.ANZ_supported_app_versions);
        // Assume that the module is not supported at first
        boolean isSupportedVersion = false;

        // Determine whether this module is supported
        for(String supportedVersion : anzSupportedVersions) {
            if(anzCurrentVerName.equals(supportedVersion)) {
                isSupportedVersion = true;
                break;
            }
        }

        // Change the textview's text and color to display the compatibility of the xposed module
        // and the ANZ GoMoney application in the UI

        // Get the textview's
        TextView tvMessage = (TextView) this.findViewById(R.id.compatibility_message);
        TextView tvSubMessage = (TextView) this.findViewById(R.id.compatibility_submessage);

        if(isSupportedVersion) {
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            tvSubMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            tvMessage.setText(R.string.compatibility_message_supported);
            tvSubMessage.setText(R.string.compatibility_submessage_supported);
        } else {
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            tvSubMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            tvMessage.setText(R.string.compatibility_message_notsupported);
            tvSubMessage.setText(R.string.compatibility_submessage_notsupported);

            PreferenceFragment prefFragment = ((PreferenceFragment) getFragmentManager().findFragmentById(R.id.prefFragment));
            CheckBoxPreference rootDetectionPreference = (CheckBoxPreference) prefFragment.getPreferenceManager().findPreference(SETTINGS.KEYS.ROOT_DETECTION);
            rootDetectionPreference.setChecked(false);
            rootDetectionPreference.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        // Get the child preference fragment
        MainActivity_PreferencesFragment preferenceFragment = (MainActivity_PreferencesFragment) getFragmentManager().findFragmentById(R.id.prefFragment);

        // Display a restart and warning dialog if values have been changed otherwise no message
        if(preferenceFragment.hasValuesChanged()) {
            onFinishAlertDialog();
        } else {
            MainActivity.this.finish();
        }
    }

    /**
     * Displays the Alert Dialog when leaving the application
     */
    public void onFinishAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.quit_title));
        builder.setMessage(getResources().getString(R.string.quit_message));
        builder.setCancelable(false);
        builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Displays the Alert Dialog when leaving the application
     */
    public void checkNfcExists() {
        if(getPackageManager().hasSystemFeature("android.hardware.nfc.hce")) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Device Not Compatible");
        builder.setMessage("Your device is not compatible due to the lack of NFC or has no HCE support. This module will not provide any additonal benefits for you.");
        builder.setCancelable(false);
        builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
