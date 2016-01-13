package nz.pbomb.xposed.anzmods;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;

import java.lang.reflect.Method;
import java.util.Map;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import common.GLOBAL;
import common.PREFERENCES;

import common.SupportedDevicesSemble;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;

import common.PACKAGES;


public class SuperKiwiHooker implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = "SuperKiwi::SuperKiwiHooker";

    private static XSharedPreferences prefs;

    private static void logging(String message) {
        if (GLOBAL.DEBUG) {
            XposedBridge.log("[" + TAG + "] " + message);
        }
    }

    private static void refreshSharedPreferences() {
        prefs = new XSharedPreferences(PACKAGES.MODULE);
        prefs.makeWorldReadable();
        prefs.reload();
        logging("Shared Preferences Properties:");
        logging("\tWorld Readable: " + prefs.makeWorldReadable());
        logging("\tPath: " + prefs.getFile().getAbsolutePath());
        logging("\tFile Readable: " + prefs.getFile().canRead());
        logging("\tExists: " + prefs.getFile().exists());
        if (prefs.getAll().size() == 0) {
            logging("Shared Preferences seems not to be initialized or does not have read permissions. Common on Android 5.0+ with SELinux Enabled and Enforcing.");
            logging("Loaded Shared Preferences Defaults Instead.");
        } else {
            logging("");
            logging("Loaded Shared Preferences:");
            Map<String, ?> prefsMap = prefs.getAll();
            for(String key: prefsMap.keySet()) {
                String val = prefsMap.get(key).toString();
                logging("\t " + key + ": " + val);
            }
        }
    }


    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        logging("Module Loaded (Debug Mode: " + (GLOBAL.DEBUG ? "ON" : "OFF") + ")");
        refreshSharedPreferences();
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {
        if(!(loadPackageParam.packageName.equals(PACKAGES.ANZ_GOMONEY) ||
                loadPackageParam.packageName.equals(PACKAGES.SEMBLE_2DEGREES) ||
                loadPackageParam.packageName.equals(PACKAGES.SEMBLE_SPARK) ||
                loadPackageParam.packageName.equals(PACKAGES.SEMBLE_VODAFONE) ||
                loadPackageParam.packageName.equals(PACKAGES.TVNZ_ONDEMAND)
        )) {
            return;
        }

        if(loadPackageParam.packageName.equals(PACKAGES.ANZ_GOMONEY)) {
            logging("Hooking Methods for ANZ GoMoney New Zealand Application.");
            hookAnzGoMoneyApplication(loadPackageParam);
        }

        if(loadPackageParam.packageName.equals(PACKAGES.SEMBLE_2DEGREES) ||
           loadPackageParam.packageName.equals(PACKAGES.SEMBLE_SPARK) ||
           loadPackageParam.packageName.equals(PACKAGES.SEMBLE_VODAFONE)) {
            logging("Hooking Methods for Semble Application.");
            hookSembleApplication(loadPackageParam);
        }

        if(loadPackageParam.packageName.equals(PACKAGES.TVNZ_ONDEMAND)) {
            logging("Hooking Methods for TVNZ onDemand Application.");
            hookTVNZOnDemandApplication(loadPackageParam);
        }
    }

    private void hookTVNZOnDemandApplication(final LoadPackageParam loadPackageParam) {
        findAndHookMethod("nz.co.tvnz.ondemand.OnDemandApp", loadPackageParam.classLoader, "A", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.TVNZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TVNZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
    }

    public void hookAnzGoMoneyApplication(final LoadPackageParam loadPackageParam) {
        /**
         * Seitc API Root Check Hooks
         */
        findAndHookMethod("xxxxxx.jejeee", loadPackageParam.classLoader, "isRooted", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });


        findAndHookMethod("xxxxxx.jejeee", loadPackageParam.classLoader, "isRootedQuickCheck", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        findAndHookMethod("xxxxxx.jejeee", loadPackageParam.classLoader, "isDebug", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });



        /**
         * ANZ GoMoney Root Check
         */
        // SharedPrefs - "wallet_invalid_rooted_device" Always returns false
        findAndHookMethod("nz.co.anz.android.mobilebanking.h.a.n", loadPackageParam.classLoader, "l", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        // SharedPrefs - "wallet_card_removed" Always returns false
        findAndHookMethod("nz.co.anz.android.mobilebanking.h.a.n", loadPackageParam.classLoader, "m", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        // Superuser.apk and shell check
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.ah", loadPackageParam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        // eligibleForWallet returns true
        findAndHookMethod("nz.co.anz.android.mobilebanking.model.k", loadPackageParam.classLoader, "g", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(true);
                }
            }
        });

        /**
         * Device Spoofing Hooks
         */
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", loadPackageParam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult("[samsung SM-N9005]");
                }
            }
        });
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", loadPackageParam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult("samsung SM-N9005");
                }
            }
        });

        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", loadPackageParam.classLoader, "e", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult("4.4.2");
                }
            }
        });

        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.u", loadPackageParam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult("SM-N9005");
                }
            }
        });

        findAndHookMethod("com.google.android.gms.b.ao", loadPackageParam.classLoader, "a", String.class, String.class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    String str = (String) param.args[0];
                    String str2 = (String) param.args[1];
                    String str3 = (String) param.args[2];
                    String str4 = (String) param.args[3];
                    String str5 = "SM-9005";
                    String str6 = (String) param.args[5];

                    param.setResult(String.format("%s/%s (Linux; U; Android %s; %s; %s Build/%s)", str, str2, str3, str4, str5, str6));
                }
            }
        });

        Class<?> x = findClass("xxxxxx.ajaaaj", loadPackageParam.classLoader);

        Method y = findMethodBestMatch(x,"b041604160416Ж0416ЖЖ0416", String.class);
        XposedBridge.log(y.getName());
        //xxxxxx.ajaaaj
        //Build.Device
        findAndHookMethod("xxxxxx.ajaaaj", loadPackageParam.classLoader, "b041604160416Ж0416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    setObjectField(param.thisObject, "f6987b044504450445", "hlte");
                }
            }
        });
        //Build.Model
        findAndHookMethod("xxxxxx.ajaaaj", loadPackageParam.classLoader, "b0416ЖЖ04160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    setObjectField(param.thisObject, "bх0445ххх04450445х", "SM-N9005");
                }
            }
        });
        //Build.Product
        findAndHookMethod("xxxxxx.ajaaaj", loadPackageParam.classLoader, "b04160416Ж04160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    setObjectField(param.thisObject, "b04450445ххх04450445х", "hltexx");
                }
            }
        });

        /*//Build.BOARD
        findAndHookMethod("xxxxxx.ajaaaj", loadPackageParam.classLoader, "b04160416041604160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(prefs.getBoolean(PREFERENCES.KEYS.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SPOOF_DEVICE)) {
                    setObjectField(param.thisObject, "b0445х0445хх04450445х", "MSM8974");
                }
            }
        });
        //Build.CPU_ABI ~ WRONG
        findAndHookMethod("xxxxxx.ajaaaj", loadPackageParam.classLoader, "b0416ЖЖЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(prefs.getBoolean(PREFERENCES.KEYS.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SPOOF_DEVICE)) {
                    setObjectField(param.thisObject, "bх04450445хх04450445х", "armeabi-v7a");
                }
            }
        });
        //Build.CPU_ABI2 ~ WRONG
        findAndHookMethod("xxxxxx.ajaaaj", loadPackageParam.classLoader, "m12118b041604160416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(prefs.getBoolean(PREFERENCES.KEYS.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SPOOF_DEVICE)) {
                    setObjectField(param.thisObject, "bх04450445хх04450445х", "armeabi");
                }
            }
        });*/

        //Build.MANUFACTURER
        findAndHookMethod("xxxxxx.ajaaaj", loadPackageParam.classLoader, "b04160416ЖЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    setObjectField(param.thisObject, "b044504450445хх04450445х", "samsung");
                }
            }
        });

        //Build.BRAND
        findAndHookMethod("xxxxxx.ajaaaj", loadPackageParam.classLoader, "b041604160416ЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                }
            }
        });

        //Build.ID
        findAndHookMethod("xxxxxx.hchchh", loadPackageParam.classLoader, "b04220422ТТ0422042204220422", android.content.Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    StringBuilder sb = new StringBuilder(500);
                    sb.append("hlte");
                    sb.append("SM-N9005");
                    sb.append("hltexx");
                    sb.append("MSM8974");
                    sb.append("samsung");
                    sb.append("samsung");
                    sb.append("Unknown"); //Hardware
                    sb.append("Unknown"); //serial

                    final TelephonyManager mTelephony = (TelephonyManager) ((Context) param.args[0]).getSystemService(Context.TELEPHONY_SERVICE);
                    String myAndroidDeviceId = mTelephony.getDeviceId();

                    sb.append(myAndroidDeviceId);

                    param.setResult(sb.toString().getBytes());
                }
            }
        });

        //Build.FINGERPRINT
        findAndHookMethod("xxxxxx.ajaaaj", loadPackageParam.classLoader, "bЖ04160416ЖЖЖ04160416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                Log.e("SuperKiwi", "afterHookedMethod: " + String.valueOf(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)));
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    setObjectField(param.thisObject, "b0445ххх044504450445х", "samsung/hltexx/hlte:4.4.2/KOT49H/N9005XXUGNG1:user/release-keys");
                }
            }
        });


        // Debug settings fragment view
        findAndHookMethod("nz.co.anz.android.mobilebanking.ui.fragment.SettingsFragment", loadPackageParam.classLoader, "addTermsAndConditions", LayoutInflater.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Class<?> settingGroupEnumClass = findClass("nz.co.anz.android.mobilebanking.ui.fragment.SettingsFragment.SettingsGroup", loadPackageParam.classLoader);
                Object[] consts = settingGroupEnumClass.getEnumConstants();

                Class settingEnumClass = findClass("nz.co.anz.android.mobilebanking.ui.c.a", loadPackageParam.classLoader);
                Object[] consts2 = settingEnumClass.getEnumConstants();

                callMethod(param.thisObject, "insertItem", new Class<?>[] { LayoutInflater.class, java.lang.String.class, int.class, settingEnumClass, settingGroupEnumClass, boolean.class }, param.args[0], "Device Info (Injected by SuperKiwi)",2130838121, consts2[45],consts[2] , false);
            }
        });
    }

    private void hookSembleApplication(final LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.csam.wallet.integrity.IntegrityCheckerImpl", loadPackageParam.classLoader, "checkDeviceIntegrity", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            refreshSharedPreferences();
            if(prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION)) {
                param.setResult(true);
            }
            }
        });

        findAndHookMethod("com.mastercard.mtp.mobileclientutilities.DeviceUtility", loadPackageParam.classLoader, "isOSPossiblyCompromised", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        findAndHookMethod("com.csam.mclient.core.WalletContext", loadPackageParam.classLoader, "getSystemOSVersion", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.MM_SUPPORT, PREFERENCES.DEFAULT_VALUES.SEMBLE.MM_SUPPORT)) {
                    logging("SupportedDevicesSemble.isSupportedDevice(): " + SupportedDevicesSemble.isSupportedDevice(loadPackageParam.packageName));
                    logging("SupportedDevicesSemble.isOSVersionSupported(): " + SupportedDevicesSemble.isOSVersionSupported(loadPackageParam.packageName));
                    if (SupportedDevicesSemble.isSupportedDevice(loadPackageParam.packageName)
                            && !SupportedDevicesSemble.isOSVersionSupported(loadPackageParam.packageName)) {
                        SupportedDevicesSemble.SupportedDevice dInfo = SupportedDevicesSemble.getSupportedDevice(loadPackageParam.packageName);
                        if (dInfo != null) {
                            param.setResult(dInfo.getSupportedOSVersions().get(dInfo.getSupportedOSVersions().size() - 1));
                        }
                    }
                    //param.setResult("6.0.1");
                }
            }
        });
    }
}
