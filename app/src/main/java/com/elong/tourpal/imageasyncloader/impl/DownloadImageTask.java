package com.elong.tourpal.imageasyncloader.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.imageasyncloader.i.IFileCacheManager;
import com.elong.tourpal.imageasyncloader.other.RecyclableBitmapDrawable;

/**
 * DownloadImageTask
 *
 * @author tao.chen1
 */
public class DownloadImageTask extends ImageTask {
    private static final String TAG = "DownloadImageTask";
    private String mDownloadUrl = "";
    private RequestQueue mDownloadReqQueue = null;
    private IFileCacheManager mFileCacheManager = null;

    public DownloadImageTask(String url, RequestQueue downloadReqQueue, IFileCacheManager fileCacheManager) {
        mDownloadUrl = url;
        mDownloadReqQueue = downloadReqQueue;
        mFileCacheManager = fileCacheManager;
    }

    @Override
    public String getKey() {
        if (TextUtils.isEmpty(mDownloadUrl)) {
            return null;
        } else {
            return mDownloadUrl;
        }
    }

    @Override
    public void doInBackground() {
        if (mDownloadReqQueue != null) {
            ImageRequest imgReq = new ImageRequest(mDownloadUrl, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    if (mImageViewList.size() > 0) {
                        Context context = mImageViewList.get(0).getContext();
                        if (mFileCacheManager != null) {
                            mFileCacheManager.cacheBitmap(bitmap, mDownloadUrl);
                        }
                        result = new RecyclableBitmapDrawable(context.getResources(), bitmap);
                    }
                    onFinished();
                }
            }, 0, 0, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    onError();
                }
            });
            if (mDownloadReqQueue != null) {
                mDownloadReqQueue.add(imgReq);
            }
        } else {
            if (Env.DEBUG) {
                Log.e(TAG, "download failed, no request queue");
            }
        }
    }
}
