package com.elong.tourpal.imageasyncloader.i;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.imageasyncloader.other.Config;
import com.elong.tourpal.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * ILocalPathGenerator
 *
 * @author tao.chen1
 */
public abstract class IFileCacheManager {

    private static final String TAG = "IFileCacheManager";

    private static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;//mb
    private static final long EXPIRE_TIME_DIFF = 14 * 24 * 3600 * 1000;//millisecond
    private static final int MB = 1024 * 1024;

    public long fileCacheSize = 100;//mb

    /**
     * 返回cache图片的文件目录
     *
     * @return 返回值需要带上/
     */
    public abstract String getBasePath();

    public String convertUrlToFilePath(String url) {
        if (!TextUtils.isEmpty(url)) {
            return getBasePath() + Utils.getMD5(url);
        }
        return null;
    }

    public void checkAndCreateBasePath() {
        File baseDir = new File(getBasePath());
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    public void cacheBitmap(Bitmap bm, String url) {
        if (bm == null) {
            if (Env.DEBUG) {
                Log.e(TAG, " trying to save null bitmap");
            }
            return;
        }
        //判断sdcard上的空间
        if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            if (Env.DEBUG) {
                Log.e(TAG, "Low free space on sd, do not cache");
            }
            return;
        }
        checkAndCreateBasePath();
        String filePath = convertUrlToFilePath(url);
        if (filePath != null) {
            File file = new File(filePath);
            try {
                file.createNewFile();
                OutputStream outStream = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                if (Env.DEBUG) {
                    Log.i(TAG, "Image saved to sd");
                }
            } catch (FileNotFoundException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            } catch (IOException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
        }
    }

    /**
     * 计算sdcard上的剩余空间
     *
     * @return
     */
    private int freeSpaceOnSd() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = 0;
        if (Build.VERSION.SDK_INT < 18) {
            sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;
        } else {
            sdFreeMB = ((double) stat.getAvailableBlocksLong() * (double) stat.getBlockSizeLong()) / MB;
        }
        return (int) sdFreeMB;
    }

    /**
     * 计算存储目录下的文件大小，当文件总大小大于规定的CACHE_SIZE或者sdcard剩余空间小于FREE_SD_SPACE_NEEDED_TO_CACHE的规定
     * 那么删除40%最近没有被使用的文件
     */
    private void removeCache() {
        File dir = new File(getBasePath());
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        int dirSize = 0;
        for (int i = 0; i < files.length; i++) {
            dirSize += files[i].length();
        }
        if (dirSize > fileCacheSize * MB || FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            int removeFactor = (int) ((0.4 * files.length) + 1);
            Arrays.sort(files, new FileLastModifSort());

            if (Env.DEBUG) {
                Log.i(TAG, "Clear some expired cache files ");
            }

            for (int i = 0; i < files.length; i++) {
                if (i < removeFactor) {
                    files[i].delete();
                } else {
                    //超出保存期限的也删除
                    if (System.currentTimeMillis() - files[i].lastModified() > EXPIRE_TIME_DIFF) {
                        files[i].delete();
                    }
                }
            }
        }
    }

    /**
     * 根据文件的最后修改时间进行排序
     */
    class FileLastModifSort implements Comparator<File> {
        public int compare(File arg0, File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

}
