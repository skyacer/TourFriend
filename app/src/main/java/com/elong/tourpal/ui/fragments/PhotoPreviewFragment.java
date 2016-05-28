package com.elong.tourpal.ui.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elong.tourpal.R;
import com.elong.tourpal.ui.activities.PostingTourActivity;
import com.elong.tourpal.ui.supports.album.AlbumConstant;
import com.elong.tourpal.ui.supports.album.GalleryPagerAdapter;
import com.elong.tourpal.ui.views.CommonTitleBar;

import java.util.List;

public class PhotoPreviewFragment extends Fragment {

    private ViewPager mViewPager;
    private GalleryPagerAdapter mAdapter;
    private int count;
    private List<Bitmap> mSelectedPhotos;
    private List<String> mSelectedPhotoPaths;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_review, container, false);
        initView(view);
        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initView(View parent) {
        setTitleBar();
        List<Bitmap> pics = ((PostingTourActivity) getActivity())
                .getCurrentSelectedPics();
        mSelectedPhotos = pics;
        List<String> paths = ((PostingTourActivity) getActivity())
                .getCurrentSelectedPicPaths();
        mSelectedPhotoPaths = paths;

        mViewPager = (ViewPager) parent.findViewById(R.id.viewpager);
        mViewPager.setOnPageChangeListener(pageChangeListener);

        mAdapter = new GalleryPagerAdapter(getActivity().getApplicationContext(), mSelectedPhotoPaths);// 构造adapter
        mViewPager.setAdapter(mAdapter);// 设置适配器
        int id = getArguments().getInt(AlbumConstant.REVIEW_PIC_INDEX);
        mViewPager.setCurrentItem(id);
    }

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        public void onPageSelected(int arg0) {// 页面选择响应函数
            count = arg0;
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {// 滑动中。。。

        }

        public void onPageScrollStateChanged(int arg0) {// 滑动状态改变

        }
    };

    private View.OnClickListener mDeleteClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mSelectedPhotoPaths.size() == 1) {
                mSelectedPhotos.clear();
                mSelectedPhotoPaths.clear();
                PostingTourActivity a = ((PostingTourActivity) getActivity());
                a.loadingAndUpdate();
                getFragmentManager().popBackStack();
            } else {
                mSelectedPhotos.remove(count);
                mSelectedPhotoPaths.remove(count);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private void setTitleBar() {
        CommonTitleBar titleBar = ((PostingTourActivity) getActivity()).mTitleBar;
        titleBar.setTitle(getString(R.string.posting_maint_album));
        titleBar.setBackgroundColor(getResources().getColor(R.color.titlebar_album_bg));
        titleBar.setBackImage(R.drawable.select_titlebar_black_back);
        titleBar.setSettingImg(getResources().getDrawable(R.drawable.select_titlebar_delete));
        /**
         * 删除后更新adapter显示，pagerAdapter需要重写getItemPosition方法才能实现
         */
        titleBar.setOnSettingListener(mDeleteClickListener);
    }
}
