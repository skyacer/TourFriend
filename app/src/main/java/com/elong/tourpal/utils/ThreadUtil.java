package com.elong.tourpal.utils;

import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * Created by LuoChangAn on 16/4/4.
 */
public class ThreadUtil {
        public static void runOnAnsy(@NonNull Runnable runnable,
                                     @NonNull String threadName) {
            Thread thread = new Thread(runnable, threadName);
            thread.start();
        }

        public static boolean isMainLoop() {
            return Looper.getMainLooper() == Looper.myLooper();
        }

}
