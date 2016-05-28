package com.elong.tourpal.utils;

import android.content.Context;

import com.elong.tourpal.application.TourPalApplication;

/**
 * Created by zhitao.xu on 2015/4/17.
 */
public class StatSharedPref extends SharedPrefBase {
    private static final String STAT_SHARED_PREF_NAME = "stat";
    private static StatSharedPref sInstance = null;

    public static StatSharedPref getInstance() {
        if (sInstance == null) {
            Context context = TourPalApplication.getAppContext();
            sInstance = new StatSharedPref(context);
        }
        return sInstance;
    }

    private StatSharedPref(Context context) {
        super(context, STAT_SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }
}
