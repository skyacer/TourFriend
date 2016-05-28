package com.elong.tourpal.imageasyncloader.other;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.elong.tourpal.application.Env;

/**
 * RecyclableBitmapDrawable
 *
 * @author tao.chen1
 */
public class RecyclableBitmapDrawable extends BitmapDrawable {

    private static final String TAG = "RecycleBitmapDrawable";

    private int mRefCacheCount = 0;
    private int mRefDisplayCount = 0;
    private boolean mHasBeenDisplayed = false;

    private static final byte[] LOCK = new byte[0];

    public RecyclableBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public void updateDisplayState(boolean isDisplayed) {
        synchronized (LOCK) {
            if (isDisplayed) {
                mRefDisplayCount++;
                mHasBeenDisplayed = true;
            } else {
                mRefDisplayCount--;
            }
            checkState();
        }
    }

    public void updateCacheState(boolean isCached) {
        synchronized (this) {
            if (isCached) {
                mRefCacheCount++;
            } else {
                mRefCacheCount--;
            }
        }
        checkState();
    }

    /**
     * check if is necessary to call recycle bitmap
     */
    private void checkState() {
        if (mRefCacheCount <= 0 && mRefDisplayCount <= 0 && mHasBeenDisplayed && isBitmapValid()) {
            getBitmap().recycle();
            if (Env.DEBUG) {
                Log.e(TAG, "recycle : " + toString());
            }
        }
    }

    private boolean isBitmapValid() {
        Bitmap bitmap = getBitmap();
        return bitmap != null && !bitmap.isRecycled();
    }
}
