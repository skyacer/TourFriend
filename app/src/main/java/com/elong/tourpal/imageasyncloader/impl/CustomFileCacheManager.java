package com.elong.tourpal.imageasyncloader.impl;

import android.os.Environment;

import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.imageasyncloader.i.IFileCacheManager;

/**
 * CustomFileCacheManager
 */
public class CustomFileCacheManager extends IFileCacheManager {

    private static final String DIR_ELONG = TourPalApplication.getAppContext().getPackageName() + "/";//"com.elong.tourpal/";
    private static final String DIR_HOT = "hot/";
    private static final String DIR_CONTENT = "post_content/";
    private static final String DIR_IMG = "img/";
    private static final String DIR_USER_INFO = "user_info/";
    public static final int TYPE_HOT_DESTINATION = 1;//热门景点背景图片
    public static final int TYPE_POSTS_CONTENT = 2;//帖子内容的图片
    public static final int TYPE_USER_INFO = 3;//个人资料的图片

    private int type = 2;

    public CustomFileCacheManager(int type) {
        this.type = type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String getBasePath() {
        String rootPath = getCommonRootPath();
        String path = "";
        switch (type) {
            case TYPE_HOT_DESTINATION:
                path = rootPath + DIR_HOT;
                break;
            case TYPE_POSTS_CONTENT:
                path = rootPath + DIR_CONTENT;
                break;
            case TYPE_USER_INFO:
                path = rootPath + DIR_USER_INFO;
                break;
        }
        return path;
    }

    private String getCommonRootPath() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return Environment.getExternalStorageDirectory().getPath() + "/" + DIR_ELONG + DIR_IMG;
        } else {
            return "sdcard/" + DIR_ELONG + DIR_IMG;
        }
    }
}
