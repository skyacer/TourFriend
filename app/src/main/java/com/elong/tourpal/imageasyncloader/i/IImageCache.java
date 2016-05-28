package com.elong.tourpal.imageasyncloader.i;

import android.graphics.drawable.BitmapDrawable;

/**
 * IImageCache
 *
 * @author tao.chen1
 */
public interface IImageCache {
    public void set(String key, BitmapDrawable data);

    public BitmapDrawable get(String key);

    public void clear();

    public void delete(String key);

    /**
     * max byte
     *
     * @return
     */
    public long getMaxSize();

    /**
     * used byte
     *
     * @return
     */
    public long getUsedSpace();
}
