# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep public interface
-keep class net.sourceforge.zbar.ImageScanner { *; }
-keep class net.sourceforge.zbar.Image { *; }
-keep class net.sourceforge.zbar.SymbolSet { *; }
-keep class net.sourceforge.zbar.Symbol { *; }
-keep class com.zolad.codescanner.view.* { *; }
-keep class com.zolad.codescanner.code.Scanner { *; }
-keep class com.zolad.codescanner.code.ImageUtil { *; }
-keep class com.zolad.codescanner.code.GraphicDecoder { *; }

-renamesourcefileattribute SourceFile
# 不混淆R文件中的所有静态字段，我们都知道R文件是通过字段来记录每个资源的id的，字段名要是被混淆了，id也就找不着了。
-keepclassmembers class **.R$* {
   public static <fields>;
}

#如果引用了v4或者v7包
-dontwarn android.support.**
# 保持native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}