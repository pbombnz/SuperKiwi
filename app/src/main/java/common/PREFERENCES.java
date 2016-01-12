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
            public static final boolean MM_SUPPORT = true;
        }

        public static final class TVNZ {
            public static final boolean ROOT_DETECTION = true;
        }
    }

    public static final class KEYS {
        public static final class ANZ {
            public static final String ROOT_DETECTION = "anzRootDetection";
            public static final String SPOOF_DEVICE = "anzSpoofDevice";
        }

        public static final class SEMBLE {
            public static final String ROOT_DETECTION = "sembleRootDetection";
            public static final String MM_SUPPORT = "sembleOSSupport";
        }

        public static final class TVNZ {
            public static final String ROOT_DETECTION = "tvnzRootDetection";
        }

        public static final class MAIN {
            public static final String ANZ = "anzPrefs";
            public static final String SEMBLE = "semblePrefs";
            public static final String TVNZ = "tvnzPrefs";

            public static final String HELP = "help";
        }
    }
}
