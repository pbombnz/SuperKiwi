package nz.pbomb.xposed.anzmods;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

import com.crossbowffs.remotepreferences.RemotePreferences;

import java.util.Map;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import common.SpoofDevice;
import nz.pbomb.xposed.anzmods.preferences.PREFERENCES;

import common.SembleCompatibilityList;
import common.SpoofDevices;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;


public class XposedMod implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = "SuperKiwi:Mod"; // Tag used for debugLog

    private static SpoofDevice anzSpoofDevice;
    private static SpoofDevice sembleSpoofDevice;
    private static XSharedPreferences prefs;
    private static SharedPreferences sharedPreferences;


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
     * Indicates whether Debug Mode is enabled based on whether the hard-coded flag is enabled or
     * by the user enabling the setting themselves.
     *
     * @return Returns true if Debug Mode is enabled, otherwise return false.
     */
    public static boolean isDebugMode() {
        // Hard-coded flag check
        if (Common.getInstance().DEBUG) {
            return true;
        }

        // Load XSharedPreferences before we start checking if debug mode is enabled from user.
        if (prefs == null) {
            refreshSharedPreferences(false);
        }

        // Attempt to check if debug mode is toggled by the user
        // Use RemotePreference first as this will be have the most recent (and stable) data.
        // Use XSharedPreference instance if RemotePreference instance doesn't exist yet.
        if(sharedPreferences != null) {
            return sharedPreferences.getBoolean(PREFERENCES.KEYS.MAIN.DEBUG, PREFERENCES.DEFAULT_VALUES.MAIN.DEBUG);
        } else {
            return prefs.getBoolean(PREFERENCES.KEYS.MAIN.DEBUG, PREFERENCES.DEFAULT_VALUES.MAIN.DEBUG);
        }
    }

    /**
     * Reloads and refreshes the package's shared preferences file to reload new confirgurations
     * that may have changed on runtime.
     *
     * @param displayLogs To show logs or not.
     */
    public static void refreshSharedPreferences(boolean displayLogs) {
        prefs = new XSharedPreferences(Common.getInstance().PACKAGE_APP);
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
        if(!(lpparam.packageName.equals(Common.getInstance().PACKAGE_ASB_MOBILE) ||
                lpparam.packageName.equals(Common.getInstance().PACKAGE_ANZ_GOMONEY) ||
                lpparam.packageName.equals(Common.getInstance().PACKAGE_SEMBLE_2DEGREES) ||
                lpparam.packageName.equals(Common.getInstance().PACKAGE_SEMBLE_SPARK) ||
                lpparam.packageName.equals(Common.getInstance().PACKAGE_SEMBLE_VODAFONE) ||
                lpparam.packageName.equals(Common.getInstance().PACKAGE_TVNZ_ONDEMAND) ||
                lpparam.packageName.equals(Common.getInstance().PACKAGE_TV3NOW)
        )) {
            return;
        }

        // Hook the Application class of each application supported by SuperKiwi. This allows us
        // to have some application Context and use it to update the RemotePreferences Content Provider
        // and the other information. This allows us to read SharedPreference from the app process
        // without tripping SELinux (when it's in enforcing mode)
        findAndHookMethod("android.app.Application", lpparam.classLoader, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                sharedPreferences = new RemotePreferences((Context) param.thisObject, "nz.pbomb.xposed.anzmods.provider.preferences", Common.getInstance().SHARED_PREFS_FILE_NAME);
                anzSpoofDevice = SpoofDevices.getDeviceInfoByHumanDeviceName(sharedPreferences.getString(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE_CHOOSER, null));
                sembleSpoofDevice = SpoofDevices.getDeviceInfoByHumanDeviceName(sharedPreferences.getString(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE_CHOOSER, null));
            }
        });

        /*if(lpparam.packageName.equals(PACKAGES.ASB_MOBILE)) {
            debugLog("Hooking Methods for ASB Mobile Application.");
            hookAsbMobileApplication(lpparam);
        }*/

        if(lpparam.packageName.equals(Common.getInstance().PACKAGE_ANZ_GOMONEY)) {
            debugLog("Hooking Methods for ANZ goMoney NZ Application.");
            hookAnzGoMoneyApplication(lpparam);
        }

        if(lpparam.packageName.equals(Common.getInstance().PACKAGE_SEMBLE_2DEGREES) ||
           lpparam.packageName.equals(Common.getInstance().PACKAGE_SEMBLE_SPARK) ||
           lpparam.packageName.equals(Common.getInstance().PACKAGE_SEMBLE_VODAFONE)) {

            if(lpparam.packageName.equals(Common.getInstance().PACKAGE_SEMBLE_2DEGREES)) {
                debugLog("Hooking Methods for Semble for 2Degrees Application.");
            } else if(lpparam.packageName.equals(Common.getInstance().PACKAGE_SEMBLE_SPARK)) {
                debugLog("Hooking Methods for Semble for Spark Application.");
            } else if(lpparam.packageName.equals(Common.getInstance().PACKAGE_SEMBLE_VODAFONE)) {
                debugLog("Hooking Methods for Semble for Vodafone Application.");
            }
            hookSembleApplication(lpparam);
        }

        if(lpparam.packageName.equals(Common.getInstance().PACKAGE_TVNZ_ONDEMAND)) {
            debugLog("Hooking Methods for TVNZ OnDemand Application.");
            hookTVNZOnDemandApplication(lpparam);
        }

        if(lpparam.packageName.equals(Common.getInstance().PACKAGE_TV3NOW)) {
            debugLog("Hooking Methods for 3NOW Application.");
            hook3NOWApplication(lpparam);
        }

    }

    /*private void hookAsbMobileApplication(LoadPackageParam lpparam) {
        findAndHookMethod("nz.co.asb.mobile.helpers.RootHelper", lpparam.classLoader, "isDeviceRooted", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();
                if (prefs.getBoolean(PREFERENCES.KEYS.ASB.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ASB.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
    }*/

    /**
     * A method that hooks the 3NOW packages if 3NOW is present on the device
     *
     * @param lpparam The package and process information of the current package
     */
    private void hook3NOWApplication(final XC_LoadPackage.LoadPackageParam lpparam) {
        // Hooks Method which always returns "false" to indicate that no root tools were detected.
        // v2.0 - Class: ? | Method: ? (Most likely same as below)
        // v2.0.1 - Class: com.scottyab.rootbeer.b | Method: a,b,c,d,a(str)
        // v2.0.2 - Class: com.scottyab.rootbeer.b | Method: a,b,c,d,a(str)
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "c", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "d", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });
        findAndHookMethod("com.scottyab.rootbeer.b", lpparam.classLoader, "a", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.TV3NOW.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TV3NOW.ROOT_DETECTION)) {
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
        // v2.2 - Class: nz.co.tvnz.ondemand.OnDemandApp | Method: B
        // v2.3 - Class: nz.co.tvnz.ondemand.OnDemandApp | Method: F
        // v2.4 - Class: nz.co.tvnz.ondemand.OnDemandApp | Method: D
        // v2.5 - Class: nz.co.tvnz.ondemand.OnDemandApp | Method: D
        // v2.5.1 - Class: nz.co.tvnz.ondemand.OnDemandApp | Method: D
        findAndHookMethod("nz.co.tvnz.ondemand.OnDemandApp", lpparam.classLoader, "D", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.TVNZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.TVNZ.ROOT_DETECTION)) {
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
        /**
         * Seitc API Root Check Hooks
         */
        //  - v5.5.2 and previous - Class: xxxxxx.jejeee | Method: isRooted
        findAndHookMethod("xxxxxx.jejeee", lpparam.classLoader, "isRooted", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        //  - v5.5.2 and previous - Class: xxxxxx.jejeee | Method: isRootedQuickCheck
        findAndHookMethod("xxxxxx.jejeee", lpparam.classLoader, "isRootedQuickCheck", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        //  - v5.5.2 and previous - Class: xxxxxx.jejeee | Method: isDebug
        findAndHookMethod("xxxxxx.jejeee", lpparam.classLoader, "isDebug", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
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
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.h.a.n | Method: k
        //  - v5.5.2 - Class: nz.co.anz.android.mobilebanking.h.a.n | Method: k
        findAndHookMethod("nz.co.anz.android.mobilebanking.h.a.n", lpparam.classLoader, "k", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        /*// SharedPrefs - "wallet_card_removed" Always returns false
        //  - v5.1.1 - nz.co.anz.android.mobilebanking.g.a.n | Method: k
        //  - v5.2.2 - nz.co.anz.android.mobilebanking.h.a.n | Method: k
        //  - v5.3.0 - nz.co.anz.android.mobilebanking.h.a.n | Method: k
        //  - v5.4.2 - nz.co.anz.android.mobilebanking.h.a.n | Method: k
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
        //  - v5.3.0 - nz.co.anz.android.mobilebanking.h.a.n | Method: e
        //  - v5.4.2 - nz.co.anz.android.mobilebanking.h.a.n | Method: e
        //  - v5.5.2 - nz.co.anz.android.mobilebanking.h.a.n | Method: e
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
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.i.e.z | Method: a
        //  - v5.5.2 - Class: nz.co.anz.android.mobilebanking.i.e.z | Method: a
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
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.i.e.ai | Method: a
        //  - v5.5.2 - Class: nz.co.anz.android.mobilebanking.i.e.ai | Method: a
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.ai", lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
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
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.b.bp Method: p
        //  - v5.5.2 - Class: nz.co.anz.android.mobilebanking.b.bs Method: p
        findAndHookMethod("nz.co.anz.android.mobilebanking.b.bs", lpparam.classLoader, "p", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(true);
                }
            }
        });


        // isEligibleForWallet returns true
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.j.a.b.ap Method: isEligibleForWallet
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.j.a.b.aw Method: isEligibleForWallet
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.j.a.b.aw Method: isEligibleForWallet
        //  - v5.5.2 - Class: nz.co.anz.android.mobilebanking.j.a.b.ay Method: isEligibleForWallet
        findAndHookMethod("nz.co.anz.android.mobilebanking.j.a.b.ay", lpparam.classLoader, "isEligibleForWallet", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
                    param.setResult(true);
                }
            }
        });

        // Hooking Mobile Promo (ANZ goMoney Wallet Popup Screen) to display on all devices
        //  - v5.2.2 and Previous - Class: nz.co.anz.android.mobilebanking.ui.util.MobileWalletPromoIgnoreCondition | Method: shouldIgnore
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.ui.util.MobileWalletPromoIgnoreCondition | Method: shouldIgnore
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.ui.util.MobileWalletPromoIgnoreCondition | Method: shouldIgnore
        //  - v5.5.2 - Class: nz.co.anz.android.mobilebanking.ui.util.MobileWalletPromoIgnoreCondition | Method: shouldIgnore
        findAndHookMethod("nz.co.anz.android.mobilebanking.ui.util.MobileWalletPromoIgnoreCondition", lpparam.classLoader, "shouldIgnore", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION)) {
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
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.i.c.m | Method: d
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.c.m", lpparam.classLoader, "d", builder, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    callMethod(param.args[0], "add", "Android-Device-Description", anzSpoofDevice.Build.MODEL);
                    callMethod(param.args[0], "add", "Android-Api-Version", Integer.toString(anzSpoofDevice.VERSION.SDK_INT));
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


                    callMethod(param.args[0], "add", String.class, String.class, "User-Agent", "goMoney NZ/" + appVers + "/" + connectionType + "/" + spoofDevice.Build.BRAND+" "+spoofDevice.Build.MODEL + "/" + spoofDevice.VERSION.RELEASE + "/" + orientation + "/");

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
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: a
        //  - v5.5.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: a
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult("["+anzSpoofDevice.Build.BRAND+" "+anzSpoofDevice.Build.MODEL+"]");
                }
            }
        });

        // Hooks method that is used to display the device brand and model in the Settings Activity. Although purely for cosmetic reasons or wallet eligibility.
        // FIND: return Build.BRAND + " " + Build.MODEL;
        //  - v5.1.1 - Class: nz.co.anz.android.mobilebanking.h.e.k | Method: b
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: b
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: b
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: b
        //  - v5.5.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: b
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", lpparam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult(anzSpoofDevice.Build.BRAND+" "+anzSpoofDevice.Build.MODEL);
                }
            }
        });

        // Hooks methods that are used to display the device Android Version in the Settings Activity. Unknown if used for cosmetic reasons or wallet eligibility..
        // FIND: return VERSION.RELEASE;
        //  - v5.1.1 - Class: nz.co.anz.android.mobilebanking.h.e.k | Method: e
        //  - v5.2.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: e
        //  - v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: e
        //  - v5.4.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: e
        //  - v5.5.2 - Class: nz.co.anz.android.mobilebanking.i.e.k | Method: e
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", lpparam.classLoader, "e", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult(anzSpoofDevice.VERSION.RELEASE);
                }
            }
        });

        // Hook method which uses device model for wallet eligibility
        // ARGS EXAMPLE: str=Build.FINGERPRINT str2=Build.MODEL
        // FIND: return str.startsWith("generic")
        // v5.1.1 - Class: nz.co.anz.android.mobilebanking.h.e.u | Method: a
        // v5.2.2 - Class: nz.co.anz.android.mobilebanking.i.e.v | Method: a
        // v5.3.0 - Class: nz.co.anz.android.mobilebanking.i.e.v | Method: a
        // v5.4.2 - Class: nz.co.anz.android.mobilebanking.i.e.u | Method: a
        // v5.5.2 - Class: nz.co.anz.android.mobilebanking.i.e.u | Method: a
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.u", lpparam.classLoader, "a", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.setResult(anzSpoofDevice.Build.MODEL);

                }
            }
        });

        // Hook method for Google Analytics Device Info. Although I highly doubt ANZ uses this for wallet eligibility, I simply did it as a precaution.
        // v5.1.1 - Class: com.google.android.gms.analytics.internal.l | Method: a
        // v5.2.2 - Class: com.google.android.gms.analytics.internal.l | Method: a
        // v5.3.0 - Class: com.google.android.gms.analytics.internal.l | Method: a
        // v5.4.2 - Class: com.google.android.gms.analytics.internal.l | Method: a
        // v5.5.2 - Class: com.google.android.gms.analytics.internal.l | Method: a
        findAndHookMethod("com.google.android.gms.analytics.internal.l", lpparam.classLoader, "a", String.class, String.class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    String str = (String) param.args[0];
                    String str2 = (String) param.args[1];
                    String str3 = (String) param.args[2];
                    String str4 = (String) param.args[3];
                    String str5 = anzSpoofDevice.Build.MODEL;
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
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖ0416ЖЖ0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖ0416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416ЖЖ0416ЖЖ0416", Integer.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    param.args[0] = Integer.valueOf(anzSpoofDevice.VERSION.SDK_INT);
                    super.afterHookedMethod(param);

                }
            }
        });

        // FIND: Version.CODENAME
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416Ж0416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416Ж0416ЖЖ0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416Ж0416ЖЖ0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416Ж0416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416Ж0416Ж0416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "f6987b044504450445", "hlte");
                    param.args[0] = anzSpoofDevice.VERSION.CODENAME;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.DEVICE
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b041604160416Ж0416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b041604160416Ж0416ЖЖ0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b041604160416Ж0416ЖЖ0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b041604160416Ж0416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b041604160416Ж0416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "f6987b044504450445", "hlte");
                    param.args[0] = anzSpoofDevice.Build.DEVICE;
                    super.afterHookedMethod(param);
                }
            }
        });

        //FIND: Build.MODEL
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ04160416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ04160416ЖЖ0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ04160416ЖЖ0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ04160416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416ЖЖ04160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "bх0445ххх04450445х", "SM-N9005");
                    param.args[0] = anzSpoofDevice.Build.MODEL;
                    super.afterHookedMethod(param);
                }
            }
        });
        // FIND: Build.PRODUCT
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04160416Ж04160416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04160416Ж04160416ЖЖ0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b04160416Ж04160416ЖЖ0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b04160416Ж04160416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416Ж04160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b04450445ххх04450445х", "hltexx");
                    param.args[0] = anzSpoofDevice.Build.PRODUCT;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.DISPLAY
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж041604160416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416Ж041604160416ЖЖ0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж041604160416ЖЖ0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж041604160416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416Ж041604160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b04450445ххх04450445х", "hltexx");
                    param.args[0] = anzSpoofDevice.Build.DISPLAY;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.BOARD
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04160416041604160416ЖЖ0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04160416041604160416ЖЖ0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b04160416041604160416ЖЖ0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b04160416041604160416ЖЖ0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416041604160416ЖЖ0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445х0445хх04450445х", "MSM8974");
                    param.args[0] = anzSpoofDevice.Build.BOARD;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.CPU_ABI
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖЖЖ0416Ж0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖЖЖ0416Ж0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416ЖЖЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445х0445хх04450445х", "MSM8974");
                    param.args[0] = anzSpoofDevice.Build.CPU_ABI;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.CPU_ABI2
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖЖ0416Ж0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖЖ0416Ж0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b04160416ЖЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416ЖЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445х0445хх04450445х", "MSM8974");
                    param.args[0] = anzSpoofDevice.Build.CPU_ABI2;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.MANUFACTURER
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416ЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416ЖЖ0416Ж0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416ЖЖ0416Ж0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж0416ЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416Ж0416ЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b044504450445хх04450445х", "samsung");
                    param.args[0] = anzSpoofDevice.Build.MANUFACTURER;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.BRAND
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b041604160416ЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = anzSpoofDevice.Build.BRAND;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.BOOTLOADER
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ0416Ж0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ0416Ж0416Ж0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ0416Ж0416Ж0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b0416ЖЖ0416Ж0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416ЖЖ0416Ж0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = anzSpoofDevice.Build.BOOTLOADER;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.HARDWARE
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b04160416Ж0416Ж0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b04160416Ж0416Ж0416Ж0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b04160416Ж0416Ж0416Ж0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b04160416Ж0416Ж0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b04160416Ж0416Ж0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = anzSpoofDevice.Build.HARDWARE;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.SERIAL
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж04160416Ж0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b0416Ж04160416Ж0416Ж0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж04160416Ж0416Ж0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b0416Ж04160416Ж0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b0416Ж04160416Ж0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = anzSpoofDevice.Build.SERIAL;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.ID
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: b041604160416ЖЖ0416Ж0416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "b041604160416ЖЖ0416Ж0416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445хх0445х04450445х", "samsung");
                    param.args[0] = anzSpoofDevice.Build.ID;
                    super.afterHookedMethod(param);
                }
            }
        });

        // FIND: Build.FINGERPRINT
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.ajaaaj | Method: bЖ04160416ЖЖЖ04160416
        // v5.3.0 - Class: xxxxxx.ajaaaj | Method: bЖ04160416ЖЖЖ04160416
        // v5.4.2 - Class: xxxxxx.ajaaaj | Method: bЖ04160416ЖЖЖ04160416
        // v5.5.2 - Class: xxxxxx.ajaaaj | Method: bЖ04160416ЖЖЖ04160416
        findAndHookMethod("xxxxxx.ajaaaj", lpparam.classLoader, "bЖ04160416ЖЖЖ04160416", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                Log.e("SuperKiwi", "afterHookedMethod: " + String.valueOf(prefs.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)));
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    //setObjectField(param.thisObject, "b0445ххх044504450445х", "samsung/hltexx/hlte:4.4.2/KOT49H/N9005XXUGNG1:user/release-keys");
                    param.args[0] = anzSpoofDevice.Build.FINGERPRINT;
                    super.afterHookedMethod(param);
                }
            }
        });


        // FIND: Build.ID
        // v5.1.1 - ?
        // v5.2.2 - Class: xxxxxx.hchchh | Method: b04220422ТТ0422042204220422
        // v5.3.0 - Class: xxxxxx.hchchh | Method: b04220422ТТ0422042204220422
        // v5.4.2 - Class: xxxxxx.hchchh | Method: b04220422ТТ0422042204220422
        // v5.5.2 - Class: xxxxxx.hchchh | Method: b04220422ТТ0422042204220422
        findAndHookMethod("xxxxxx.hchchh", lpparam.classLoader, "b04220422ТТ0422042204220422", android.content.Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE)) {
                    StringBuilder sb = new StringBuilder(500);
                    sb.append(anzSpoofDevice.Build.DEVICE);      // r1 = android.os.Build.DEVICE;	 Catch:{ Exception -> 0x007d } (Line 261)
                    sb.append(anzSpoofDevice.Build.MODEL);  // r1 = android.os.Build.MODEL;	 Catch:{ Exception -> 0x007d } (Line 263)
                    sb.append(anzSpoofDevice.Build.PRODUCT);    // r1 = android.os.Build.PRODUCT;	 Catch:{ Exception -> 0x007d } (Line 265)
                    sb.append(anzSpoofDevice.Build.BOARD);   // r1 = android.os.Build.BOARD;	 Catch:{ Exception -> 0x007d } (Line 265)
                    sb.append(anzSpoofDevice.Build.MANUFACTURER);   // r1 = android.os.Build.MANUFACTURER;	 Catch:{ Exception -> 0x007d } (Line 267)
                    sb.append(anzSpoofDevice.Build.BRAND);   // r1 = android.os.Build.BRAND;	 Catch:{ Exception -> 0x007d } (Line 269)
                    sb.append(anzSpoofDevice.Build.HARDWARE);   // r1 = android.os.Build.HARDWARE;	 Catch:{ Exception -> 0x007d } (Line 289)
                    sb.append(anzSpoofDevice.Build.SERIAL);   // r1 = android.os.Build.SERIAL;	 Catch:{ Exception -> 0x007d } (Line 306)

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
        // v5.4.2 - Class: nz.co.anz.android.mobilebanking.ui.settings.bc | Method: b
        /*findAndHookMethod("nz.co.anz.android.mobilebanking.ui.settings.bc", lpparam.classLoader, "b", LayoutInflater.class, new XC_MethodHook() {
            @SuppressLint("SetTextI18n")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //TODO: Add Commenting

                // v5.3.0 - Class: nz.co.anz.android.mobilebanking.ui.fragment.SettingsFragment.SettingsGroup
                // v5.4.2 - Class: nz.co.anz.android.mobilebanking.ui.settings.bg
                Class<?> settingGroupEnumClass = findClass("nz.co.anz.android.mobilebanking.ui.settings.bg", lpparam.classLoader);
                Object[] consts = settingGroupEnumClass.getEnumConstants();

                // v5.1.1 - ?
                // v5.2.2 - Enum: nz.co.anz.android.mobilebanking.ui.c.a | Method: -
                // v5.3.0 - Enum: nz.co.anz.android.mobilebanking.ui.d.a | Method: -
                // v5.4.2 - Enum: nz.co.anz.android.mobilebanking.ui.c.a | Method: -
                Class settingEnumClass = findClass("nz.co.anz.android.mobilebanking.ui.c.a", lpparam.classLoader);
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
        });*/

        // Hook Method related to App window flags to allow for screenshooting.
        // N/A - Class: android.view.Window | Method: setFlags
        findAndHookMethod("android.view.Window", lpparam.classLoader, "setFlags", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if(sharedPreferences.getBoolean(PREFERENCES.KEYS.ANZ.SCREENSHOT_ENABLED, PREFERENCES.DEFAULT_VALUES.ANZ.SCREENSHOT_ENABLED)) {
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
     * @param lpparam The package and process information of the current package
     */
    private void hookSembleApplication(final LoadPackageParam lpparam) {
        /**
         * Root Detection Methods
         */

        // Returns "true" to confirm that the integrity of the device is fine and is untouched to any modifications
        findAndHookMethod("com.csam.wallet.integrity.IntegrityCheckerImpl", lpparam.classLoader, "checkDeviceIntegrity", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION)) {
                    param.setResult(true);
                }
            }
        });

        // Returns "false" to confirm that their is no root tools are installed.
        findAndHookMethod("com.mastercard.mtp.mobileclientutilities.DeviceUtility", lpparam.classLoader, "isOSPossiblyCompromised", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        /**
         * Spoof Device / Unofficial Android OS Support Methods
         */

        // Spoof Device Model to Samsung Galaxy Note 3
        findAndHookMethod("com.csam.mclient.core.WalletContext", lpparam.classLoader, "getDeviceModel", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SEMBLE.SPOOF_DEVICE)) {
                    param.setResult(sembleSpoofDevice.Build.MODEL);
                }
            }
        });

        // Spoof Device Manufacturer to Samsung Galaxy Note 3
        findAndHookMethod("com.csam.mclient.core.WalletContext", lpparam.classLoader, "getManufacturer", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();
                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SEMBLE.SPOOF_DEVICE)) {
                    param.setResult(sembleSpoofDevice.Build.MANUFACTURER);
                }
            }
        });

        // Spoofs Device System Android OS Version based on their actual device's information
        // unless the "Spoof Device" feature is enabled, then we Device System OS Version to
        // Samsung Galaxy Note 3 instead. This is handy when you have a compatible device, but
        // the OS Version is not which occurs when the OEM or your operator updates the firmware
        // of your device but Semble hasn't updated to handle the new firmware.
        findAndHookMethod("com.csam.mclient.core.WalletContext", lpparam.classLoader, "getSystemOSVersion", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //refreshSharedPreferences();

                debugLog("[Semble] Overriding Method: com.csam.mclient.core.WalletContext.getSystemOSVersion()");

                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SEMBLE.SPOOF_DEVICE)) {
                    debugLog("[Semble]\tDisabled Method Hook as Spoof Device feature is enabled.");
                    param.setResult(String.valueOf(sembleSpoofDevice.VERSION.RELEASE));
                    return;
                }


                if (sharedPreferences.getBoolean(PREFERENCES.KEYS.SEMBLE.MM_SUPPORT, PREFERENCES.DEFAULT_VALUES.SEMBLE.MM_SUPPORT)) {
                    debugLog("[Semble]\tChecking device information is suitable for this feature...");

                    boolean isSupportedDevice = SembleCompatibilityList.isSupportedDevice(lpparam.packageName);
                    boolean isSupportedOS = SembleCompatibilityList.isOSVersionSupported(lpparam.packageName);

                    debugLog("[Semble]\tDevice: Brand: " + Build.BRAND + " Manufacturer: " + Build.MANUFACTURER + " Model: " + Build.MODEL + " Fingerprint: \" + Build.FINGERPRINT");
                    debugLog("[Semble]\tSembleCompatibilityList.isSupportedDevice(): " + isSupportedDevice);
                    debugLog("[Semble]\tSembleCompatibilityList.isOSVersionSupported(): " + isSupportedOS);

                    if (isSupportedDevice && !isSupportedOS) {
                        SembleCompatibilityList.SembleDevice dInfo = SembleCompatibilityList.getSupportedDevice(lpparam.packageName);
                        String dInfo_latestOSSupport = dInfo.getSupportedOSVersions().get(dInfo.getSupportedOSVersions().size() - 1);
                        debugLog("[Semble]\tDevice's system OS is now spoofed as \"" + dInfo_latestOSSupport + "\" instead of \"" + Build.VERSION.RELEASE + "\"");
                        param.setResult(dInfo_latestOSSupport);
                    } else if (!isSupportedDevice && !isSupportedOS) {
                        debugLog("[Semble]\tDevice is not supported by Semble at all hence no hook method execution is needed. Check Semble Compatibility List or use Spoof Device feature.");
                    } else if (isSupportedDevice && isSupportedOS) {
                        debugLog("[Semble]\tDevice is completely supported hence no hook method execution is needed.");
                    }
                } else {
                    debugLog("[Semble]\tfeature disabled. Hook did not continue.");
                }
            }
        });

        /*
            Logging Methods
         */
        findAndHookMethod("com.csam.mclient.action.script.LoggerImpl", lpparam.classLoader, "isLoggingEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(1);
                }
            }
        });

        findAndHookMethod("com.csam.mno.extension.logging.Logger", lpparam.classLoader, "isEnable", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });

        findAndHookMethod("com.csam.mno.extension.logging.Logger", lpparam.classLoader, "isDebugLevel", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });

        findAndHookMethod("com.csam.mno.extension.logging.Logger", lpparam.classLoader, "isErrorLevel", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });

        findAndHookMethod("com.csam.mno.extension.logging.Logger", lpparam.classLoader, "isInfoLevel", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });

        findAndHookMethod("com.csam.proxy.logging.Logger", lpparam.classLoader, "isEnable", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });

        findAndHookMethod("com.csam.sbu.manager.a.a", lpparam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });

        findAndHookMethod("com.csam.sbu.manager.a.a", lpparam.classLoader, "d", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });

        findAndHookMethod("com.csam.sbu.manager.a.a", lpparam.classLoader, "e", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });

        findAndHookMethod("com.csam.tsmnz.logging.Logger", lpparam.classLoader, "isEnable", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });

        findAndHookMethod("com.gemalto.mbw.richclient.log.LoggerFactory", lpparam.classLoader, "isLoggingEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isDebugMode()) {
                    param.setResult(true);
                }
            }
        });
    }
}
