package com.elong.tourpal.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.ui.supports.album.GalleryPagerAdapter;
import com.elong.tourpal.ui.views.CommonTitleBar;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends ActivityBase {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = GalleryActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private GalleryPagerAdapter mAdapter;
    private CommonTitleBar mTitleBar;
    private int mCurrentIdx = 0;

    private List<String> mGalleryImageUrls = new ArrayList<String>();
    private List<String> mGalleryThumbsUrls = new ArrayList<String>();

    /**
     *
     * @param c context
     * @param idx 初始显示的图片idx
     * @param urls 要显示的图片的url
     * @param thumbs 要显示的图片的缩略图
     */
    public static void startActivity(Context c, int idx, ArrayList<String> urls, ArrayList<String> thumbs) {
        Intent intent = new Intent(c, GalleryActivity.class);
        intent.putExtra(Extras.EXTRA_CURRENT_PAGE_IDX, idx);
        intent.putStringArrayListExtra(Extras.EXTRA_IMAGE_URLS, urls);
        intent.putStringArrayListExtra(Extras.EXTRA_THUMB_URLS, thumbs);

        c.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        resolveIntent();
        initViews();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        mCurrentIdx = intent.getIntExtra(Extras.EXTRA_CURRENT_PAGE_IDX, 0);
        if (intent.hasExtra(Extras.EXTRA_IMAGE_URLS)) {
            ArrayList<String> urls = intent.getStringArrayListExtra(Extras.EXTRA_IMAGE_URLS);
            if (DEBUG) {
                Log.d(TAG, "urls = " + urls.toString() + " url size=" + urls.size());
            }
            mGalleryImageUrls.addAll(urls);
        }
        if (intent.hasExtra(Extras.EXTRA_THUMB_URLS)) {
            ArrayList<String> thumbs = intent.getStringArrayListExtra(Extras.EXTRA_THUMB_URLS);
            mGalleryThumbsUrls.addAll(thumbs);
        }
    }

    private void initViews() {
        initTitle();
        mViewPager = (ViewPager) findViewById(R.id.gallery_viewpager);
        mViewPager.setOnPageChangeListener(pageChangeListener);

        mAdapter = new GalleryPagerAdapter(this.getApplicationContext(), mGalleryImageUrls, mGalleryThumbsUrls);// 构造adapter
        mViewPager.setAdapter(mAdapter);// 设置适配器
        mViewPager.setCurrentItem(mCurrentIdx);
    }

    private void initTitle() {
        setTitleBarVisiable(false);
        mTitleBar = (CommonTitleBar) findViewById(R.id.gallery_titlebar);
        mTitleBar.setTitle(getString(R.string.posting_maint_album));
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.titlebar_album_bg));
        mTitleBar.setBackImage(R.drawable.select_titlebar_black_back);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mCurrentIdx = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

}
