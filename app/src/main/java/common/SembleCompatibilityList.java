package common;


import android.os.Build;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Retrieves and parses the compatibility list on the Semble website to detect which devices and
 * operating systems are supported. Used to bypass OS version checking. Each device information
 * is hard coded now as it seemed there was errors in the Semble compatibility list (like typos or
 * incorrect information altogether).
 *
 * @author Prashant B. (PBombNZ)
 *compatibility list
 */
public class SembleCompatibilityList {
    /** The URL for the compatibility list on the Semble website */
    private static final String COMPATIBILITY_LIST_URI = "http://www.semble.co.nz/services/snapper";

    private static List<SembleDevice> sembleDevicesVodafone;
    private static List<SembleDevice> sembleDevices2Degrees;
    private static List<SembleDevice> sembleDevicesSpark;

    static {
        // Obsolete calls as the list generation is hard-coded
        /*
        sembleDevicesVodafone = retrieveSupportedDevicesListByCarrier("vodafone");
        sembleDevices2Degrees = retrieveSupportedDevicesListByCarrier("2dgrees");
        sembleDevicesSpark = retrieveSupportedDevicesListByCarrier("spark");
        */


        // Create Semble Compatibility List for each carrier
        /*
         * NOTE: It is hard-coded to allow for easy tweaking and updating of the lists which is
         * needed. The original run-time version generated the lists sourced from the Semble
         * website which has quite a typos and incorrect data making the data collected
         * inconsistent and therefore unusable.
         */

        // Vodafone
        sembleDevicesVodafone = new ArrayList<>();
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy Xcover 3",  "SM-G388F", "4.4.4, 5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S7", "SM-G930F", "6.0.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S7 Edge", "SM-G935F", "6.0.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S6", "SM-G920I", "5.0.2, 5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S6 Edge+", "SM-G928I", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S6 Edge", "SM-G925I", "5.0.2, 5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S5", "SM-G900F", "4.4.2, 5.0"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S5 Mini", "SM-G800Y", "4.4.2"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S5 LTE", "SM-G900I", "4.4.2, 5.0"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S4", "GT-I9505", "4.4.2, 5.0.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S4 Mini", "GT-I9195", "4.4.2"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S4 Active", "GT-I9295", "4.4.2"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S4 4G", "GT-I9506", "4.4.2, 5.0.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S3", "GT-I9300", "4.3"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy S3 4G", "GT-I9305", "4.3, 4.4.4"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy Note 5", "SM-N920I", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy Note 4", "SM-N910U", "4.4.4, 5.0.1, 5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy Note 3", "SM-N9005", "4.3, 4.4.2, 5.0"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy J5", "SM-J500Y", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy J2", "SM-J200Y", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy Grand Prime", "SM-G530MU", "4.4.4"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy Core LTE Prime", "SM-G360G", "4.4.4"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy Ace3", "GT-S7275T", "4.2.2"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy Ace3", "GT-S7275R", "4.2.2"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy A5", "SM-A500Y", "4.4.4, 5.0.2"));
        sembleDevicesVodafone.add(new SembleDevice("Samsung", "Galaxy A3", "SM-A300Y", "4.4.4, 5.0.2"));
        sembleDevicesVodafone.add(new SembleDevice("Huawei", "P8", "GRA-L09", "5.0, 5.0.1"));
        sembleDevicesVodafone.add(new SembleDevice("Huawei", "P8", "HUAWEI GRA-L09", "5.0, 5.0.1"));
        sembleDevicesVodafone.add(new SembleDevice("Sony", "Xperia Z5", "E6653", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Sony", "Xperia Z5 Compact", "E5823", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Sony", "Xperia Z3", "D6653", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Sony", "Xperia Z3 Compact", "D5833", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Sony", "Xperia Z2", "D6503", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Sony", "Xperia Z1", "C6903", "4.4.4, 5.0.2, 5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Sony", "Xperia Z", "C6603", "5.0.2, 5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("HTC", "One M9", "0PJA10", "5.0.2, 5.1"));
        sembleDevicesVodafone.add(new SembleDevice("HTC", "One M8", "HTC_0P6B", "4.4.4, 5.0.1, 6.0"));
        sembleDevicesVodafone.add(new SembleDevice("HTC", "One M7", "HTC_PN071", "4.4.2, 4.4.3, 5.0.2"));
        sembleDevicesVodafone.add(new SembleDevice("LGE", "G4", "LG-H815", "5.1, 6.0"));
        sembleDevicesVodafone.add(new SembleDevice("LGE", "G4", "LG-H815T", "5.1, 6.0"));
        sembleDevicesVodafone.add(new SembleDevice("LGE", "G3", "LG-D855", "5.0"));
        sembleDevicesVodafone.add(new SembleDevice("Vodafone", "Smart Ultra", "P839V55", "5.1.1"));
        sembleDevicesVodafone.add(new SembleDevice("Vodafone", "Smart 4 Turbo", "890N", "4.4.4"));
        sembleDevicesVodafone.add(new SembleDevice("Vodafone", "Smart 4 Turbo", "Vodafone Smart 4 turbo", "4.4.4"));
        sembleDevicesVodafone.add(new SembleDevice("Vodafone", "Prime 6", "VF-895N", "5.0.2"));
        sembleDevicesVodafone.add(new SembleDevice("Alcatel", "onetouch idol 3", "6039Y", "5.0.2"));

        // 2Degrees
        sembleDevices2Degrees = new ArrayList<>();
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy Xcover 3", "SM-G388F", "4.4.4, 5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S7", "SM-G930F", "6.0.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S7 Edge", "SM-G935F", "6.0.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S6", "SM-G920I", "5.0.2, 5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S6 Edge+", "SM-G928I", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S6 Edge", "SM-G925I", "5.0.2, 5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S5", "SM-G900F", "4.4.2, 5.0"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S5 Mini", "SM-G800Y", "4.4.2"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S5 LTE", "SM-G900I", "4.4.2, 5.0"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S4", "GT-I9505", "4.4.2, 5.0.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S4 Mini", "GT-I9195", "4.4.2"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S4 Active", "GT-I9295", "4.4.2"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S4 4G", "GT-I9506", "4.4.2, 5.0.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S3", "GT-I9300", "4.3"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy S3", "GT-I9300T", "4.3"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy Note 5", "SM-N920I", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy Note 4", "SM-N910U", "4.4.4, 5.0.1, 5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy Note 3", "SM-N9005", "4.3, 4.4.2, 5.0"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy J5", "SM-J500Y", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy J2", "SM-J200Y", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy Grand Prime", "SM-G530MU", "4.4.4"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy Core LTE Prime", "SM-G360G", "4.4.4"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy Ace3", "GT-S7275T", "4.2.2"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy Ace3", "GT-S7275R", "4.2.2"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy A5", "SM-A500Y", "4.4.4, 5.0.2"));
        sembleDevices2Degrees.add(new SembleDevice("Samsung", "Galaxy A3", "SM-A300Y", "4.4.4, 5.0.2"));
        sembleDevices2Degrees.add(new SembleDevice("Huawei", "P8", "GRA-L09", "5.0, 5.0.1"));
        sembleDevices2Degrees.add(new SembleDevice("Huawei", "P8", "HUAWEI GRA-L09", "5.0, 5.0.1"));
        sembleDevices2Degrees.add(new SembleDevice("Sony", "Xperia Z5", "E6653", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Sony", "Xperia Z5 Compact", "E5823", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Sony", "Xperia Z3", "D6653", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Sony", "Xperia Z3 Compact", "D5833", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Sony", "Xperia Z2", "D6503", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Sony", "Xperia Z1", "C6903", "4.4.4, 5.0.2, 5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Sony", "Xperia Z", "C6603", "5.0.2, 5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("HTC", "One M9", "0PJA10", "5.0.2, 5.1"));
        sembleDevices2Degrees.add(new SembleDevice("HTC", "One M8", "HTC_0P6B", "4.4.4, 5.0.1, 6.0"));
        sembleDevices2Degrees.add(new SembleDevice("HTC", "One M7", "HTC_PN071", "4.4.2, 4.4.3, 5.0.2"));
        sembleDevices2Degrees.add(new SembleDevice("LGE", "G4", "LG-H815", "5.1, 6.0"));
        sembleDevices2Degrees.add(new SembleDevice("LGE", "G4", "LG-H815T", "5.1, 6.0"));
        sembleDevices2Degrees.add(new SembleDevice("LGE", "G3", "LG-D855", "5.0"));
        sembleDevices2Degrees.add(new SembleDevice("Vodafone", "Smart Ultra", "P839V55", "5.1.1"));
        sembleDevices2Degrees.add(new SembleDevice("Vodafone", "Prime 6", "VF-895N", "5.0.2"));

        // Spark
        sembleDevicesSpark = new ArrayList<>();
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy Xcover 3", "SM-G388F", "4.4.4, 5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S7", "SM-G930F", "6.0.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S7 Edge", "SM-G935F", "6.0.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S6", "SM-G920I", "5.0.2, 5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S6 Edge+", "SM-G928I", "5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S6 Edge", "SM-G925I", "5.0.2, 5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S5", "SM-G900F", "4.4.2, 5.0"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S5 Mini", "SM-G800Y", "4.4.2"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S5 LTE", "SM-G900I", "4.4.2, 5.0"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S4", "GT-I9505", "4.4.2, 5.0.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S4 Mini", "GT-I9195", "4.4.2"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S4 Active", "GT-I9295", "4.4.2"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S4 4G", "GT-I9506", "4.4.2, 5.0.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S3", "GT-I9300", "4.3"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy S3", "GT-I9300T", "4.3"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy Note 5", "SM-N920I", "5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy Note 4", "SM-N910U", "4.4.4, 5.0.1, 5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy Note 3", "SM-N9005", "4.3, 4.4.2, 5.0"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy J5", "SM-J500Y", "5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy J2", "SM-J200Y", "5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy J1", "SM-J100Y", "4.4.4"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy Grand Prime", "SM-G530MU", "4.4.4"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy Core LTE Prime", "SM-G360G", "4.4.4"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy Ace3", "GT-S7275T", "4.2.2"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy Ace3", "GT-S7275R", "4.2.2"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy A5", "SM-A500Y", "4.4.4, 5.0.2"));
        sembleDevicesSpark.add(new SembleDevice("Samsung", "Galaxy A3", "SM-A300Y", "4.4.4, 5.0.2"));
        sembleDevicesSpark.add(new SembleDevice("Huawei", "P8", "GRA-L09", "5.0, 5.0.1"));
        sembleDevicesSpark.add(new SembleDevice("Huawei", "P8", "HUAWEI GRA-L09", "5.0, 5.0.1"));
        sembleDevicesSpark.add(new SembleDevice("Sony", "Xperia Z5", "E6653", "5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Sony", "Xperia Z5 Compact", "E5823", "5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Sony", "Xperia Z3", "D6653", "5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Sony", "Xperia Z3 Compact", "D5833", "5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Sony", "Xperia Z2", "D6503", "5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Sony", "Xperia Z1", "C6903", "4.4.4, 5.0.2, 5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("Sony", "Xperia Z", "C6603", "5.0.2, 5.1.1"));
        sembleDevicesSpark.add(new SembleDevice("HTC", "One M9", "0PJA10", "5.0.2, 5.1"));
        sembleDevicesSpark.add(new SembleDevice("HTC", "One M8", "HTC_0P6B", "4.4.4, 5.0.1, 6.0"));
        sembleDevicesSpark.add(new SembleDevice("HTC", "One M7", "HTC_PN071", "4.4.2, 4.4.3, 5.0.2"));
        sembleDevicesSpark.add(new SembleDevice("LGE", "G4", "LG-H815", "5.1, 6.0"));
        sembleDevicesSpark.add(new SembleDevice("LGE", "G4", "LG-H815T", "5.1, 6.0"));
        sembleDevicesSpark.add(new SembleDevice("LGE", "G3", "LG-D855", "5.0"));
    }

    /* Helper Methods */

    /**
     * Gets a List of {@link SembleDevice} based on which carrier-
     * variant of Semble the current device has.
     *
     * @param packageName The package name of the specified carrier-variant of Semble
     * @return A List of {@link SembleDevice}, which are
     * the supported devices and OS versions for the carrier-variant of Semble otherwise return null.
     */
    private static List<SembleDevice> getSupportedDevicesByCarrier(String packageName) {
        // Determine which package the user has, and return the correct list.
        switch(packageName) {
            case PACKAGES.SEMBLE_VODAFONE:
                return sembleDevicesVodafone;
            case PACKAGES.SEMBLE_2DEGREES:
                return sembleDevices2Degrees;
            case PACKAGES.SEMBLE_SPARK:
                return sembleDevicesSpark;
            default:
                // Dead code: here to quiet the compiler
                return null;
        }
    }

    /* Public Static Methods */

    /**
     * Gets the Supported Device matching the current device's properties, if present.
     *
     * @param packageName The package name of the specified carrier-variant of Semble
     * @return The {@link SembleDevice} related to the current
     * device, otherwise if the device is not supported, return null.
     */
    public static SembleDevice getSupportedDevice(String packageName) {
        List<SembleDevice> sembleDevicesList = getSupportedDevicesByCarrier(packageName);
        if(sembleDevicesList != null) {
            for (SembleDevice dInfo : sembleDevicesList) {
                if (dInfo.getManufacturer().equalsIgnoreCase(Build.MANUFACTURER) && dInfo.getModel().equalsIgnoreCase(Build.MODEL)) {
                    return dInfo;
                }
            }
        }
        return null;
    }

    /**
     * Determine if the current device is supported by the specified carrier-variant of Semble
     *
     * @param packageName The package name of the specified carrier-variant of Semble
     * @return true, if the device is supported, otherwise return false
     */
    public static boolean isSupportedDevice(String packageName) {
        SembleDevice dInfo = getSupportedDevice(packageName);
        return dInfo != null;
    }

    /**
     * Similar to {@link SembleCompatibilityList#isSupportedDevice(String)} but additionally checks
     * if the Android operating system version (via {@link android.os.Build.VERSION#RELEASE } is
     * compatible.
     *
     * @param packageName The package name of the specified carrier-variant of Semble
     * @return true, if the device and OS version is supported, otherwise return false
     */
    public static boolean isOSVersionSupported(String packageName) {
        SembleDevice dInfo = getSupportedDevice(packageName);
        if(dInfo != null) {
            for (int i = 0; i < dInfo.getSupportedOSVersions().size(); i++) {
                String supportedOsVersion = dInfo.getSupportedOSVersions().get(i);
                if (supportedOsVersion.equalsIgnoreCase(Build.VERSION.RELEASE)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private static List<SembleDevice> retrieveSupportedDevicesListByCarrier(String carrier) {
        List<SembleDevice> sembleDevices = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(COMPATIBILITY_LIST_URI).get();

            Elements divContainer = doc.select("div.snapper-hidden[data-expand=" + carrier + "]");
            Elements tables = divContainer.select("table.phone-table");
            for (Element table : tables) {
                Elements trs = table.select("tr");

                for (int i = 0; i < trs.size(); i++) {
                    Elements firstCol = trs.get(i).select("td.large-col");  // Phone Info (Manufacturer, Model)
                    Elements secondCol = trs.get(i).select("td.small-col");  // Supported Android Versions

                    Pattern pattern = Pattern.compile("([a-zA-Z]+)\\s(.+)\\s\\[(.+)\\]");
                    Matcher matcher = pattern.matcher(firstCol.text());

                    if (matcher.find()) {
                        String manufacturer = matcher.group(1);
                        String modelStr = matcher.group(2);
                        String model = matcher.group(3);

                        sembleDevices.add(new SembleDevice(manufacturer, modelStr, model, secondCol.text()));
                    }
                }
            }
        }
        catch (IOException e) {
            Log.e("SuperKiwi", e.getMessage());
        }
        return sembleDevices;
    }

    /**
     * The device information of each supported device is stored in a this object where
     * the device information can be easily accessible and retrievable.
     */
    public static class SembleDevice {
        private final String manufacturer;
        private final String modelStr;
        private final String model;
        private final List<String> supportedOSVersions;
        private final String supportedOSVersionsStr;

        public SembleDevice(String manufacturer, String modelStr, String model, String supportedOSVersions) {
            this.manufacturer = manufacturer;
            this.modelStr = modelStr;
            this.model = model;
            this.supportedOSVersions = Arrays.asList(supportedOSVersions.split(", "));
            this.supportedOSVersionsStr = supportedOSVersions;
        }

        /**
         * @return A string which contains the manufacturer name of the device (ie. For the
         * International Samsung Galaxy S3, it would return "Samsung").
         */
        public String getManufacturer() {
            return manufacturer;
        }

        /**
         * @return A string which contains the Consumer model name of the device (ie. For the
         * International Samsung Galaxy S3, it would return "Galaxy S3").
         */
        @SuppressWarnings("unused")
        public String getModelStr() {
            return modelStr;
        }

        /**
         * @return A string which contains the model number name of the device (ie. For the
         * International Samsung Galaxy S3, it would return "GT-I9300").
         */
        public String getModel() {
            return model;
        }

        /**
         * @return A list of Strings, each String containing an Android OS version number that is
         * officially supported by Semble
         */
        public List<String> getSupportedOSVersions() {
            return supportedOSVersions;
        }

        @Override
        public String toString() {
            return "SembleDevice [manufacturer=" + manufacturer + ", modelStr=" + modelStr + ", model=" + model
                    + ", supportedOSVersions.size()=" + supportedOSVersions.size() + ", supportedOSVersionsStr="
                    + supportedOSVersionsStr + "]";
        }

        /**
         * Executed on PC to generate Semble Compatibility list (more info in {@link #main(String[])} main method.
         * This method makes a code-like string allowing to copy-paste into the Application code and make
         * adjustments if need to be.
         */
        public String toNewConstructorString() {
            return "new SembleDevice(\"" + manufacturer + "\", \"" + modelStr + "\", \"" + model
                    + "\", \""+ supportedOSVersionsStr + "\")";
        }

    }



    /**
     * Executed on PC to generate Semble Compatibility list. It is not executed on runtime
     * execution within the application as some device information collected is wrong hence
     * why generate a hard coded version and modify any incorrect device information after.
     *
     */
    public static void main(String[] args) {
        for(SembleDevice sDevice : sembleDevicesSpark) {
            System.out.println("sembleDevicesSpark.add(" + sDevice.toNewConstructorString() + ");");
        }
    }

}
