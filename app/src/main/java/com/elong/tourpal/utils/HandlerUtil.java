package com.elong.tourpal.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;

/**
 * Created by LuoChangAn on 16/4/1.
 */
public class HandlerUtil {
    public static void doIdleHandler(MessageQueue.IdleHandler idleHandler) {
        Looper.getMainLooper().myQueue().addIdleHandler(idleHandler);
    }

    public static void doDelay(Runnable runable, long delayMillis) {
        Handler handler = new Handler();
        handler.postDelayed(runable, delayMillis);
    }

    public static void doOnMainThread(Runnable runnable) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }
}

