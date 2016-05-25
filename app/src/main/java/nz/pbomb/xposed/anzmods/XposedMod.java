package nz.pbomb.xposed.anzmods;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import common.DeviceInfo;
import common.GLOBAL;
import common.PREFERENCES;

import common.SembleCompatibilityList;
import common.SpoofDevices;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;

import common.PACKAGES;


public class XposedMod implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = "SuperKiwi::Mod"; // Tag used for debugLog

    private static XSharedPreferences prefs;

    /**
     * Displays a message in Xposed Logs and logcat if and only if Debug Mode is enabled.
     *
     * @param message The message to be displayed in logs
     */
    public static void debugLog(String message) {
        if (isDebugMode()) {
           log(message);
        }
    }

    /**
     * Displays a message in Xposed Logs and logcat.
     *
     * @param message The message to be displayed in logs
     */
    public static void log(String message) {
        XposedBridge.log("[" + TAG + "] " + message);
    }

    /**
     * Indicates whether Debug Mode is enabled (either hard-coded or toggled by end-user) and will
     * display them in logcat as well as the Xposed log file.
     *
     * @return Returns true if Debug Mode is enabled, otherwise return false.
     */
    public static boolean isDebugMode() {
        if (GLOBAL.DEBUG) {
            return true;
        }
        if (prefs == null) {
            refreshSharedPreferences(false);
        }
        return prefs.getBoolean(PREFERENCES.KEYS.MAIN.DEBUG, PREFERENCES.DEFAULT_VALUES.MAIN.DEBUG);
    }

    public static void refreshSharedPreferences() {
        boolean displayLogs = isDebugMode();
        refreshSharedPreferences(displayLogs);
    }

    /**
     * Reloads and refreshes the package's shared preferences file to reload new confirgurations
     * that may have changed on runtime.
     *
     * @param displayLogs To show logs or not.
     */
    public static void refreshSharedPreferences(boolean displayLogs) {
        prefs = new XSharedPreferences(PACKAGES.MODULE);
        prefs.makeWorldReadable();
        prefs.reload();

        // Only continue if we want to produce logging
        if(!displayLogs) {
            return;
        }

        // Logging the properties to see if the file is actually readable
        log("Shared Preferences Properties:");
        log("\tWorld Readable: " + prefs.makeWorldReadable());
        log("\tPath: " + prefs.getFile().getAbsolutePath());
        log("\tFile Readable: " + prefs.getFile().canRead());
        log("\tExists: " + prefs.getFile().exists());

        // Display the preferences loaded, only if the file was readable otherwise display an error
        if (prefs.getAll().size() == 0) {
            log("Shared Preferences seems not to be initialized or does not have read permissions. Common on heavy ROMs with SELinux enforcing.");
            log("Loaded Shared Preferences Defaults Instead.");
        } else {
            log("");
            log("Loaded Shared Preferences:");
            Map<String, ?> prefsMap = prefs.getAll();
            for(String key: prefsMap.keySet()) {
                String val = prefsMap.get(key).toString();
                log("\t " + key + ": " + val);
            }
        }
    }


    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        refreshSharedPreferences(false);
        XposedBridge.log("Module Loaded (Debug Mode: " + (isDebugMode() ? "ON" : "OFF") + ")");
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Don't handle package param unless its ANZ, Semble, TVNZ OnDemand or 3NOW being loaded
        if(!(lpparam.packageName.equals(PACKAGES.ANZ_GOMONEY) ||
                lpparam.packageName.equals(PACKAGES.SEMBLE_2DEGREES) ||
                lpparam.packageName.equals(PACKAGES.SEMBLE_SPARK) ||
                lpparam.packageName.equals(PACKAGES.SEMBLE_VODAFONE) ||
                lpparam.packageName.equals(PACKAGES.TVNZ_ONDEMAND) ||
                lpparam.packageName.equals(PACKAGES.TV3NOW)
        )) {
            return;
        }

        if(lpparam.packageName.equals(PACKAGES.ANZ_GOMONEY)) {
            debugLog("Hooking Methods for ANZ goMoney NZ Application.");
            hookAnzGoMoneyApplication(lpparam);
        }

        if(lpparam.packageName.equals(PACKAGES.SEMBLE_2DEGREES) ||
           lpparam.packageName.equals(PACKAGES.SEMBLE_SPARK) ||
           lpparam.packageName.equals(PACKAGES.SEMBLE_VODAFONE)) {
            switch (lpparam.packageName) {
                case PACKAGES.SEMBLE_2DEGREES:
                    debugLog("Hooking Methods for Semble for 2Degrees Application.");
                    break;
                case PACKAGES.SEMBLE_SPARK:
                    debugLog("Hooking Methods for Semble for Spark Application.");
                    break;
                case PACKAGES.SEMBLE_VODAFONE:
                    debugLog("Hooking Methods for Semble for Vodafone Application.");
                    break;
            }
            hookSembleApplication(lpparam);
        }

        if(lpparam.packageName.equals(PACKAGES.TVNZ_ONDEMAND)) {
            debugLog("Hooking Methods for TVNZ OnDemand Application.");
            hookTVNZOnDemandApplication(lpparam);
        }

        if(lpparam.packageName.equals(PACKAGES.TV3NOW)) {
            debugLog("Hooking Methods for 3NOW Application.");
            hook3NOWApplication(lpparam);
        }

    }

    /**
     * A method that hooks the 3NOW packages if 3NOW is present on the device
     *
     * @param lpparam The package and process information of the current package
     */
    private void hook3NOWApplication(XC_LoadPackage.LoadPackageParam lpparam) {
        // Hooks Method which always returns "false" to indicate that no root tools were detected.
        // v2.0 - Class: ? | Method: ?
        // v2.0.1 - Class: com.scottyab.rootbeer.b | Method: a,b,c,d,a(str)
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "c", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "d", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "a", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
    }


    /**
     * A method that hooks the TVNZ OnDemand packages if TVNZ OnDemand is present on the device
     *
     * @param lpparam The package and process information of the current package
     */
    private void hookTVNZOnDemandApplication(XC_LoadPackage.LoadPackageParam lpparam) {
        // Hooks Method which always returns "false" to indicate that no root tools were detected.
        // v2.2 - Class: B | Method: ?
        // v2.3 - Class: F | Method: ?
        // v2.4 - Class: D | Method: D
        findAndHookMethod("nz.co.tvnz.ondemand.OnDemandApp", lpparam.classLoader, "D", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.TVNZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TVNZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
    }

    /**
     * A method that hooks the ANZ GoMoney New Zealand packages if ANZ GoMoney is present on the device
     *
     * @param lpparam The package and process information of the current package
     */
    public void hookAnzGoMoneyApplication(final XC_LoadPackage.LoadPackageParam lpparam) {
        final DeviceInfo deviceInfo = SpoofDevices.getDeviceInfo(SpoofDevices.DEVICE.SAMSUNG_GALAXY_NOTE_3);
        /**
         * Seitc API Root Check Hooks
         */
        //  - v5.3.0 and previous - Class: xxxxxx.jejeee | Method: isRooted
        findAndHookMethod("xxxxxx.jejeee", lpparam.classLoader, "isRooted", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        //  - v5.3.0 and previous - Class: xxxxxx.jejeee | Method: isRootedQuickCheck
        findAndHookMethod("xxxxxx.jejeee", lpparam.classLoader, "isRootedQuickCheck", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        //  - v5.3.0 and previous - Class: xxxxxx.jejeee | Method: isDebug
        findAndHookMethod("xxxxxx.jejeee", lpparam.classLoader, "isDebug", new XC_MethodHook() {
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
        //  - v5.1.1 - Class: nz.co.anz.android.mobilebanking.g.a.n | Method: l?
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.h.a.n | Method: l
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.h.a.n | Method: k
        findAndHookMethod("nz.co.anz.android.mobilebanking.h.a.n", lpparam.classLoader, "k", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        /*// SharedPrefs - "wallet_card_removed" Always returns false
        //  - v5.1.1 - nz.co.anz.android.mobilebanking.g.a.n | Method: k
        //  - v5.2.2 - nz.co.anz.android.mobilebanking.h.a.n | Method: k
        findAndHookMethod("nz.co.anz.android.mobilebanking.h.a.n", loadPackageParam.classLoader, "m", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });*/

        // Hooks the Shared Preferences method when editing the "wallet_invalid_rooted_device" value for debugging purposes
        // so we know how the Shared Preferences looks.
        //  - v5.1.1 - nz.co.anz.android.mobilebanking.g.a.n | Method: e
        //  - v5.2.2 - nz.co.anz.android.mobilebanking.h.a.n | Method: e
        //  - v5.2.3 - nz.co.anz.android.mobilebanking.h.a.n | Method: e
        findAndHookMethod("nz.co.anz.android.mobilebanking.h.a.n", lpparam.classLoader, "e", boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    callMethod(param.thisObject, "e", false);

                    Map<String, ?> sp = ((SharedPreferences) getObjectField(param.thisObject, "a")).getAll();

                    for(String key : sp.keySet()) {
                        debugLog("\t"+key+":"+sp.get(key));
                    }
                }
        });

        // ANZ GoMoney NZ's Internal logging Hook - Used for displaying potentially helpful information for easy hooking
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.i.e.z | Method: a
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.e.z | Method: a
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.z", lpparam.classLoader, "a", String.class, String.class, Throwable.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(isDebugMode()) {
                        Log.d(String.valueOf(param.args[0]), String.valueOf(param.args[1]), (Throwable) param.args[2]);
                    }
                }
            });


        // Superuser.apk and shell check
        //  - v5.1.1 - Class: nz.co.anz.android.mobilebanking.h.e.ah | Method: a?
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.i.e.ai | Method: a
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.e.ai | Method: a
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.ai", lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        // eligibleForWallet returns true
        //  - v5.1.1-5.2.2 - Class: nz.co.anz.android.mobilebanking.model.k | Method: f
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.model.k | Method: Removed in this version. Private variable still present.
        /*findAndHookMethod("nz.co.anz.android.mobilebanking.model.k", lpparam.classLoader, "f", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(true);
                }
            }
        });*/

        // eligibleForWallet returns true
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.b.bl Method: p
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.b.bo Method: p
        findAndHookMethod("nz.co.anz.android.mobilebanking.b.bo", lpparam.classLoader, "p", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(true);
                }
            }
        });


        // isEligibleForWallet returns true
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.j.a.b.ap Method: isEligibleForWallet
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.j.a.b.aw Method: isEligibleForWallet
        findAndHookMethod("nz.co.anz.android.mobilebanking.j.a.b.aw", lpparam.classLoader, "isEligibleForWallet", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(true);
                }
            }
        });

        // Hooking Mobile Promo (ANZ goMoney Wallet Popup Screen) to display on all devices
        //  - v5.2.2 and Previous - Class: nz.co.anz.android.mobilebanking.ui.util.MobileWalletPromoIgnoreCondition | Method: shouldIgnore
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.ui.util.MobileWalletPromoIgnoreCondition | Method: shouldIgnore
        findAndHookMethod("nz.co.anz.android.mobilebanking.ui.util.MobileWalletPromoIgnoreCondition", lpparam.classLoader, "shouldIgnore", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });


        /**
         * Device Spoofing Hooks
         */


        Class<?> builder = findClass("com.squareup.okhttp.Headers.Builder", lpparam.classLoader);

        // Device Information Builder used to communicate to ANZ Servers (Model and SDK INT)
        //  - v5.2.2 and Previous - Class: nz.co.anz.android.mobilebanking.i.c.i | Method: d
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.c.m | Method: d
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.c.m", lpparam.classLoader, "d", builder, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    callMethod(param.args[0], "add", "Android-Device-Description", deviceInfo.Build.MODEL);
                    callMethod(param.args[0], "add", "Android-Api-Version", Integer.toString(deviceInfo.VERSION.SDK_INT));
                    debugLog("we added spoof");
                    param.setResult(null);
                }
            }
        });

        // Device Information Builder used to communicate to ANZ Servers (Full Device Information)
        //  - v5.2.2 and Previous - Class: nz.co.anz.android.mobilebanking.i.c.i | Method: c
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.c.m | Method: c
        /*findAndHookMethod("nz.co.anz.android.mobilebanking.i.c.i", loadPackageParam.classLoader, "c", builder, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    String appVers = (String) callStaticMethod(findClass("nz.co.anz.android.mobilebanking.i.e.k", loadPackageParam.classLoader), "c");
                    String connectionType = (String) callStaticMethod(findClass("nz.co.anz.android.mobilebanking.i.e.k", loadPackageParam.classLoader), "d");
                    String orientation = (String) callStaticMethod(findClass("nz.co.anz.android.mobilebanking.i.e.k", loadPackageParam.classLoader), "f");
                    //callStaticMethod("nz.co.anz.android.mobilebanking.i.e.k", "c")


                    callMethod(param.args[0], "add", String.class, String.class, "User-Agent", "goMoney NZ/" + appVers + "/" + connectionType + "/" + deviceInfo.Build.BRAND+" "+deviceInfo.Build.MODEL + "/" + deviceInfo.VERSION.RELEASE + "/" + orientation + "/");

                    debugLog("we added spoof_2");
                    param.setResult(null);
                }
            }
        });*/


        // root check - redundant
        /*findAndHookMethod("nz.co.anz.android.mobilebanking.f", loadPackageParam.classLoader, "i", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    debugLog("we added spoof 3 - "+param.getResult());
                    debugLog("we added spoof 3");
                    param.setResult(param.getResult());
                }
            }
        });*/

        // Hooks method that is used to display the device brand and model in the Settings Activity. Although purely for cosmetic reasons.
        // FIND: return "[" + Build.BRAND + " " + Build.MODEL + "]";
        //  - v5.1.1 - Class: nz.co.anz.android.mobilebanking.h.e.k | Method: a
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: a
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: a
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult("[samsung SM-N9005]");
                }
            }
        });

        // Hooks method that is used to display the device brand and model in the Settings Activity. Although purely for cosmetic reasons or wallet eligibility.
        // FIND: return Build.BRAND + " " + Build.MODEL;
        //  - v5.1.1 - Class: nz.co.anz.android.mobilebanking.h.e.k | Method: b
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: b
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: b
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", lpparam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult("samsung SM-N9005");
                }
            }
        });

        // Hooks methods that are used to display the device Android Version in the Settings Activity. Unknown if used for cosmetic reasons or wallet eligibility..
        // FIND: return VERSION.RELEASE;
        //  - v5.1.1 - Class: nz.co.anz.android.mobilebanking.h.e.k | Method: e
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: e
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: e
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", lpparam.classLoader, "e", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult("4.4.2");
                }
            }
        });

        // Hook method which uses device model for wallet eligibility
        // ARGS EXAMPLE: str=Build.FINGERPRINT str2=Build.MODEL
        // FIND: return str.startsWith("generic")
        // v5.1.1 - Class: nz.co.anz.android.mobilebanking.h.e.u | Method: a
        // v5.2.2 - Class: nz.co.anz.android.mobilebanking.i.e.v | Method: a
        // v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.e.v | Method: a
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.v", lpparam.classLoader, "a", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult("SM-N9005");
                }
            }
        });

        // Hook method for Google Analytics Device Info. Although I highly doubt ANZ uses this for wallet eligibility, I simply did it as a precaution.
        // v5.1.1 - Class: com.google.android.gms.analytics.internal.l | Method: a
        // v5.2.2 - Class: com.google.android.gms.analytics.internal.l | Method: a
        // v5.3.0 - Class: com.google.android.gms.analytics.internal.l | Method: a
        findAndHookMethod("com.google.android.gms.analytics.internal.l", lpparam.classLoader, "a", String.class, String.class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    String str = (String) param.args[0];
                    String str2 = (String) param.args[1];
                    String str3 = (String) param.args[2];
                    String str4 = (String) param.args[3];
                    String str5 = "SM-N9005";
                    String str6 = (String) param.args[5];

                    param.setResult(String.format("%s/%s (Linux; U; Android %s; %s; %s Build/%s)", str, str2, str3, str4, str5, str6));
                }
            }
        });

        // most of the method hooks below simply call the method they are hooking to changed to
        // override what is stored in variables which all concern wallet eligibility.

        // FIND: Version.SDK_INT
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖ0416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖ0416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416ЖЖ0416ЖЖ0416", Integer.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.args[0] = Integer.valueOf(deviceInfo.VERSION.SDK_INT);
                    super.afterHookedMethod(param);

                }
            }
        });

        // FIND: Version.CODENAME
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416Ж0416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416Ж0416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416Ж0416Ж0416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "f6987b044504450445", "hlte");
                    param.args[0] = deviceInfo.VERSION.CODENAME;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.DEVICE
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b041604160416Ж0416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b041604160416Ж0416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b041604160416Ж0416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "f6987b044504450445", "hlte");
                    param.args[0] = deviceInfo.Build.DEVICE;
                    super.afterHookedMethod(param);
                }
            }
        });

        //FIND: Build.MODEL
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ04160416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ04160416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416ЖЖ04160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "bх0445ххх04450445х", "SM-N9005");
                    param.args[0] = deviceInfo.Build.MODEL;
                    super.afterHookedMethod(param);
                }
            }
        });
        // FIND: Build.PRODUCT
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04160416Ж04160416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04160416Ж04160416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416Ж04160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b04450445ххх04450445х", "hltexx");
                    param.args[0] = deviceInfo.Build.PRODUCT;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.DISPLAY
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж041604160416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416Ж041604160416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416Ж041604160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b04450445ххх04450445х", "hltexx");
                    param.args[0] = deviceInfo.Build.DISPLAY;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.BOARD
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04160416041604160416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04160416041604160416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416041604160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445х0445хх04450445х", "MSM8974");
                    param.args[0] = deviceInfo.Build.BOARD;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.CPU_ABI
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416ЖЖЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445х0445хх04450445х", "MSM8974");
                    param.args[0] = deviceInfo.Build.CPU_ABI;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.CPU_ABI2
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416ЖЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445х0445хх04450445х", "MSM8974");
                    param.args[0] = deviceInfo.Build.CPU_ABI2;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.MANUFACTURER
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416ЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416ЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416Ж0416ЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b044504450445хх04450445х", "samsung");
                    param.args[0] = deviceInfo.Build.MANUFACTURER;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.BRAND
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b041604160416ЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = deviceInfo.Build.BRAND;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.BOOTLOADER
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ0416Ж0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ0416Ж0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416ЖЖ0416Ж0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = deviceInfo.Build.BOOTLOADER;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.HARDWARE
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04160416Ж0416Ж0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04160416Ж0416Ж0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416Ж0416Ж0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = deviceInfo.Build.HARDWARE;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.SERIAL
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж04160416Ж0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416Ж04160416Ж0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416Ж04160416Ж0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = deviceInfo.Build.SERIAL;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.ID
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b041604160416ЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = deviceInfo.Build.ID;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.FINGERPRINT
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: bЖ04160416ЖЖЖ04160416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: bЖ04160416ЖЖЖ04160416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "bЖ04160416ЖЖЖ04160416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                Log.e("SuperKiwi", "afterHookedMethod: " + String.valueOf(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)));
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445ххх044504450445х", "samsung/hltexx/hlte:4.4.2/KOT49H/N9005XXUGNG1:user/release-keys");
                    param.args[0] = deviceInfo.Build.FINGERPRINT;
                    super.afterHookedMethod(param);
                }
            }
        });


        // FIND: Build.ID
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04220422ТТ0422042204220422
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04220422ТТ0422042204220422
        findAndHookMethod("xxxxxx.hchchh", lpparam.classLoader, "b04220422ТТ0422042204220422", android.content.Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    StringBuilder sb = new StringBuilder(500);
                    sb.append(deviceInfo.Build.DEVICE);      // r1 = android.os.Build.DEVICE;	 Catch:{ Exception -> 0x007d } (Line 261)
                    sb.append(deviceInfo.Build.MODEL);  // r1 = android.os.Build.MODEL;	 Catch:{ Exception -> 0x007d } (Line 263)
                    sb.append(deviceInfo.Build.PRODUCT);    // r1 = android.os.Build.PRODUCT;	 Catch:{ Exception -> 0x007d } (Line 265)
                    sb.append(deviceInfo.Build.BOARD);   // r1 = android.os.Build.BOARD;	 Catch:{ Exception -> 0x007d } (Line 265)
                    sb.append(deviceInfo.Build.MANUFACTURER);   // r1 = android.os.Build.MANUFACTURER;	 Catch:{ Exception -> 0x007d } (Line 267)
                    sb.append(deviceInfo.Build.BRAND);   // r1 = android.os.Build.BRAND;	 Catch:{ Exception -> 0x007d } (Line 269)
                    sb.append(deviceInfo.Build.HARDWARE);   // r1 = android.os.Build.HARDWARE;	 Catch:{ Exception -> 0x007d } (Line 289)
                    sb.append(deviceInfo.Build.SERIAL);   // r1 = android.os.Build.SERIAL;	 Catch:{ Exception -> 0x007d } (Line 306)

                    final TelephonyManager mTelephony = (TelephonyManager) ((Context) param.args[0]).getSystemService(Context.TELEPHONY_SERVICE);
                    String myAndroidDeviceId = mTelephony.getDeviceId();

                    sb.append(myAndroidDeviceId);

                    param.setResult(sb.toString().getBytes());
                }
            }
        });

        // Inserts a "SuperKiwi" Setting into the ANZ GoMoney Settings Fragement
        // If Debug mode is enabled, also inserts a "Device Info" fragment orginally hidden
        // v5.1.1 - ?
        // v5.2.2 - Class: nz.co.anz.android.mobilebanking.ui.fragment.SettingsFragment | Method: addTermsAndConditions
        // v5.3.0 - Class: nz.co.anz.android.mobilebanking.ui.fragment.SettingsFragment | Method: addTermsAndConditions
        findAndHookMethod("nz.co.anz.android.mobilebanking.ui.fragment.SettingsFragment", lpparam.classLoader, "addTermsAndConditions", LayoutInflater.class, new XC_MethodHook() {
            @SuppressLint("SetTextI18n")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //TODO: Add Commenting

                Class<?> settingGroupEnumClass = findClass("nz.co.anz.android.mobilebanking.ui.fragment.SettingsFragment.SettingsGroup", lpparam.classLoader);
                Object[] consts = settingGroupEnumClass.getEnumConstants();

                // v5.1.1 - ?
                // v5.2.2 - Enum: nz.co.anz.android.mobilebanking.ui.c.a | Method: -
                // v5.3.0 - Enum: nz.co.anz.android.mobilebanking.ui.d.a | Method: -
                Class settingEnumClass = findClass("nz.co.anz.android.mobilebanking.ui.d.a", lpparam.classLoader);
                Object[] consts2 = settingEnumClass.getEnumConstants();

                Activity act = (Activity) callMethod(param.thisObject, "getActivity");
                final Context ctx = act.getApplicationContext();

                int deviceInfoDrawableIconID = ctx.getResources().getIdentifier("tc_ic", "drawable", ctx.getPackageName());


                if(isDebugMode()) {
                    callMethod(param.thisObject, "insertItem", new Class<?>[]{LayoutInflater.class, java.lang.String.class, int.class, settingEnumClass, settingGroupEnumClass, boolean.class}, param.args[0], "Device Info", deviceInfoDrawableIconID, consts2[49], consts[2], false);
                }

                //Activity act = (Activity) callMethod(param.thisObject, "getActivity");
                //final Context ctx = act.getApplicationContext();

                //R.layout.settings_list_item_view
                int settings_list_item_view = ctx.getResources().getIdentifier("settings_list_item_view", "layout", ctx.getPackageName());
                int settingsListItemView_text = ctx.getResources().getIdentifier("settingsListItemView_text", "id", ctx.getPackageName());
                int settings_list_item_view__icon = ctx.getResources().getIdentifier("settings_list_item_view__icon", "id", ctx.getPackageName());
                int settings_list_item_view__chevron = ctx.getResources().getIdentifier("settings_list_item_view__chevron", "id", ctx.getPackageName());
                int nfc_ic = ctx.getResources().getIdentifier("nfc_ic", "drawable", ctx.getPackageName());
                int settings_list_item_view__newBadge = ctx.getResources().getIdentifier("settings_list_item_view__newBadge", "id", ctx.getPackageName());

                ViewGroup viewGroup = (ViewGroup) ((LayoutInflater) param.args[0]).inflate(settings_list_item_view, null);
                ((TextView) viewGroup.findViewById(settingsListItemView_text)).setText("SuperKiwi Settings");
                ((ImageView) viewGroup.findViewById(settings_list_item_view__icon)).setImageResource(nfc_ic);
                viewGroup.findViewById(settings_list_item_view__chevron).setVisibility(View.VISIBLE);
                View findViewById = viewGroup.findViewById(settings_list_item_view__newBadge);
                findViewById.setVisibility(View.INVISIBLE);
                viewGroup.setTag(consts2[44]);

                viewGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(PACKAGES.MODULE);
                        ctx.startActivity(launchIntent);
                    }
                });

                ViewGroup anzGroup = (ViewGroup) getObjectField(param.thisObject, "registrationGroup");

                int i3 = anzGroup.getChildCount();
                anzGroup.addView(viewGroup, i3);

            }
        });

        // Hook Method related to App window flags to allow for screenshooting.
        // N/A - Class: android.view.Window | Method: setFlags
        findAndHookMethod("android.view.Window", lpparam.classLoader, "setFlags", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED, PREFERENCES.DEFAULT_VALUES.ANZ.SCREENSHOT_ENABLED)) {
                    Integer flags = (Integer) param.args[0];
                    flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                    param.args[0] = flags;
                }
            }
        });
    }

    /**
     * A method that hooks all Semble packages if Semble is present on the device
     *
     * @param loadPackageParam The package and process information of the current package
     */
    private void hookSembleApplication(final LoadPackageParam loadPackageParam) {
        /**
         * Root Detection Methods
         */

        // Returns "true" to confirm that the integrity of the device is fine and is untouched to any modifications
        findAndHookMethod("com.csam.wallet.integrity.IntegrityCheckerImpl", loadPackageParam.classLoader, "checkDeviceIntegrity", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION)) {
                    param.setResult(true);
                }
            }
        });

        // Returns "false" to confirm that their is no root tools are installed.
        findAndHookMethod("com.mastercard.mtp.mobileclientutilities.DeviceUtility", loadPackageParam.classLoader, "isOSPossiblyCompromised", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        /**
         * Spoof Device / Unofficial Android OS Support Methods
         */

        // Spoof Device Model to Samsung Galaxy Note 3
        findAndHookMethod("com.csam.mclient.core.WalletContext", loadPackageParam.classLoader, "getDeviceModel", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SEMBLE.SPOOF_DEVICE)) {
                    param.setResult("SM-N9005");
                }
            }
        });

        // Spoof Device Manufacturer to Samsung Galaxy Note 3
        findAndHookMethod("com.csam.mclient.core.WalletContext", loadPackageParam.classLoader, "getManufacturer", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if(prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SEMBLE.SPOOF_DEVICE)) {
                    param.setResult("samsung");
                }
            }
        });

        // Spoofs Device System Android OS Version based on their actual device's information
        // unless the "Spoof Device" feature is enabled, then we Device System OS Version to
        // Samsung Galaxy Note 3 instead. This is handy when you have a compatible device, but
        // the OS Version is not which occurs when the OEM or your operator updates the firmware
        // of your device but Semble hasn't updated to handle the new firmware.
        findAndHookMethod("com.csam.mclient.core.WalletContext", loadPackageParam.classLoader, "getSystemOSVersion", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();

                debugLog("[Semble] Calling Method: com.csam.mclient.core.WalletContext.getSystemOSVersion()");

                if(prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SEMBLE.SPOOF_DEVICE)) {
                    debugLog("[Semble] Overriding Unofficial Android OS Support Method Hook due to having Spoof Device feature enabled.");
                    param.setResult("5.0");
                    return;
                }


                if (prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.MM_SUPPORT, PREFERENCES.DEFAULT_VALUES.SEMBLE.MM_SUPPORT)) {
                    debugLog("[Semble] Calling Method: Successfully hooked.");

                    debugLog("[Semble] Device: Brand: " + Build.BRAND + " Manufacturer: " + Build.MANUFACTURER + " Model: " + Build.MODEL);
                    debugLog("[Semble] Device: Fingerprint: " + Build.FINGERPRINT);
                    debugLog("[Semble] SembleCompatibilityList.isSupportedDevice(): " + SembleCompatibilityList.isSupportedDevice(loadPackageParam.packageName));
                    debugLog("[Semble] SembleCompatibilityList.isOSVersionSupported(): " + SembleCompatibilityList.isOSVersionSupported(loadPackageParam.packageName));

                    if (SembleCompatibilityList.isSupportedDevice(loadPackageParam.packageName)
                            && !SembleCompatibilityList.isOSVersionSupported(loadPackageParam.packageName)) {
                        SembleCompatibilityList.SembleDevice dInfo = SembleCompatibilityList.getSupportedDevice(loadPackageParam.packageName);
                        if (dInfo != null) {
                            debugLog("[Semble] Device's system OS is now seen as \"" + dInfo.getSupportedOSVersions().get(dInfo.getSupportedOSVersions().size() - 1) + "\" instead of \"" + Build.VERSION.RELEASE + "\"");
                            param.setResult(dInfo.getSupportedOSVersions().get(dInfo.getSupportedOSVersions().size() - 1));
                        } else {
                            debugLog("[Semble] Device is not supported by Semble at all. Check Semble Compatibility List. (Errno 2)");
                        }
                    } else {
                        debugLog("[Semble] Device is either not supported by Semble at all OR is completely supported hence no hook method execution is needed. Check Semble Compatibility List. (Errno 1)");
                    }
                } else {
                    debugLog("[Semble] Failed to Enter Hooked Method as feature is not enabled. Check SharedPrefs file to see if its readable. (Errno 1)");
                }
            }
        });
    }
}
