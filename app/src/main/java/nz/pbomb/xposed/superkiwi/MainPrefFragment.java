package nz.pbomb.xposed.superkiwi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import common.SETTINGS;


public class MainPrefFragment extends PreferenceFragment implements OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_main);

        findPreference(SETTINGS.KEYS.MAIN.ANZ).setOnPreferenceClickListener(this);
        findPreference(SETTINGS.KEYS.MAIN.SEMBLE).setOnPreferenceClickListener(this);

        findPreference(SETTINGS.KEYS.MAIN.DONATE).setOnPreferenceClickListener(this);
        findPreference(SETTINGS.KEYS.MAIN.CONTACT).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String prefKey = preference.getKey();
        Intent intent = null;

        if(prefKey.equals(SETTINGS.KEYS.MAIN.ANZ)) {
            intent = new Intent(getActivity().getApplicationContext(), ANZPrefActivity.class);
        } else if(prefKey.equals(SETTINGS.KEYS.MAIN.SEMBLE)) {
            intent = new Intent(getActivity().getApplicationContext(), SemblePrefActivity.class);
        }else if(prefKey.equals(SETTINGS.KEYS.MAIN.DONATE)) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=QNQDESEMGWDPY"));
        } else if(prefKey.equals(SETTINGS.KEYS.MAIN.CONTACT)) {
            intent = new Intent(getActivity().getApplicationContext(), ContactActivity.class);
        }
        startActivity(intent);
        return true;
    }
}
