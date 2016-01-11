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
 * operating systems are supported. Used to bypass OS version checking.
 *
 * @author Prashant B. (PBombNZ)
 *
 */
public class SupportedDevicesSemble {
    /** The URL for the compatibility list on the Semble website */
    private static final String COMPATIBILITY_LIST_URI = "http://www.semble.co.nz/services/snapper";

    public static List<SupportedDevice> supportedDevicesVodafone;
    public static List<SupportedDevice> supportedDevices2Degrees;
    public static List<SupportedDevice> supportedDevicesSpark;

    static {
        supportedDevicesVodafone = retrieveSupportedDevicesListByCarrier("vodafone");
        supportedDevices2Degrees = retrieveSupportedDevicesListByCarrier("2degrees");
        supportedDevicesSpark = retrieveSupportedDevicesListByCarrier("spark");
    }

    /* Helper Methods */

    /**
     * Gets a List of {@link common.SupportedDevicesSemble.SupportedDevice} based on which carrier-
     * variant of Semble the current device has.
     *
     * @param packageName The package name of the specified carrier-variant of Semble
     * @return A List of {@link common.SupportedDevicesSemble.SupportedDevice}, which are
     * the supported devices and OS versions for the carrier-variant of Semble otherwise return null.
     */
    private static List<SupportedDevice> getSupportedDevicesByCarrier(String packageName) {
        // Determine which package the user has, and return the correct list.
        switch(packageName) {
            case PACKAGES.SEMBLE_VODAFONE:
                return supportedDevicesVodafone;
            case PACKAGES.SEMBLE_2DEGREES:
                return supportedDevices2Degrees;
            case PACKAGES.SEMBLE_SPARK:
                return supportedDevicesSpark;
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
     * @return The {@link common.SupportedDevicesSemble.SupportedDevice} related to the current
     * device, otherwise if the device is not supported, return null.
     */
    public static SupportedDevice getSupportedDevice(String packageName) {
        List<SupportedDevice> supportedDevicesList = getSupportedDevicesByCarrier(packageName);
        if(supportedDevicesList != null) {
            for (SupportedDevice dInfo : supportedDevicesList) {
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
        SupportedDevice dInfo = getSupportedDevice(packageName);
        return dInfo != null;
    }

    /**
     * Similar to {@link SupportedDevicesSemble#isSupportedDevice(String)} but additionally checks
     * if the Android operating system version (via {@link android.os.Build.VERSION#RELEASE } is
     * compatible.
     *
     * @param packageName The package name of the specified carrier-variant of Semble
     * @return true, if the device and OS version is supported, otherwise return false
     */
    public static boolean isOSVersionSupported(String packageName) {
        SupportedDevice dInfo = getSupportedDevice(packageName);
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

    private static List<SupportedDevice> retrieveSupportedDevicesListByCarrier(String carrier) {
        List<SupportedDevice> supportedDevices = new ArrayList<>();
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

                        supportedDevices.add(new SupportedDevice(manufacturer, modelStr, model, secondCol.text()));
                    }
                }
            }
        }
        catch (IOException e) {
            Log.e("SuperKiwi", e.getMessage());
        }
        return supportedDevices;
    }

    /**
     * The device information of each supported device is stored in a SupportedDevice object where
     * the device information can be easily accessible and retrievable.
     */
    public static class SupportedDevice {
        private final String manufacturer;
        private final String modelStr;
        private final String model;
        private final List<String> supportedOSVersions;
        private final String supportedOSVersionsStr;

        public SupportedDevice(String manufacturer, String modelStr, String model, String supportedOSVersions) {
            this.manufacturer = manufacturer;
            this.modelStr = modelStr;
            this.model = model;
            this.supportedOSVersions = Arrays.asList(supportedOSVersions.split(", "));
            this.supportedOSVersionsStr = supportedOSVersions;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public String getModelStr() {
            return modelStr;
        }

        public String getModel() {
            return model;
        }

        public List<String> getSupportedOSVersions() {
            return supportedOSVersions;
        }

        @Override
        public String toString() {
            return "SupportedDevice [manufacturer=" + manufacturer + ", modelStr=" + modelStr + ", model=" + model
                    + ", supportedOSVersions.size()=" + supportedOSVersions.size() + ", supportedOSVersionsStr="
                    + supportedOSVersionsStr + "]";
        }
    }
}
