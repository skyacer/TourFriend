package com.elong.tourpal.application;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * 整个app的环境变量
 */
public class Env {

    private static final String TAG = "Env";

    public static final boolean DEBUG = false;

    public static final String CHANNEL_WANDOUJIA = "wandoujia";
    public static final String CHANNEL_BAIDU = "baidu";
    public static final String CHANNEL_QIHOO360 = "qihoo360";
    public static final String CHANNEL_XIAOMI = "xiaomi";
    public static final String CHANNEL_YINGYONGBAO = "yingyongbao";
    public static final String CHANNEL_HIAPK = "hiapk";

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }
        return versionCode;
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }
        return versionName;
    }

    /**
     * 获取渠道号
     * @param context
     * @return 渠道号（渠道名称）
     */
    public static String getChannelId(Context context) {
        String channelId = DEBUG ? "debug" : "";
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            channelId = appInfo.metaData.getString("CHANNEL_ID");
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG){
                Log.e(TAG, "e:", e);
            }
        }
        return channelId;
    }
}
