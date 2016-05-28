# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Develop\SDK\Android\sdk/tools/proguard/proguard-android.txt
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


#js接口
-keepclassmembers class com.elong.tourpal.ui.activities.LoginWebviewActivity$JsInterface {
   public *;
}

#微信分享
-keep class com.tencent.mm.sdk.** {
   *;
}

#下拉刷新插件
-dontwarn in.srain.cube.**

#个推
-dontwarn com.igexin.**
-keep class com.igexin.**{*;}