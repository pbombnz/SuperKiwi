# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Prashant\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class nz.pbomb.xposed.anzmods.XposedMod { public *; }
#-keep class nz.pbomb.xposed.anzmods.Common { public *; }
-keep class nz.pbomb.xposed.anzmods.preferences.PreferenceProvider { *; }
-keep class com.crossbowffs.remotepreferences.* { *; }

# Retain generated class which implement ViewBinder.
-keep public class * implements butterknife.internal.ViewBinder { public <init>(); }

# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinder.
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# Removes warning when includes JAR files
-keepattributes EnclosingMethod

