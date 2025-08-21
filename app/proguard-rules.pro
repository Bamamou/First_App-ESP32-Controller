# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Bluetooth related classes
-keep class android.bluetooth.** { *; }
-keep class androidx.core.app.ActivityCompat { *; }

# Keep custom classes
-keep class com.smarthome.esp32controller.** { *; }

# Keep Android framework classes
-keep class androidx.appcompat.** { *; }
-keep class com.google.android.material.** { *; }

# Remove debug logs in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Keep line numbers for debugging crashes
-keepattributes SourceFile,LineNumberTable

# Optimize code
-dontpreverify
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*