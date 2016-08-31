package nz.pbomb.xposed.anzmods.preferences;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

import nz.pbomb.xposed.anzmods.Common;

public class PreferenceProvider extends RemotePreferenceProvider {
    public PreferenceProvider() {
        super("nz.pbomb.xposed.anzmods.provider.preferences", new String[] { Common.getInstance().SHARED_PREFS_FILE_NAME });
    }
}
