package com.elong.tourpal.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.fragments.PhotoGridFragment;
import com.elong.tourpal.ui.fragments.PostingTourFragment;
import com.elong.tourpal.ui.supports.album.AlbumConstant;
import com.elong.tourpal.ui.supports.album.Bimp;
import com.elong.tourpal.ui.supports.album.PhotoItem;
import com.elong.tourpal.ui.views.CommonDialog;
import com.elong.tourpal.ui.views.CommonTitleBar;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PostingTourActivity extends FragmentActivity {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = PostingTourActivity.class.getSimpleName();
    /**
     * 保存的内容是desId_desName格式
     */
    public ArrayList<String> mSelectedDistinations = new ArrayList<String>();
    public ArrayList<String> mSelectedTags = new ArrayList<String>();
    private List<Bitmap> mSelectedPics = new ArrayList<Bitmap>();
    private List<PhotoItem> mDataList;
    private ArrayList<String> mSelectedPicPaths = new ArrayList<String>();
    private HashMap<String, String> mCheckedPicPaths = new HashMap<String, String>();
    private String mCameraPhotoPath = "";
    public CommonTitleBar mTitleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            Log.d(TAG, "onCreate()");
        }
        //打点数据
        Statistics.log(getApplicationContext(), StatisticsEnv.TOURPOST_ENTER);

        setContentView(R.layout.activity_posting_tour);
        mTitleBar = (CommonTitleBar) findViewById(R.id.posting_main_titlebar);
        mTitleBar.setOnBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) {
                    Log.e(TAG, "size:" + getSupportFragmentManager().getBackStackEntryCount());
                }
                doBackAction();
            }
        });

        PostingTourFragment uploadPicMain = new PostingTourFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.upload_pic_fragment_contain, uploadPicMain,
                        AlbumConstant.FRAGMENT_MAIN).commitAllowingStateLoss();

    }

    private void doBackAction() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            //打点数据
            Statistics.log(getApplicationContext(), StatisticsEnv.TOURPOST_CANCEL);
            if (checkPostingPageFinishOnBack()) {
                if (postExitCheck()) {
                    finish();
                }
            }
        }
    }

    /**
     * 对发帖退出进行检查提示
     *
     * @return true：直接退出，false：提示不退出
     */
    private boolean postExitCheck() {
        PostingTourFragment mainFragment = (PostingTourFragment) (getSupportFragmentManager().findFragmentByTag(AlbumConstant.FRAGMENT_MAIN));

        if (mainFragment.hasInput()) {
            CommonDialog dialog = new CommonDialog(this);
            dialog.setTitle(R.string.common_tips);
            dialog.setMessage(R.string.posting_main_exit_tip);
            dialog.setRightBtnOnclickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PostingTourActivity.super.onBackPressed();
                }
            });
            dialog.show();
            return false;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (DEBUG) {
            Log.e(TAG, "onSaveInstanceState");
        }
        super.onSaveInstanceState(outState);
        outState.putString(AlbumConstant.SAVE_CAMERA_PT_PATH, mCameraPhotoPath);
        outState.putStringArrayList(AlbumConstant.SAVE_SELECT_PT_PATH, mSelectedPicPaths);
        outState.putStringArrayList(AlbumConstant.SAVE_SELECT_DESTINATION, mSelectedDistinations);
        outState.putStringArrayList(AlbumConstant.SAVE_SELECT_TAG, mSelectedTags);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.e(TAG, "onRestoreInstanceState");
        }
        super.onRestoreInstanceState(savedInstanceState);
        mCameraPhotoPath = savedInstanceState.getString(AlbumConstant.SAVE_CAMERA_PT_PATH);
        mSelectedPicPaths = savedInstanceState
                .getStringArrayList(AlbumConstant.SAVE_SELECT_PT_PATH);
        mSelectedDistinations = savedInstanceState.getStringArrayList(AlbumConstant.SAVE_SELECT_DESTINATION);
        mSelectedTags = savedInstanceState.getStringArrayList(AlbumConstant.SAVE_SELECT_TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Log.d(TAG, "onDestroy()");
        }
    }

    public void albumPhoto() {
        Fragment imageFridFragment = new PhotoGridFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.upload_pic_fragment_contain,
                imageFridFragment, AlbumConstant.FRAGMENT_IMAGE_GRID);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void cameraPhoto() {
        //退出图片列表界面
        getSupportFragmentManager().popBackStack();

        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = new File(Environment.getExternalStorageDirectory() + "/elongsdu/tourpal");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "mico." + String.valueOf(System.currentTimeMillis())
                + ".jpg");
        mCameraPhotoPath = file.getPath();
        Uri imageUri = Uri.fromFile(file);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(openCameraIntent, AlbumConstant.TAKE_PICTURE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AlbumConstant.TAKE_PICTURE:
                if (mSelectedPicPaths.size() < AlbumConstant.UPLOAD_PHOTO_MAX
                        && resultCode == RESULT_OK) {
                    mSelectedPicPaths.add(mCameraPhotoPath);
                    // 发广播通知扫描图片,保证立马显示到相册中
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(new File(mCameraPhotoPath));
                    intent.setData(uri);
                    this.sendBroadcast(intent);
                    loadingAndUpdate();
                }

                break;
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<PostingTourActivity> mWActivity;

        public MyHandler(WeakReference<PostingTourActivity> wActivity) {
            mWActivity = wActivity;
        }

        @Override
        public void dispatchMessage(Message msg) {
            if (mWActivity.get() == null) {
                return;
            }

            switch (msg.what) {
                case 1:
                    PostingTourFragment pf = ((PostingTourFragment) mWActivity.get().getSupportFragmentManager()
                            .findFragmentByTag(AlbumConstant.FRAGMENT_MAIN));
                    if (pf == null) {
                        return;
                    }

                    pf.updateUI();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    Handler handler = new MyHandler(new WeakReference<PostingTourActivity>(this));

    public void loadingAndUpdate() {
        new Thread(new Runnable() {
            public void run() {
                mSelectedPics.clear();

                for (int i = 0; i < mSelectedPicPaths.size(); i++) {
                    try {
                        String path = mSelectedPicPaths.get(i);
                        Bitmap bm = Bimp.revitionImageSize(path);
                        mSelectedPics.add(bm);
                    } catch (IOException e) {
                    }
                }

                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }).start();
    }

    // ********************* 获取数据资源接口 **********************//
    public int getCurrentSelectedPicNum() {
        return mSelectedPics.size();
    }

    public List<Bitmap> getCurrentSelectedPics() {
        return mSelectedPics;
    }

    public List<String> getCurrentSelectedPicPaths() {
        return mSelectedPicPaths;
    }

    public List<PhotoItem> getAllPhotoItem() {
        return mDataList;
    }

    public void setAllPhotoItem(List<PhotoItem> datas) {
        mDataList = datas;
    }

    public void addCheckedPicPath(String path) {
        mCheckedPicPaths.put(path, path);
    }

    public void removeChechedPicPath(String path) {
        mCheckedPicPaths.remove(path);
    }

    public int getCheckedPicNum() {
        return mCheckedPicPaths.size();
    }

    public HashMap<String, String> getCheckedPicPaths() {
        return mCheckedPicPaths;
    }

    public void selectFinished() {
        Collection<String> values = mCheckedPicPaths.values();
        Iterator<String> i = values.iterator();
        for (; i.hasNext(); ) {
            mSelectedPicPaths.add(i.next());
        }
        clearSelectData();
    }

    public void clearSelectData() {
        mCheckedPicPaths.clear();
    }

    private Calendar mStartTime = Calendar.getInstance();
    private Calendar mEndTime;

    public void setStartTime(Calendar st) {
        mStartTime = st;
    }

    public void setEndTime(Calendar et) {
        mEndTime = et;
    }

    public Calendar getStartTime() {
        return mStartTime;
    }

    public Calendar getEndTime() {
        if (mEndTime == null) {
            mEndTime = Calendar.getInstance();
            mEndTime.add(Calendar.DATE, 1);
        }
        return mEndTime;
    }   

    @Override
    public void onBackPressed() {
        doBackAction();
    }

    private boolean checkPostingPageFinishOnBack() {
        boolean exit = true;
        try {
            PostingTourFragment mainFragment = (PostingTourFragment) (getSupportFragmentManager().findFragmentByTag(AlbumConstant.FRAGMENT_MAIN));
            exit = mainFragment.doOnBackWithoutFinish();
        } catch (Exception e) {
        }
        return exit;
    }
}
