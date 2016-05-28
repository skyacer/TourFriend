package com.elong.tourpal.update;

import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.module.file.TagsFileManager;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.search.DestinationDataManager;
import com.elong.tourpal.search.DestinationSearchManager;
import com.elong.tourpal.utils.SharedPref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by zhitao.xu on 2015/4/22.
 */
public class DataUpdateManager {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = DataUpdateManager.class.getSimpleName();

    /**
     * 检查更新目的地数据，有更新就下载并更新到数据库，如果没有下载更新，还检查是否有已下载的文件需要更新到数据库
     */
    public static void destinationFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 检查目的地数据是否有更新，有并下载
                checkUpdateFileDownload();
            }
        }).start();
    }


    /**
     * 检查文件更新并下载
     *
     * @return
     */
    private static void checkUpdateFileDownload() {
        if (DEBUG) {
            Log.d(TAG, "checkUpdateFileDownload()");
        }
        int index = DestinationDataManager.DESTINATION_DATA_FILE_NAME.indexOf(".");
        String lastFileName = SharedPref.getInstance().getString(SharedPref.KEY_LAST_DOWNLOAD_DESTINATION_FILE, DestinationDataManager.DESTINATION_DATA_FILE_NAME.substring(0, index));
        String tagLastFileName = SharedPref.getInstance().getString(SharedPref.KEY_LAST_DOWNLOAD_TAGS_FILE, TagsFileManager.POST_TAGS_FILE_NAME);
        // update multi file
        Request request = RequestBuilder.buildDataUpdateRequest(new String[]{lastFileName, tagLastFileName});
        MessageProtos.ResponseInfo responseInfo = request.get();
        MessageProtos.DataUpdateResponseInfo duri = null;
        if (DEBUG) {
            String errorCode;
            if (responseInfo == null) {
                errorCode = "responseInfo null";
            } else {
                errorCode = "" + responseInfo.getErrCode();
            }
            if (DEBUG) {
                Log.e(TAG, "errorCode=" + errorCode);
            }
        }

        if (responseInfo != null && responseInfo.getErrCode() == MessageProtos.SUCCESS) {
            duri = responseInfo.getDataUpdateInfo();
        }

        // 判断服务器检查更新返回结果
        if (duri != null) {
            List<MessageProtos.UpdateData> newFilesDatas = duri.getUpdateDataList();// = responseInfo.getHotCitysList();
            InputStream is = null;
            FileOutputStream fos = null;

            // 处理待更新的文件列表
            for (MessageProtos.UpdateData newFD : newFilesDatas) {
                // fileName包含版本号信息
                String fileName = newFD.getFileName();
                if (DEBUG) {
                    Log.e(TAG, "filename=" + fileName);
                }
                // url不包含版本号信息
                String urlStr = newFD.getUrl();

                try {
                    URL url = new URL(urlStr);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    is = conn.getInputStream();

                    byte[] buf = new byte[1024];
                    String newFilePath = TourPalApplication.getAppContext().getFilesDir().getPath() + File.separator + fileName;
                    fos = new FileOutputStream(newFilePath);

                    int read = 0;
                    while ((read = is.read(buf)) > 0) {
                        fos.write(buf, 0, read);
                    }
                    fos.flush();

                    // update file download success!
                    if (DEBUG) {
                        Log.d(TAG, "checkUpdateFileDownload(), new file download success! filename:" + fileName);
                    }
                    //对新下载的文件做出来
                    doSomethingWithNewFile(newFilePath);
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "e:", e);
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            if (DEBUG) {
                                Log.e(TAG, "e:", e);
                            }
                        }
                    }

                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            if (DEBUG) {
                                Log.e(TAG, "e:", e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理新下载的数据文件
     *
     * @param newFilePath
     */
    private static void doSomethingWithNewFile(final String newFilePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(newFilePath);

                if (file.getName().startsWith("dest_")) {
                    // 设置最新文件
                    SharedPref.getInstance().setString(SharedPref.KEY_LAST_DOWNLOAD_DESTINATION_FILE, file.getName());
                    DestinationSearchManager.updateDestinationDataFromFile(file);
                } else if (file.getName().startsWith(TagsFileManager.POST_TAGS_FILE_PREF)) {
                    if (DEBUG) {
                        Log.d(TAG, "dosomething for file " + file.getName() + ",last file name "
                                + SharedPref.getInstance().getString(SharedPref.KEY_LAST_DOWNLOAD_TAGS_FILE,
                                TagsFileManager.POST_TAGS_FILE_NAME));
                    }
                    SharedPref.getInstance().setString(SharedPref.KEY_LAST_DOWNLOAD_TAGS_FILE, file.getName());
                    try {
                        TagsFileManager.buildTagsCache(new FileInputStream(file));
                    } catch (FileNotFoundException e) {
                        if (DEBUG) {
                            Log.e(TAG, "e:", e);
                        }
                    }
                }
            }
        }).start();
    }
}
