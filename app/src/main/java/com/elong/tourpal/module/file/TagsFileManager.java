package com.elong.tourpal.module.file;

import android.text.TextUtils;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.utils.SharedPref;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by zhitao.xu on 2015/5/20.
 */
public class TagsFileManager {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = TagsFileManager.class.getSimpleName();

    public static final String POST_TAGS_FILE_NAME = "tags_0.db";
    public static final String POST_TAGS_FILE_PREF = "tags_";
    public static ArrayList<String> POST_TAGS = new ArrayList<String>();

    public static void initPostTags() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tagLastFileName = SharedPref.getInstance().getString(SharedPref.KEY_LAST_DOWNLOAD_TAGS_FILE, TagsFileManager.POST_TAGS_FILE_NAME);
                InputStream is = null;

                // 如果没有更新的Tags文件就读入Assets下的文件,如果有新文件就读取新文件的tags文件
                if (TagsFileManager.POST_TAGS_FILE_NAME.equals(tagLastFileName)) {
                    // 读取Tags文件中的tag缓存到内存
                    try {
                        is = TourPalApplication.getAppContext().getAssets().open(POST_TAGS_FILE_NAME);
                    } catch (IOException e) {
                        if (DEBUG) {
                            Log.e(TAG, "e:", e);
                        }
                    }
                } else {
                    try {
                        // 获取新的tags文件InputStream
                        File[] files = TourPalApplication.getAppContext().getFilesDir().listFiles();
                        for (File f : files) {
                            if (f.getName().equals(tagLastFileName)) {
                                is = new FileInputStream(f);
                                break;
                            }
                        }
                    } catch (FileNotFoundException e) {
                        if (DEBUG) {
                            Log.e(TAG, "e:", e);
                        }
                    }
                }

                if (is != null){
                    buildTagsCache(is);
                }
            }
        }).start();
    }

    public static void buildTagsCache(InputStream inputStream) {
        ArrayList<String> tags = new ArrayList<String>();
        InputStream is = inputStream;
        BufferedReader br = null;
        boolean isOk = true;

        try {
            br = new BufferedReader(new InputStreamReader(is));
            String line;

            while (!TextUtils.isEmpty(line = br.readLine())) {
                if (DEBUG) {
                    Log.d(TAG, "tag:" + line);
                }
                tags.add(line);
            }
        } catch (IOException e) {
            isOk = false;
            if (DEBUG) {
                Log.e(TAG, "e:", e);
            }
        } finally {
            try {
                if (br != null) {
                    br.close();
                }

                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
        }

        // 添加tag标签
        if (isOk) {
            if (DEBUG) {
                for (String t1 : POST_TAGS) {
                    Log.d(TAG, "tag origin :" + t1);
                }
            }
            POST_TAGS.clear();
            POST_TAGS.addAll(tags);
            if (DEBUG) {
                for (String t2 : POST_TAGS) {
                    Log.d(TAG, "tag updated :" + t2);
                }
            }
        }
    }
}
