package nz.pbomb.xposed.superkiwi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * A UI for the xposed module that allows the users to toggle preferences and features
 *
 * @author Prashant Bhikhu (PBombNZ)
 */
public class SemblePrefActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semble);
    }



    @Override
    public void onBackPressed() {
        // Get the child preference fragment
        SemblePrefFragment preferenceFragment = (SemblePrefFragment) getFragmentManager().findFragmentById(R.id.semble_prefFragment);

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
        builder.setTitle(getResources().getString(R.string.SemblePrefActivity_quit_title));
        builder.setMessage(getResources().getString(R.string.SemblePrefActivity_quit_message));
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
}
