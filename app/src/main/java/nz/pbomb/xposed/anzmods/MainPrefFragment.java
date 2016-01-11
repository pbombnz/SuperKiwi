package nz.pbomb.xposed.anzmods;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import common.PREFERENCES;


public class MainPrefFragment extends PreferenceFragment implements OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_main);

        findPreference(PREFERENCES.KEYS.MAIN.ANZ).setOnPreferenceClickListener(this);
        findPreference(PREFERENCES.KEYS.MAIN.SEMBLE).setOnPreferenceClickListener(this);
        findPreference(PREFERENCES.KEYS.MAIN.TVNZ).setOnPreferenceClickListener(this);

        findPreference(PREFERENCES.KEYS.MAIN.HELP).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String prefKey = preference.getKey();
        Intent intent = null;

        if(prefKey.equals(PREFERENCES.KEYS.MAIN.ANZ)) {
            intent = new Intent(getActivity().getApplicationContext(), ANZPrefActivity.class);
        } else if(prefKey.equals(PREFERENCES.KEYS.MAIN.SEMBLE)) {
            intent = new Intent(getActivity().getApplicationContext(), SemblePrefActivity.class);
        } else if(prefKey.equals(PREFERENCES.KEYS.MAIN.TVNZ)) {
            intent = new Intent(getActivity().getApplicationContext(), TVNZPrefActivity.class);
        } else if(prefKey.equals(PREFERENCES.KEYS.MAIN.HELP)) {
            intent = new Intent(getActivity().getApplicationContext(), HelpActivity.class);
        }
        startActivity(intent);
        return true;
    }
}
