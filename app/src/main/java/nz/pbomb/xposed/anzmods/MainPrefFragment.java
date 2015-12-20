package nz.pbomb.xposed.anzmods;

import android.content.Intent;
import android.net.Uri;
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

        findPreference(PREFERENCES.KEYS.MAIN.HELP).setOnPreferenceClickListener(this);
        findPreference(PREFERENCES.KEYS.MAIN.DONATE).setOnPreferenceClickListener(this);
        findPreference(PREFERENCES.KEYS.MAIN.CONTACT).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String prefKey = preference.getKey();
        Intent intent = null;

        if(prefKey.equals(PREFERENCES.KEYS.MAIN.ANZ)) {
            intent = new Intent(getActivity().getApplicationContext(), ANZPrefActivity.class);
        } else if(prefKey.equals(PREFERENCES.KEYS.MAIN.SEMBLE)) {
            intent = new Intent(getActivity().getApplicationContext(), SemblePrefActivity.class);
        } else if(prefKey.equals(PREFERENCES.KEYS.MAIN.HELP)) {
            intent = new Intent(getActivity().getApplicationContext(), HelpActivity.class);
        } else if(prefKey.equals(PREFERENCES.KEYS.MAIN.DONATE)) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=QNQDESEMGWDPY"));
        } else if(prefKey.equals(PREFERENCES.KEYS.MAIN.CONTACT)) {
            intent = new Intent(getActivity().getApplicationContext(), ContactActivity.class);
        }
        startActivity(intent);
        return true;
    }
}
