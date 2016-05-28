package com.elong.tourpal.imageasyncloader.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.imageasyncloader.core.ImageLoader;
import com.elong.tourpal.imageasyncloader.i.IFileCacheManager;
import com.elong.tourpal.imageasyncloader.impl.CustomFileCacheManager;
import com.elong.tourpal.imageasyncloader.impl.DownloadImageTask;
import com.elong.tourpal.imageasyncloader.impl.ImageTask;
import com.elong.tourpal.imageasyncloader.impl.LoadImageTask;
import com.elong.tourpal.imageasyncloader.other.RecyclableBitmapDrawable;

import java.io.File;

/**
 * CustomImageView
 *
 * @author tao.chen1
 */
public class CustomImageView extends ImageView {
    private static final String TAG = "CustomImageView";

    private String mImageUrl = null;

    private String mImagePath = null;

    private ImageTask mCurrentTask = null;

    private IFileCacheManager mFileCacheManager = null;

    public CustomImageView(Context context) {
        super(context);
        init();
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mFileCacheManager = new CustomFileCacheManager(CustomFileCacheManager.TYPE_POSTS_CONTENT);
    }

    /**
     * @param type TYPE in {@link com.elong.tourpal.imageasyncloader.impl.CustomFileCacheManager}
     */
    public void setFileCacheManagerType(int type){
        if (mFileCacheManager != null) {
            mFileCacheManager = new CustomFileCacheManager(type);
        } else {
            ((CustomFileCacheManager) mFileCacheManager).setType(type);
        }
    }

    public String convertUrlToPath(String url) {
        if (mFileCacheManager != null) {
            return mFileCacheManager.convertUrlToFilePath(url);
        } else {
            return null;
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        final Drawable previousDrawable = getDrawable();
        super.setImageDrawable(drawable);
        updateDrawableState(previousDrawable, false);
        updateDrawableState(drawable, true);
    }

    private void updateDrawableState(Drawable drawable, boolean isDisplayed){
        if (drawable instanceof RecyclableBitmapDrawable) {
            ((RecyclableBitmapDrawable) drawable).updateDisplayState(isDisplayed);
        }
    }

    public void loadImage() {
        if (TextUtils.isEmpty(mImagePath) && TextUtils.isEmpty(mImageUrl)) {
            return;
        }

        if (!TextUtils.isEmpty(mImagePath)) {
            loadImageFromFile();
        } else if (!TextUtils.isEmpty(mImageUrl)) {
            loadImageFromUrl();
        }
    }

    public void loadImageFromFile() {
        //load image from file
        LoadImageTask task = new LoadImageTask(mImagePath);
        ImageLoader.getInstance().addImageTask(task, this);
    }

    public void loadImageFromUrl() {
        String imgFilePath = convertUrlToPath(mImageUrl);
        if (!TextUtils.isEmpty(imgFilePath)){
            File imgFile = new File(imgFilePath);
            if (imgFile.exists()){
                //load image from file
                LoadImageTask task = new LoadImageTask(imgFilePath);
                ImageLoader.getInstance().addImageTask(task, this);
                if (Env.DEBUG){
                    Log.d(TAG, "file cache img exist, load local file cache : " + imgFilePath);
                }
            } else {
                //load image from the internet
//                setImageResource(R.color.common_gray2_bg);
                DownloadImageTask task = new DownloadImageTask(mImageUrl, ImageLoader.getInstance().getRequestQueue(getContext()), mFileCacheManager);
                ImageLoader.getInstance().addImageTask(task, this);
                if (Env.DEBUG){
                    Log.d(TAG, "file cache img not exist, download img from net : " + mImageUrl);
                }
            }
        }
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String imagePath) {
//        this.mImageUrl = null;
        this.mImagePath = imagePath;
    }

    public ImageTask getCurrentTask() {
        return mCurrentTask;
    }

    public void setCurrentTask(ImageTask task) {
        this.mCurrentTask = task;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String ImageUrl) {
//        this.mImagePath = null;
        this.mImageUrl = ImageUrl;
    }
}
