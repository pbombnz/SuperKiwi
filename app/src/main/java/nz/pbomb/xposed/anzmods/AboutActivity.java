package nz.pbomb.xposed.anzmods;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

import common.PACKAGES;
import common.PREFERENCES;

/**
 * A UI for the xposed module that allows the users to toggle preferences and features
 *
 * @author Prashant Bhikhu (PBombNZ)
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        new File("/data/data/" + PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true, false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        new File("/data/data/" + PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true, false);
        super.onPause();
    }
}
