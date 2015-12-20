package common;

public final class PREFERENCES {
    public static final String SHARED_PREFS_FILE_NAME = PACKAGES.MODULE + "_preferences";

    public static final class DEFAULT_VALUES {
        public static final class ANZ {
            public static final boolean ROOT_DETECTION = true;
            public static final boolean SPOOF_DEVICE = false;
        }

        public static final class SEMBLE {
            public static final boolean ROOT_DETECTION = true;
        }

        public static final class OTHER {
            public static final boolean ANZ_INSTALLED = true;
            public static final boolean SEMBLE_INSTALLED = true;
        }
    }

    public static final class KEYS {
        public static final class ANZ {
            public static final String ROOT_DETECTION = "anzRootDetection";
            public static final String SPOOF_DEVICE = "anzSpoofDevice";
        }

        public static final class SEMBLE {
            public static final String ROOT_DETECTION = "sembleRootDetection";
        }

        public static final class MAIN {
            public static final String ANZ = "anzPrefs";
            public static final String SEMBLE = "semblePrefs";

            public static final String HELP = "help";
            public static final String DONATE = "donate";
            public static final String CONTACT = "contact";
        }

        public static final class OTHER {
            public static final String ANZ_INSTALLED = "anzInstalled";
            public static final String SEMBLE_INSTALLED = "sembleInstalled";
        }
    }
}
