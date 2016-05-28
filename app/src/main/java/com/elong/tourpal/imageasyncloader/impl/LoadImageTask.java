package com.elong.tourpal.imageasyncloader.impl;

import android.content.Context;
import android.text.TextUtils;

import com.elong.tourpal.imageasyncloader.other.Utils;

/**
 * LoadImageTask
 *
 * @author tao.chen1
 */
public class LoadImageTask extends ImageTask {
    private String mFilePath = "";

    public LoadImageTask(String filePath) {
        mFilePath = filePath;
    }

    /**
     * when file path is empty ,return can be null
     *
     * @return
     */
    @Override
    public String getKey() {
        if (TextUtils.isEmpty(mFilePath)) {
            return "";
        } else {
            return mFilePath;
        }
    }

    @Override
    public void doInBackground() {
        if (!TextUtils.isEmpty(mFilePath) && mImageViewList != null && mImageViewList.size() > 0) {
            //use first Context of ImageView in mImageViewList to load Image
            Context context = mImageViewList.get(0).getContext();
            if (context != null) {
                result = Utils.readBitmapDrawable(context, mFilePath);

                onFinished();
            } else {
                onError();
            }
        }
    }
}
