package nz.pbomb.xposed.anzmods;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.util.Map;

import common.PACKAGES;
import common.PREFERENCES;

/**
 * A UI for the xposed module that allows the users to toggle preferences and features
 *
 * @author Prashant Bhikhu (PBombNZ)
 */
public class TVNZPrefActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvnz);
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

    public static class TVNZPrefFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_tvnz);

            getPreferenceManager().findPreference(PREFERENCES.KEYS.TVNZ.ROOT_DETECTION).setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Toast.makeText(getActivity().getApplicationContext(), "Always active. Cannot be disabled.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
