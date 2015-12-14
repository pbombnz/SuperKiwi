package nz.pbomb.xposed.anzmods;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;

import common.PACKAGES;
import common.SETTINGS;
import common.XPOSED_STRINGS;


public class ANZHooker implements IXposedHookLoadPackage {
    private XSharedPreferences sharedPreferences;
    private View walletSettingsFragmentView = null;

    public ANZHooker() {
        sharedPreferences = new XSharedPreferences(PACKAGES.MODS);
        //sharedPreferences.makeWorldReadable();
        //XposedBridge.log(sharedPreferences.getFile().getAbsolutePath());
    }

    @Override
    public void handleLoadPackage(LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals(PACKAGES.ANZ_GOMONEY))
            return;

        /**
         * Seitc API Root Check Hooks
         */
        findAndHookMethod("xxxxxx.jejeee", loadPackageParam.classLoader, "isRooted", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.ROOT_DETECTION, SETTINGS.DEFAULT_VALUES.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        findAndHookMethod("xxxxxx.jejeee", loadPackageParam.classLoader, "isRootedQuickCheck", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.ROOT_DETECTION, SETTINGS.DEFAULT_VALUES.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        findAndHookMethod("xxxxxx.jejeee", loadPackageParam.classLoader, "isDebug", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.ROOT_DETECTION, SETTINGS.DEFAULT_VALUES.ROOT_DETECTION)) {
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
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.ROOT_DETECTION, SETTINGS.DEFAULT_VALUES.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        // SharedPrefs - "wallet_card_removed" Always returns false
        findAndHookMethod("nz.co.anz.android.mobilebanking.h.a.n", loadPackageParam.classLoader, "m", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.ROOT_DETECTION, SETTINGS.DEFAULT_VALUES.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        // Superuser.apk and shell check
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.ah", loadPackageParam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.ROOT_DETECTION, SETTINGS.DEFAULT_VALUES.ROOT_DETECTION)) {
                    param.setResult(false);
                }
            }
        });

        // eligibleForWallet returns true
        findAndHookMethod("nz.co.anz.android.mobilebanking.model.k", loadPackageParam.classLoader, "g", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.ROOT_DETECTION, SETTINGS.DEFAULT_VALUES.ROOT_DETECTION)) {
                    param.setResult(true);
                }
            }
        });


        /**
         * Display Disclaimer Hooks
         */
        findAndHookMethod("nz.co.anz.android.mobilebanking.presentation.wallet.view.WalletSettingsFragment", loadPackageParam.classLoader, "onCreateView", LayoutInflater.class, ViewGroup.class, Bundle.class,  new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                walletSettingsFragmentView = (View) param.getResult();
            }
        });

        findAndHookMethod("nz.co.anz.android.mobilebanking.presentation.wallet.view.WalletSettingsFragment", loadPackageParam.classLoader, "onActivateSwitchChecked", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log(String.valueOf(sharedPreferences.getBoolean(SETTINGS.KEYS.ROOT_DETECTION, SETTINGS.DEFAULT_VALUES.ROOT_DETECTION)));

                if(sharedPreferences.getBoolean(SETTINGS.KEYS.ROOT_DETECTION, SETTINGS.DEFAULT_VALUES.ROOT_DETECTION)) {
                    Switch activateSwitch = (Switch) getObjectField(param.thisObject, "activateSwitch");
                    //XposedBridge.log("wallet_invalid_rooted_device = false");

                    if (activateSwitch.isChecked()) {
                        //final View v = (View) param.thisObject;
                        //Context context = v.getContext();

                        AlertDialog alertDialog = new AlertDialog.Builder(walletSettingsFragmentView.getContext()).create();
                        alertDialog.setTitle(XPOSED_STRINGS.DISCLAIMER_TITLE);
                        alertDialog.setMessage(XPOSED_STRINGS.DISCLAIMER_SUMMARY);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        alertDialog.show();
                    }
                }
            }
        });


        /**
         * Device Spoofing Hooks
         */
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", loadPackageParam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.SPOOF_DEVICE, SETTINGS.DEFAULT_VALUES.SPOOF_DEVICE)) {
                    param.setResult("[samsung SM-N9005]");
                }
            }
        });
        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.k", loadPackageParam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.SPOOF_DEVICE, SETTINGS.DEFAULT_VALUES.SPOOF_DEVICE)) {
                    param.setResult("samsung SM-N9005");
                }
            }
        });

        findAndHookMethod("nz.co.anz.android.mobilebanking.i.e.u", loadPackageParam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(sharedPreferences.getBoolean(SETTINGS.KEYS.SPOOF_DEVICE, SETTINGS.DEFAULT_VALUES.SPOOF_DEVICE)) {
                    param.setResult(Build.FINGERPRINT.startsWith("generic") ? "HTC M7" : "SM-N9005");
                }
            }
        });
    }
}
