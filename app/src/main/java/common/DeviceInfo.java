package common;


public class DeviceInfo {

    public VERSION VERSION;
    public Build Build;

    public DeviceInfo(String BOARD, String INCREMENTAL, String RELEASE, String SDK, int SDK_INT, String CODENAME, String ID, String DISPLAY, String PRODUCT, String DEVICE, String CPU_ABI, String CPU_ABI2, String MANUFACTURER, String BRAND, String MODEL, String BOOTLOADER, String RADIO, String HARDWARE, String SERIAL, String[] SUPPORTED_ABIS, String[] SUPPORTED_32_BIT_ABIS, String[] SUPPORTED_64_BIT_ABIS, String TYPE, String TAGS, String FINGERPRINT) {
        this.Build = new Build(BOARD,ID,DISPLAY,PRODUCT,DEVICE,CPU_ABI,CPU_ABI2,MANUFACTURER,BRAND,MODEL,BOOTLOADER,RADIO,HARDWARE,SERIAL,SUPPORTED_ABIS,SUPPORTED_32_BIT_ABIS,SUPPORTED_64_BIT_ABIS,TYPE,TAGS,FINGERPRINT);
        this.VERSION = new VERSION(CODENAME,INCREMENTAL,RELEASE,SDK,SDK_INT);
    }

    public static class VERSION {
        public final String INCREMENTAL;
        public final String RELEASE;
        public final String SDK;
        public final int SDK_INT;
        public final String CODENAME;

        public VERSION(String CODENAME, String INCREMENTAL, String RELEASE, String SDK, int SDK_INT) {
            this.CODENAME = CODENAME;
            this.INCREMENTAL = INCREMENTAL;
            this.RELEASE = RELEASE;
            this.SDK = SDK;
            this.SDK_INT = SDK_INT;
        }
    }
    public static class Build {
        public final String ID;
        public final String DISPLAY;
        public final String PRODUCT;
        public final String DEVICE;
        public final String BOARD;
        public final String CPU_ABI;
        public final String CPU_ABI2;

        public final String MANUFACTURER;
        public final String BRAND;
        public final String MODEL;
        public final String BOOTLOADER;

        public final String RADIO;
        public final String HARDWARE;
        public final String SERIAL;

        public final String[] SUPPORTED_ABIS;
        public final String[] SUPPORTED_32_BIT_ABIS;
        public final String[] SUPPORTED_64_BIT_ABIS;

        public final String TYPE;
        public final String TAGS;
        public final String FINGERPRINT;

        public Build(String BOARD, String ID, String DISPLAY, String PRODUCT, String DEVICE, String CPU_ABI, String CPU_ABI2, String MANUFACTURER, String BRAND, String MODEL, String BOOTLOADER, String RADIO, String HARDWARE, String SERIAL, String[] SUPPORTED_ABIS, String[] SUPPORTED_32_BIT_ABIS, String[] SUPPORTED_64_BIT_ABIS, String TYPE, String TAGS, String FINGERPRINT) {
            this.BOARD = BOARD;
            this.ID = ID;
            this.DISPLAY = DISPLAY;
            this.PRODUCT = PRODUCT;
            this.DEVICE = DEVICE;
            this.CPU_ABI = CPU_ABI;
            this.CPU_ABI2 = CPU_ABI2;
            this.MANUFACTURER = MANUFACTURER;
            this.BRAND = BRAND;
            this.MODEL = MODEL;
            this.BOOTLOADER = BOOTLOADER;
            this.RADIO = RADIO;
            this.HARDWARE = HARDWARE;
            this.SERIAL = SERIAL;
            this.SUPPORTED_ABIS = SUPPORTED_ABIS;
            this.SUPPORTED_32_BIT_ABIS = SUPPORTED_32_BIT_ABIS;
            this.SUPPORTED_64_BIT_ABIS = SUPPORTED_64_BIT_ABIS;
            this.TYPE = TYPE;
            this.TAGS = TAGS;
            this.FINGERPRINT = FINGERPRINT;
        }
    }
}
