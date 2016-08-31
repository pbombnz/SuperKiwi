package nz.pbomb.xposed.anzmods;

/**
 * Common Singleton Class to hold global variables.
 */
public class Common {
    private static Common ourInstance = new Common();

    public static Common getInstance() {
        return ourInstance;
    }

    private Common() {
    }

    public final String PACKAGE_APP = "nz.pbomb.xposed.anzmods";
    public final String PACKAGE_ASB_MOBILE = "nz.co.asb.asbmobile";
    public final String PACKAGE_ANZ_GOMONEY = "nz.co.anz.android.mobilebanking";
    public final String PACKAGE_SEMBLE_SPARK = "com.sparknz.semble";
    public final String PACKAGE_SEMBLE_2DEGREES = "com.twodegreesmobile.semble";
    public final String PACKAGE_SEMBLE_VODAFONE = "nz.co.vodafone.android.semble";
    public final String PACKAGE_TVNZ_ONDEMAND = "nz.co.tvnz.ondemand.phone.android";
    public final String PACKAGE_TV3NOW = "com.mediaworks.android";

    public final String SHARED_PREFS_FILE_NAME = PACKAGE_APP + "_preferences";
}

