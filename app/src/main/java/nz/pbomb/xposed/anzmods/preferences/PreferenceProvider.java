package nz.pbomb.xposed.anzmods.preferences;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class PreferenceProvider extends RemotePreferenceProvider {
    public PreferenceProvider() {
        super("nz.pbomb.xposed.anzmods.provider.preferences", new String[] { PREFERENCES.SHARED_PREFS_FILE_NAME });
    }
}
