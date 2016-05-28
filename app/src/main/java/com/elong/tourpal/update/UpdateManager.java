package com.elong.tourpal.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.views.CommonDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zhitao.xu on 2015/2/9.
 */
public class UpdateManager {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = UpdateManager.class.getSimpleName();
    private static final String APK_NAME_PREFIX = "elong_tourpal_";

    private String mLatestVersionUrl;
    private String mLatestVersionName;
    private String mLatestVersionApkName;

    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelUpdate = false;

    private Context mContext;

    private CommonDialog mDownloadDialog;

    private Handler mHandler = new MyHandler();

    class MyHandler extends Handler {
        /* 检查更新 */
        private static final int CHECK_VERSION = 1;
        /* 下载中 */
        private static final int WHAT_REFRESH_DOWNLOAD_PROGRESS = 2;
        /* 下载结束 */
        private static final int WHAT_DOWNLOAD_FINISH = 3;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_VERSION:
                    if (msg.arg1 == 1) {
                        // 显示提示对话框
                        showNoticeDialog();
                    } else {
                        if (msg.arg2 == 0) {
                            Toast.makeText(mContext, R.string.update_already_new, Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                // 正在下载
                case WHAT_REFRESH_DOWNLOAD_PROGRESS:
                    // 设置进度条位置
                    mDownloadDialog.setMessage(mContext.getString(R.string.update_downloading, String.valueOf(progress) + "%"));
                    break;
                case WHAT_DOWNLOAD_FINISH:
                    // 安装文件
                    installApk();
                    break;
                default:
                    break;
            }
        }

        ;
    }

    ;

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate(final boolean onBack) {
//        if (isUpdate()) {
//            // 显示提示对话框
//            showNoticeDialog();
//        } else {
//            Toast.makeText(mContext, R.string.update_already_new, Toast.LENGTH_LONG).show();
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                doCheckUpdate(onBack);
            }
        }).start();
    }

    /**
     * 检查软件是否有更新版本
     *
     * @return
     */
    private void doCheckUpdate(boolean onBack) {

        Request request = RequestBuilder.buildGetUpdateInfoRequest();
        MessageProtos.ResponseInfo responseInfo = request.get();
        if (responseInfo != null && responseInfo.getErrCode() == MessageProtos.SUCCESS) {
            MessageProtos.ClientUpdateInfo cui = responseInfo.getClientUpdateInfo();
            if (cui != null) {
                boolean needUpdate = cui.getNeedUpdate();
                if (needUpdate) {
                    //需要更新
                    mLatestVersionName = cui.getLatestVersionName();
                    mLatestVersionUrl = cui.getLatestVersionUrl();
                    mLatestVersionApkName = APK_NAME_PREFIX + mLatestVersionName + ".apk";
                }
                Message msg = mHandler.obtainMessage(MyHandler.CHECK_VERSION, needUpdate ? 1 : 0, onBack ? 1 : 0);
                mHandler.sendMessage(msg);
            }
        }

    }


    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {

        CommonDialog noticeDialog = new CommonDialog(mContext);
        noticeDialog.setTitle(R.string.update_version_title);
        noticeDialog.setMessage(R.string.update_version_info);
        noticeDialog.setRightBtnText(R.string.update_ok_btn);
        noticeDialog.setRightBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDownloadDialog();
            }
        });
        noticeDialog.setLeftBtnText(R.string.update_later_btn);
        noticeDialog.show();

    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        mDownloadDialog = new CommonDialog(mContext);
        mDownloadDialog.setTitle(R.string.update_loading_title);
        mDownloadDialog.setButtonsVisibility(false);
        mDownloadDialog.setMessage(mContext.getString(R.string.update_downloading, "0%"));
        mDownloadDialog.show();

//        // 构造软件下载对话框
//        AlertDialog.Builder builder = new Builder(mContext);
//        builder.setTitle(R.string.update_loading_title);
//        // 给下载对话框增加进度条
//        final LayoutInflater inflater = LayoutInflater.from(mContext);
//        View v = inflater.inflate(R.layout.update_progress, null);
//        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
//        builder.setView(v);
//        // 取消更新
//        builder.setNegativeButton(R.string.update_loading_cancel, new OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                // 设置取消状态
//                cancelUpdate = true;
//            }
//        });
//        mDownloadDialog = builder.create();
//        mDownloadDialog.show();
        // 现在文件
        downloadApk();
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 启动新线程下载软件
        new DownloadApkThread().start();
    }

    /**
     * 下载文件线程
     */
    private class DownloadApkThread extends Thread {

        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (!TextUtils.isEmpty(mLatestVersionUrl) && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = sdpath + "download";
                    URL url = new URL(mLatestVersionUrl);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, mLatestVersionApkName);
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(MyHandler.WHAT_REFRESH_DOWNLOAD_PROGRESS);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(MyHandler.WHAT_DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            } catch (IOException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    }

    ;

    /**
     * 安装APK文件
     */
    private void installApk() {
        File apkfile = new File(mSavePath, mLatestVersionApkName);
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}

