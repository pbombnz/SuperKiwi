package nz.pbomb.xposed.superkiwi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.content.pm.PackageInfo;
import android.widget.TextView;

import common.PACKAGES;
import common.PREFERENCES;


/**
 * A UI for the xposed module that allows the users to toggle preferences and features
 *
 * @author Prashant Bhikhu (PBombNZ)
 */
public class ANZPrefActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anz);

        NfcValidation();
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

            tvMessage.setText(R.string.ANZPrefActivity_compatibility_message_supported);
            tvSubMessage.setText(R.string.ANZPrefActivity_compatibility_submessage_supported);
        } else {
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            tvSubMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            tvMessage.setText(R.string.ANZPrefActivity_compatibility_message_notsupported);
            tvSubMessage.setText(R.string.ANZPrefActivity_compatibility_submessage_notsupported);

            PreferenceFragment prefFragment = ((PreferenceFragment) getFragmentManager().findFragmentById(R.id.prefFragment));
            CheckBoxPreference rootDetectionPreference = (CheckBoxPreference) prefFragment.getPreferenceManager().findPreference(PREFERENCES.KEYS.ANZ.ROOT_DETECTION);
            rootDetectionPreference.setChecked(false);
            rootDetectionPreference.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        // Get the child preference fragment
        ANZPrefFragment preferenceFragment = (ANZPrefFragment) getFragmentManager().findFragmentById(R.id.prefFragment);

        // Display a restart and warning dialog if values have been changed otherwise no message
        if(preferenceFragment.hasValuesChanged()) {
            onFinishAlertDialog();
        } else {
            ANZPrefActivity.this.finish();
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
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Displays the Alert Dialog when leaving the application
     */
    public void NfcValidation() {
        if(getPackageManager().hasSystemFeature("android.hardware.nfc.hce")) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle("Device Not Compatible")
            .setMessage("Your device is not compatible due to the lack of NFC or lack of HCE support. These preferences will not provide any benefits for you.")
            .setCancelable(false)
            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
