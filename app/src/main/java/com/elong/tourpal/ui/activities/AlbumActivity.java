package com.elong.tourpal.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.ui.supports.album.AlbumConstant;
import com.elong.tourpal.ui.supports.album.AlbumHelper;
import com.elong.tourpal.ui.supports.album.PhotoGridAdapter;
import com.elong.tourpal.ui.supports.album.PhotoItem;
import com.elong.tourpal.ui.views.CommonTitleBar;

import java.io.File;
import java.util.List;

/**
 * Created by zhitao.xu on 2015/5/7.
 */
public class AlbumActivity extends Activity {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = AlbumActivity.class.getSimpleName();
    private GridView mGridView;
    private PhotoGridAdapter mAdapter;
    private AlbumHelper mHelper;
    private TextView mFinishButton;
    private List<PhotoItem> mDataList;
    public CommonTitleBar mTitleBar;
    public static final int UPLOAD_AVATAR_RESULT = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        mHelper = new AlbumHelper(getApplicationContext());//AlbumHelper.getHelper();
        mDataList = mHelper.getImageData();
        initView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView() {
        setTitleBar();
        mGridView = (GridView) findViewById(R.id.pic_gridview);
        mAdapter = new PhotoGridAdapter(this, mDataList, null);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    PhotoItem pi = mDataList.get(position - 1);
                    if (DEBUG) {
                        Log.d(TAG, "select photo path : " + pi.mImagePath);
                    }
                    CropPhotoActivity.startActivityForResult(AlbumActivity.this, pi.mImagePath, UPLOAD_AVATAR_RESULT);
//                    Intent intent = new Intent(AlbumActivity.this, CropPhotoActivity.class);
//                    intent.putExtra(Extras.EXTRA_IMAGE_PATH, pi.mImagePath);
//
//                    AlbumActivity.this.startActivityForResult(intent, UPLOAD_AVATAR_RESULT);
                } else {
                    cameraPhoto();
                }
            }
        });
    }

    private void setTitleBar() {
        mTitleBar = (CommonTitleBar) findViewById(R.id.posting_main_titlebar);
        CommonTitleBar titleBar = mTitleBar;
        titleBar.setTitle(getString(R.string.posting_maint_album));
        titleBar.setBackgroundColor(getResources().getColor(R.color.titlebar_album_bg));
        titleBar.setBackImage(R.drawable.select_titlebar_black_back);
        titleBar.getRightButton().setVisibility(View.GONE);
//        titleBar.setRightTVBG(R.drawable.select_titlebar_black_block);
//        titleBar.setSettingTxt(getString(R.string.posting_maint_finish));
        mFinishButton = (TextView) titleBar.getRightButton();
        mFinishButton.setEnabled(false);
    }

    private String mCameraPhotoPath = "";

    public void cameraPhoto() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPLOAD_AVATAR_RESULT && resultCode == RESULT_OK) {
            finish();
        } else if (requestCode == AlbumConstant.TAKE_PICTURE && resultCode == RESULT_OK) {
            // 发广播通知扫描图片,保证立马显示到相册中
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(mCameraPhotoPath));
            intent.setData(uri);
            this.sendBroadcast(intent);
            // 对图片裁剪
            CropPhotoActivity.startActivityForResult(AlbumActivity.this, mCameraPhotoPath, UPLOAD_AVATAR_RESULT);
        }
    }
}
