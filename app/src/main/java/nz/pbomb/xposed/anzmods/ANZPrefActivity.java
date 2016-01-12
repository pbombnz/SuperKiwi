package nz.pbomb.xposed.anzmods;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.io.File;

import common.GLOBAL;
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

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Get the child preference fragment
        ANZPrefFragment preferenceFragment = (ANZPrefFragment) getFragmentManager().findFragmentById(R.id.anz_prefFragment);

        // Display a restart and warning dialog if values have been changed otherwise no message
        if(preferenceFragment.hasValuesChanged()) {
            onFinishAlertDialog();
        } else {
            finish();
        }
    }

    /**
     * Displays the Alert Dialog when leaving the application
     */
    public void onFinishAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.ANZPrefActivity_quit_title));
        builder.setMessage(getResources().getString(R.string.ANZPrefActivity_quit_message));
        builder.setCancelable(false);
        builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean hasHostBasedCardEmulation() {
        return GLOBAL.DEBUG || getPackageManager().hasSystemFeature("android.hardware.nfc.hce");
    }

    /**
     * Displays the Alert Dialog when leaving the application
     */
    public void NfcValidation() {
        if(hasHostBasedCardEmulation()) {
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

    @Override
    protected void onDestroy() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true,false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true,false);
        super.onPause();
    }
}
