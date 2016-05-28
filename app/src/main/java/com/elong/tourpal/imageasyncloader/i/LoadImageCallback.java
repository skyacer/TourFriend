package com.elong.tourpal.imageasyncloader.i;

import android.graphics.drawable.BitmapDrawable;

/**
 * LoadImageCallback
 *
 * @author tao.chen1
 */
public interface LoadImageCallback {
    public void OnStart();

    public void OnError(int errCode);

    public void OnFinished(BitmapDrawable result);
}
