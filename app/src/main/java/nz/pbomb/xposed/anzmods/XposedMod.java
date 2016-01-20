package nz.pbomb.xposed.anzmods;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import common.GLOBAL;
import common.PREFERENCES;

import common.SembleCompatibilityList;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;

import common.PACKAGES;


public class XposedMod implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = "SuperKiwi::Mod"; // Tag used for logging

    private static XSharedPreferences prefs;

    /**
     * Displays debugging messages if debug mode is enabled and will display them in logcat
     * as well as the Xposed log file.
     *
     * @param message The message to display in the log files
     */
    private static void logging(String message) {
        if (isDebugMode()) {
            XposedBridge.log("[" + TAG + "] " + message);
        }
    }

    private static boolean isDebugMode() {
        if (GLOBAL.DEBUG) {
            return true;
        }

        if(prefs != null) {
            refreshSharedPreferences(false);
            if(prefs.getBoolean(PREFERENCES.KEYS.MAIN.DEBUG, PREFERENCES.DEFAULT_VALUES.MAIN.DEBUG)) {
                return true;
            }
        }
        return false;
    }

    private static void refreshSharedPreferences() {
        refreshSharedPreferences(true);
    }

    /**
     * Reloads and refreshes the package's shared preferences file to reload new confirgurations
     * that may have changed on runtime.
     */
    private static void refreshSharedPreferences(boolean displayLogs) {
        prefs = new XSharedPreferences(PACKAGES.MODULE);
        prefs.makeWorldReadable();
        prefs.reload();

        if(!displayLogs) {
            return;
        }

        // Logging the properties to see if the file is actually readable
        logging("Shared Preferences Properties:");
        logging("\tWorld Readable: " + prefs.makeWorldReadable());
        logging("\tPath: " + prefs.getFile().getAbsolutePath());
        logging("\tFile Readable: " + prefs.getFile().canRead());
        logging("\tExists: " + prefs.getFile().exists());

        // Display the preferences loaded, only if the file was readable otherwise display an error
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
            refreshSharedPreferences(false);
            logging("Module Loaded (Debug Mode: " + (isDebugMode() ? "ON" : "OFF") + ")");
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {
        // Don't continue execution of the method unless its ANZ, Semble or TVNZ OnDemand being loaded
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
            switch (loadPackageParam.packageName) {
                case PACKAGES.SEMBLE_2DEGREES:
                    logging("Hooking Methods for Semble for 2Degrees Application.");
                    break;
                case PACKAGES.SEMBLE_SPARK:
                    logging("Hooking Methods for Semble for Spark Application.");
                    break;
                case PACKAGES.SEMBLE_VODAFONE:
                    logging("Hooking Methods for Semble for Vodafone Application.");
                    break;
            }
            hookSembleApplication(loadPackageParam);
        }

        if(loadPackageParam.packageName.equals(PACKAGES.TVNZ_ONDEMAND)) {
            logging("Hooking Methods for TVNZ OnDemand Application.");
            hookTVNZOnDemandApplication(loadPackageParam);
        }
    }

    /**
     * A method that hooks the TVNZ OnDemand packages if TVNZ OnDemand is present on the device
     *
     * @param loadPackageParam The package and process information of the current package
     */
    private void hookTVNZOnDemandApplication(final LoadPackageParam loadPackageParam) {
        // Returns "false" to indicate that no root tools were detected.
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

    /**
     * A method that hooks the ANZ GoMoney New Zealand packages if ANZ GoMoney is present on the device
     *
     * @param loadPackageParam The package and process information of the current package
     */
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

        //Class<?> x = findClass("xxxxxx.ajaaaj", loadPackageParam.classLoader);
        // Method y = findMethodBestMatch(x,"b041604160416Ж0416ЖЖ0416", String.class);
        //XposedBridge.log(y.getName());

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
                    sb.append("hlte");      // r1 = android.os.Build.DEVICE;	 Catch:{ Exception -> 0x007d } (Line 261)
                    sb.append("SM-N9005");  // r1 = android.os.Build.MODEL;	 Catch:{ Exception -> 0x007d } (Line 263)
                    sb.append("hltexx");    // r1 = android.os.Build.PRODUCT;	 Catch:{ Exception -> 0x007d } (Line 265)
                    sb.append("MSM8974");   // r1 = android.os.Build.BOARD;	 Catch:{ Exception -> 0x007d } (Line 265)
                    sb.append("samsung");   // r1 = android.os.Build.MANUFACTURER;	 Catch:{ Exception -> 0x007d } (Line 267)
                    sb.append("samsung");   // r1 = android.os.Build.BRAND;	 Catch:{ Exception -> 0x007d } (Line 269)
                    sb.append("qcom");   // r1 = android.os.Build.HARDWARE;	 Catch:{ Exception -> 0x007d } (Line 289)
                    sb.append("unknown");   // r1 = android.os.Build.SERIAL;	 Catch:{ Exception -> 0x007d } (Line 306)

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


        // Inserts a "SuperKiwi" Setting into the ANZ GoMoney Settings Fragement
        // If Debug mode is enabled, also inserts a "Device Info" fragment orginally hidden
        findAndHookMethod("nz.co.anz.android.mobilebanking.ui.fragment.SettingsFragment", loadPackageParam.classLoader, "addTermsAndConditions", LayoutInflater.class, new XC_MethodHook() {
            @SuppressLint("SetTextI18n")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Class<?> settingGroupEnumClass = findClass("nz.co.anz.android.mobilebanking.ui.fragment.SettingsFragment.SettingsGroup", loadPackageParam.classLoader);
                Object[] consts = settingGroupEnumClass.getEnumConstants();

                Class settingEnumClass = findClass("nz.co.anz.android.mobilebanking.ui.c.a", loadPackageParam.classLoader);
                Object[] consts2 = settingEnumClass.getEnumConstants();

                if(isDebugMode()) {
                    callMethod(param.thisObject, "insertItem", new Class<?>[]{LayoutInflater.class, java.lang.String.class, int.class, settingEnumClass, settingGroupEnumClass, boolean.class}, param.args[0], "Device Info", 2130838121, consts2[45], consts[2], false);
                }

                Activity act = (Activity) callMethod(param.thisObject, "getActivity");
                final Context ctx = act.getApplicationContext();

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
                viewGroup.setTag(consts2[46]);

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

        // Spoofs Device System OS Version based on their current device unless the "Spoof Device"
        // feature is enabled, then we Device System OS Version to Samsung Galaxy Note 3 instead.
        findAndHookMethod("com.csam.mclient.core.WalletContext", loadPackageParam.classLoader, "getSystemOSVersion", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshSharedPreferences();

                logging("[Semble] Calling Method: com.csam.mclient.core.WalletContext.getSystemOSVersion()");

                if(prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.SEMBLE.SPOOF_DEVICE)) {
                    logging("[Semble] Overriding Unoffical Android OS Support Method Hook due to having Spoof Device feature enabled.");
                    param.setResult("5.0");
                    return;
                }


                if (prefs.getBoolean(PREFERENCES.KEYS.SEMBLE.MM_SUPPORT, PREFERENCES.DEFAULT_VALUES.SEMBLE.MM_SUPPORT)) {
                    logging("[Semble] Calling Method: Successfully hooked.");

                    logging("[Semble] Device: Brand: " + Build.BRAND + " Manufacturer: " + Build.MANUFACTURER + " Model: " + Build.MODEL);
                    logging("[Semble] Device: Fingerprint: " + Build.FINGERPRINT);
                    logging("[Semble] SembleCompatibilityList.isSupportedDevice(): " + SembleCompatibilityList.isSupportedDevice(loadPackageParam.packageName));
                    logging("[Semble] SembleCompatibilityList.isOSVersionSupported(): " + SembleCompatibilityList.isOSVersionSupported(loadPackageParam.packageName));

                    if (SembleCompatibilityList.isSupportedDevice(loadPackageParam.packageName)
                            && !SembleCompatibilityList.isOSVersionSupported(loadPackageParam.packageName)) {
                        SembleCompatibilityList.SupportedDevice dInfo = SembleCompatibilityList.getSupportedDevice(loadPackageParam.packageName);
                        if (dInfo != null) {
                            logging("[Semble] Device's system OS is now seen as \"" + dInfo.getSupportedOSVersions().get(dInfo.getSupportedOSVersions().size() - 1) + "\" instead of \"" + Build.VERSION.RELEASE + "\"");
                            param.setResult(dInfo.getSupportedOSVersions().get(dInfo.getSupportedOSVersions().size() - 1));
                        } else {
                            logging("[Semble] Device is not supported by Semble at all. Check Semble Compatibility List. (Errno 2)");
                        }
                    } else {
                        logging("[Semble] Device is either not supported by Semble at all OR is completely supported hence no hook method execution is needed. Check Semble Compatibility List. (Errno 1)");
                    }
                } else {
                    logging("[Semble] Failed to Enter Hooked Method as feature is not enabled. Check SharedPrefs file to see if its readable. (Errno 1)");
                }
            }
        });
    }
}
