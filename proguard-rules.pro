-keepattributes *Annotation*,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-repackageclasses 'com.dev.internal'
-allowaccessmodification

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

-keepclasseswithmembernames class * { native <methods>; }

-obfuscationdictionary obfuscation-dictionary.txt
-classobfuscationdictionary obfuscation-dictionary.txt
-packageobfuscationdictionary obfuscation-dictionary.txt

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5

-keep class com.dev.test.myfirstapp.utils.EncryptionUtils { public *; }
-keep class com.dev.test.myfirstapp.forwarders.TelegramForwarder { *; }