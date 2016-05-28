package com.elong.tourpal.natives;

/**
 * NativeUtils
 */
public class NativeUtils {

    private static final String TAG = "NativeUtils";

    static {
        System.loadLibrary("Native_Verifier");
    }

    public static final int MAX_TRUNK_SIZE = 5;

    public static String getVerifyString(String data) {
        return getVerifyString(getTrunkArrays(data));
    }

    public static native String getVerifyString(String[] params);

    public static native String getReqDESKey();

    public static native String getStoreKey();

    public static String getReqDESKey8() {
        return getReqDESKey().substring(0, 8);
    }

    private static String[] getTrunkArrays(String data) {
        if (data != null) {
            if (data.length() > MAX_TRUNK_SIZE) {
                data = data.substring(0, MAX_TRUNK_SIZE);
            }
            int len = data.length();
            String[] result = new String[len];
            for (int i = 0; i < len; i++) {
                result[i] = data.substring(i, i + 1);
            }
            return result;
        } else {
            return null;
        }
    }
}
