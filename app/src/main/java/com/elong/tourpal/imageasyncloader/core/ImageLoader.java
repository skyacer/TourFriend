package com.elong.tourpal.imageasyncloader.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.imageasyncloader.impl.ImageTask;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ImageLoader
 *
 * @author tao.chen1
 */
public class ImageLoader {

    private static final String TAG = "ImageLoader";

    private ConcurrentHashMap<String, ImageTask> mWorkingTaskList;

    private static volatile ImageLoader sInstance = null;

    private RequestQueue mRequestQueue = null;

    private ImageLoader() {
        mWorkingTaskList = new ConcurrentHashMap<>();
    }

    public static ImageLoader getInstance() {
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoader();
                }
            }
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            Context c = context.getApplicationContext();
            if (c == null) {
                c = context;
            }
            mRequestQueue = Volley.newRequestQueue(c);
        }
        return mRequestQueue;
    }

    public void addImageTask(ImageTask task, CustomImageView imageView) {
        if (mWorkingTaskList != null) {
            mWorkingTaskList = new ConcurrentHashMap<>();
        }
        if (imageView != null) {
            final String taskKey = task.getKey();

            //check if imageView has the same task
            ImageTask oldTask = imageView.getCurrentTask();
            if (oldTask != null) {
                if (!oldTask.equalsClazAndKey(task)) {
                    //no same old task
                    oldTask.removeImageView(imageView);
                } else {
                    //has same old task
                    String oldTaskKey = oldTask.getKey();
                    if (!TextUtils.isEmpty(oldTaskKey)) {
                        if (mWorkingTaskList.containsKey(oldTaskKey)) {
                            //old task is working on background
                            //do nothing
                            return;
                        } else {
                            oldTask.removeImageView(imageView);
                        }
                    }
                }
            }

            ImageTask workingTask = mWorkingTaskList.get(taskKey);
            if (workingTask != null) {
                workingTask.addImageView(imageView);
                imageView.setCurrentTask(workingTask);
            } else {
                task.setFinishedListener(new ImageTask.TaskFinishedListener() {
                    @Override
                    public void onFinished(String key) {
                        if (!TextUtils.isEmpty(key)) {
                            mWorkingTaskList.remove(key);
                            if (Env.DEBUG) {
                                Log.e(TAG, "load finished key = " + key);
                            }
                        }
                    }

                    @Override
                    public void onInterrupted(String key) {
                        //if invoked, task of spec key in mWorkingTaskList is removed earlier than onFinished
                        if (!TextUtils.isEmpty(key)) {
                            mWorkingTaskList.remove(key);
                        }
                    }
                });
                task.addImageView(imageView);
                imageView.setCurrentTask(task);
                mWorkingTaskList.put(taskKey, task);
                if (Env.DEBUG) {
                    Log.e(TAG, "load task key = " + task.getKey());
                }
                ImageTaskExecutor.getInstance().execute(task);
            }
        }
    }

}
