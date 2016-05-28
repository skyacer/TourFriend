package com.elong.tourpal.imageasyncloader.impl;

import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.imageasyncloader.i.IImageTask;
import com.elong.tourpal.imageasyncloader.other.Config;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tao.chen1 on 2015/1/15.
 */
public abstract class ImageTask implements IImageTask {

    private static final String TAG = "ImageTask";
    private static final Boolean DEBUG = Config.DEBUG;

    protected BitmapDrawable result = null;

    protected CopyOnWriteArrayList<CustomImageView> mImageViewList = new CopyOnWriteArrayList<>();

    private UIHandler mUIHandler = new UIHandler();

    private TaskFinishedListener mFinishedListener = null;

    public void setFinishedListener(TaskFinishedListener mFinishedListener) {
        this.mFinishedListener = mFinishedListener;
    }

    @Override
    public String getKey() {
        return null;
    }

    public void addImageView(CustomImageView imageView) {
        mImageViewList.add(imageView);
    }

    public void removeImageView(CustomImageView imageView) {
        mImageViewList.remove(imageView);
        if (mImageViewList.size() == 0) {
            mFinishedListener.onInterrupted(getKey());
        }
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        boolean isLoaded = doLoadFromCache();
        if (!isLoaded) {
            doInBackground();
        } else {
            if (Env.DEBUG) {
                Log.e(TAG, "bingo cache:" + getKey());
            }
            onFinished();
        }
        long costTime = System.currentTimeMillis() - startTime;
        if (DEBUG) {
            Log.e(TAG, "tid:" + Thread.currentThread().getId() + "---run time:" + costTime + "---key:" + getKey());
        }
    }

    public abstract void doInBackground();

    /**
     * try get image from cache
     *
     * @return true if bingo
     */
    public boolean doLoadFromCache() {
        String key = getKey();
        if (DEBUG) {
            Log.e(TAG, String.format("KEY=%s, cache size=%d", key, ImageCache.getsInstance().mMemoryCache.size()));
        }
        if (!TextUtils.isEmpty(key)) {
            result = ImageCache.getsInstance().get(key);
        }
        return result != null;
    }

    @Override
    public void onFinished() {
        //先加入cache中
        ImageCache.getsInstance().set(getKey(), result);
        //再通知界面刷新
        if (mUIHandler != null) {
            Message msg = mUIHandler.obtainMessage(WHAT_TASK_DONE);
            msg.obj = this;
            mUIHandler.sendMessage(msg);
        }
    }

    public void doFinished() {
        if (mImageViewList != null && mImageViewList.size() > 0) {
            for (CustomImageView view : mImageViewList) {
                view.setImageDrawable(result);
            }
        }
        //clear result's ref after task done
        result = null;
        //callback to remove task in ImageLoader
        if (mFinishedListener != null) {
            mFinishedListener.onFinished(getKey());
        }
    }

    @Override
    public void onError() {
        //TODO set error image the imageView's default image
    }

    public static final int WHAT_TASK_DONE = 1;

    private static class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            ImageTask task = (ImageTask) msg.obj;
            switch (msg.what) {
                case WHAT_TASK_DONE:
                    task.doFinished();
                    break;
                default:
                    break;
            }
        }
    }

    public interface TaskFinishedListener {
        public void onFinished(String key);

        public void onInterrupted(String key);
    }

    public boolean equalsClazAndKey(ImageTask task) {
        if (task != null) {
            String mClass = this.getClass().getName();
            String cClass = task.getClass().getName();
            if (mClass.equals(cClass)) {
                String mKey = getKey();
                String cKey = task.getKey();
                if (!TextUtils.isEmpty(mKey) && mKey.equals(cKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    public BitmapDrawable getLoadResult() {
        return result;
    }

}
