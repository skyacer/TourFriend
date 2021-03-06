package com.elong.tourpal.application;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;


/**
 * Created by LuoChangAn on 16/4/1.
 */
public abstract class AppProfile {
    /* package */ static Context sContext;
    static long sInitTimestamp = 0;

    private static final String TAG = "AppProfile";

    public static final Context getContext() {
        return sContext;
    }

    public static final long getAppInitTimestamp() {
        return sInitTimestamp;
    }

    public static final String getPackageName() {
        return sContext.getPackageName();
    }


    public static void gotoBackground(Context context) {
        PackageManager pm = context.getPackageManager();
        ResolveInfo homeInfo = pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);

        if (homeInfo == null) return;

        ActivityInfo ai = homeInfo.activityInfo;
        Intent startIntent = new Intent(Intent.ACTION_MAIN);
        startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        startIntent.setComponent(new ComponentName(ai.packageName, ai.name));

        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(startIntent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG,e.getMessage());
        } catch (SecurityException e) {
            Log.d(TAG,e.getMessage());
        }
    }

    public static boolean isMainProcess() {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) sContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : am.getRunningAppProcesses()) {
            if (pid == appProcessInfo.pid) {
                if (TextUtils.equals(sContext.getPackageName(), appProcessInfo.processName)) {
                    Log.i("XXXX", appProcessInfo.processName + "; uid = " + appProcessInfo.uid + "; pid = " + pid);
                    return true;
                }
            }
        }
        return false;
    }
}
