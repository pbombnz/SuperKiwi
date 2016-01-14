package common;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class SpoofDevices {
    private static List<DeviceInfo> devices;

    static {
        devices = new ArrayList<>();

        DeviceInfo note3 = new DeviceInfo(
                "MSM8974",
                "N9005XXUGNG1",
                "4.4.2",
                "19",
                19,
                "REL",
                "KOT49H",
                "KOT49H.N9005XXUGNG1",
                "hltexx",
                "hlte",
                "armeabi-v7a",
                "armeabi",
                "samsung",
                "samsung",
                "SM-N9005",
                "N9005XXUGNG1",
                "N9005XXUGNG1",
                "qcom",
                Build.UNKNOWN,
                new String[] { "armeabi-v7a", "armeabi" },
                new String[] { "armeabi-v7a", "armeabi" },
                new String[0],
                "user",
                "release-keys",
                "samsung/hltexx/hlte:4.4.2/KOT49H/N9005XXUGNG1:user/release-keys");

        devices.add(note3);
    }

    public static DeviceInfo getDeviceInfo(String manufacturer, String brand, String model) {
        for(DeviceInfo dInfo: devices) {
            if(dInfo.Build.MANUFACTURER.equals(manufacturer)
                || dInfo.Build.BRAND.equals(brand)
                || dInfo.Build.MODEL.equals(model)) {
                return dInfo;
            }
        }
        return null;
    }
}
