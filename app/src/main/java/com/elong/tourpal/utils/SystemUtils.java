package com.elong.tourpal.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.elong.tourpal.application.Env;

import java.lang.reflect.Method;

/**
 * SystemUtils
 */
public class SystemUtils {

    private static final String TAG = "SystemUtils";
    public static final String DEFAULT_IMEI = "IMEI_DEFAULT";
    private static String cacheIMIE = null;

    /**
     * 获取设备唯一id
     *
     * @param context context
     * @return 设备唯一id
     */
    public static String getMid(Context context) {
        String imei = getImei(context);
        String AndroidID = android.provider.Settings.System.getString(context.getContentResolver(), "android_id");
        String serialNo = getDeviceSerialForMid();
        String m2 = Utils.getMD5("" + imei + AndroidID + serialNo);
        return m2;
    }

    private static String getDeviceSerialForMid() {
        String serial = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
        }
        return serial;
    }

    public static synchronized String getImei(Context ctx) {
        if (cacheIMIE != null) {
            return cacheIMIE;
        }
        if (ctx != null) {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                if (tm != null) {
                    cacheIMIE = tm.getDeviceId();
                    if (cacheIMIE != null) {
                        return cacheIMIE;
                    }
                }
            } catch (Exception e) {
                if (Env.DEBUG) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        return DEFAULT_IMEI;
    }
}
