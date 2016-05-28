package com.elong.tourpal.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.imageasyncloader.impl.CustomFileCacheManager;
import com.elong.tourpal.imageasyncloader.other.Utils;
import com.elong.tourpal.imageasyncloader.view.CropImageView;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.push.TourpalPushReceiver;
import com.elong.tourpal.ui.views.CommonTitleBar;
import com.elong.tourpal.ui.views.CommonToastDialog;
import com.elong.tourpal.utils.SharedPref;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CropPhotoActivity extends ActivityBase {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = CropPhotoActivity.class.getSimpleName();
    private CropImageView mImageView;
    private CommonTitleBar mTitleBar;
    private String mGalleryImageUrls;
    private ImageView mPreviewIV;

    public static void startActivity(Context c, String photoPath) {
        Intent intent = new Intent(c, CropPhotoActivity.class);
        intent.putExtra(Extras.EXTRA_IMAGE_PATH, photoPath);

        c.startActivity(intent);
    }


    public static void startActivityForResult (Activity activity, String photoPath, int requestCode) {
        Intent intent = new Intent(activity, CropPhotoActivity.class);
        intent.putExtra(Extras.EXTRA_IMAGE_PATH, photoPath);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photocut);
        resolveIntent();
        initViews();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extras.EXTRA_IMAGE_PATH)) {
            mGalleryImageUrls = intent.getStringExtra(Extras.EXTRA_IMAGE_PATH);
            if (DEBUG) {
                Log.d(TAG, "path = " + mGalleryImageUrls);
            }
        }
    }

    private void initViews() {
        initTitle();
        mImageView = (CropImageView) findViewById(R.id.photocut_iv);
        Drawable d = Drawable.createFromPath(mGalleryImageUrls);
        mPreviewIV = (ImageView) findViewById(R.id.mPreviewIV);
        mImageView.setDrawable(mGalleryImageUrls, 200, 200);
    }

    private void initTitle() {
        setTitleBarVisiable(false);
        mTitleBar = (CommonTitleBar) findViewById(R.id.photocut_titlebar);
        mTitleBar.setTitle(getString(R.string.posting_maint_album));
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.titlebar_album_bg));
        mTitleBar.setBackImage(R.drawable.select_titlebar_black_back);
        mTitleBar.setSettingVisible(true);
        mTitleBar.setSettingTxt("使用");
        mTitleBar.setOnSettingListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CommonToastDialog dialog = new CommonToastDialog(CropPhotoActivity.this);
                dialog.setIsLoading(true);
                dialog.setmDialogIcon(R.drawable.icon_loading);
                dialog.setDialogTitle(TourPalApplication.getAppContext().getString(R.string.user_center_uploading_avatar));
                dialog.show();

                final Bitmap b = getBitmap();//mImageView.getCropImage();
//                Drawable d = new BitmapDrawable(b);
//                mPreviewIV.setVisibility(View.VISIBLE);
//                mPreviewIV.setImageDrawable(d);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        if (DEBUG) {
                            Log.d(TAG, "baos=" + baos + ", array=" + baos.toByteArray());
                        }
                        Request request = RequestBuilder.buildUploadAvatarRequest(baos.toByteArray());
                        MessageProtos.ResponseInfo responseInfo = request.post();
                        boolean isSuccess = true;

                        if (responseInfo != null) {
                            if (responseInfo.getErrCode() == MessageProtos.SUCCESS) {
                                isSuccess = true;
                                // 成功后保存原图到文件中（以url的md5为文件名）
                                MessageProtos.UserInfo userInfo = responseInfo.getUserInfo();
                                String avatarUrl = userInfo.getHeadImgUrl();
                                String avatarOriginUrl = userInfo.getHeadImgUrlOrigin();
                                if (DEBUG) {
                                    Log.d(TAG, "url1=" + avatarUrl + ", url2=" + avatarOriginUrl);
                                }
                                CustomFileCacheManager cacheManager = new CustomFileCacheManager(CustomFileCacheManager.TYPE_USER_INFO);
                                cacheManager.cacheBitmap(b, avatarOriginUrl);
                                SharedPref.getInstance().setMyUserInfo(userInfo);

                                // 上传头像成功后，发送广播通知刷新界面头像
                                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(TourPalApplication.getAppContext());
                                broadcastManager.sendBroadcast(new Intent(Actions.ACTION_UPDATE_MY_USER_INFO));
                            } else {
                                isSuccess = false;
                            }

                        } else {
                            isSuccess = false;
                        }

                        if (isSuccess) {
                            mImageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.setIsLoading(false);
                                    dialog.setmDialogIcon(R.drawable.icon_tip);
                                    dialog.setDialogTitle(TourPalApplication.getAppContext().getString(R.string.user_center_upload_avatar_success));
                                    dialog.setDuration(2000);
                                    dialog.show();
                                    dialog.dismiss();
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });
                        } else {
                            mImageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.setIsLoading(false);
                                    dialog.setmDialogIcon(R.drawable.icon_tip);
                                    dialog.setDialogTitle(TourPalApplication.getAppContext().getString(R.string.user_center_upload_avatar_fail));
                                    dialog.setDuration(3000);
                                    dialog.show();
                                }
                            });
                        }
                    }
                }).start();

            }
        });
    }

    /**
     * 获取裁剪框内截图
     *
     * @return
     */
    private Bitmap getBitmap() {
        // 获取截屏
        View view = this.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        Bitmap finalBitmap = Bitmap.createBitmap(view.getDrawingCache(),
                mImageView.leftBound() + 1, frame.centerY() - mImageView.getCropHeight() / 2 + 1, mImageView.getCropWidth() - 2,
                mImageView.getCropHeight() - 2);

        // 释放资源
        view.destroyDrawingCache();
        return finalBitmap;
    }
}
