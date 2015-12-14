package common;

public final class SETTINGS {
    public static final String SHARED_PREFS_FILE_NAME = PACKAGES.MODS + "_preferences";

    public static final class DEFAULT_VALUES {
        public static final boolean ROOT_DETECTION = true;
        public static final boolean SPOOF_DEVICE = false;
    }

    public static final class KEYS {
        public static final String ROOT_DETECTION = "rootDetection";
        public static final String SPOOF_DEVICE = "spoofDevice";

        public static final String HELP = "help";
        public static final String DONATE = "donate";
        public static final String CONTACT = "contact";
    }
}
