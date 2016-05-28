package com.elong.tourpal.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.ui.activities.PostingTourActivity;
import com.elong.tourpal.ui.supports.album.AlbumConstant;
import com.elong.tourpal.ui.supports.album.AlbumUtils;
import com.elong.tourpal.ui.supports.album.GalleryPagerAdapter;
import com.elong.tourpal.ui.supports.album.PhotoItem;
import com.elong.tourpal.ui.views.CommonTitleBar;

import java.util.ArrayList;
import java.util.List;

public class AlbumPhotoPreviewFragment extends Fragment {
    private ViewPager mViewPager;
    private GalleryPagerAdapter mAdapter;
    private int mCurrentPosition = 0;
    public List<String> mSelectedPhotoPaths;
    public int mSelectedNum;
    RelativeLayout photo_relativeLayout;
    private CheckBox mSelectCheckBox;
    private List<PhotoItem> mDataList;
    private int mSelectPicNum;
    private int mCheckedPicNum;
    private TextView mFinishButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_photo_preview, container, false);
        initView(view);
        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataList = ((PostingTourActivity) getActivity()).getAllPhotoItem();
        mSelectPicNum = ((PostingTourActivity) getActivity()).getCurrentSelectedPicNum();
        mCheckedPicNum = ((PostingTourActivity) getActivity()).getCurrentSelectedPicNum();
    }

    private void initView(View parent) {
        setTitleBar();
        setFinishButtonText(((PostingTourActivity) getActivity()).getCheckedPicNum());
        photo_relativeLayout = (RelativeLayout) parent
                .findViewById(R.id.photo_relativeLayout);
        photo_relativeLayout.setBackgroundColor(0x70000000);
        mSelectCheckBox = (CheckBox) parent.findViewById(R.id.posting_preview_selected);
        mSelectCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoItem item = mDataList.get(mCurrentPosition);
                int checkNum = ((PostingTourActivity) getActivity()).getCheckedPicNum();

                if (!item.mIsSelected) {
                    if (mSelectPicNum + checkNum < AlbumConstant.UPLOAD_PHOTO_MAX) {
                        ((PostingTourActivity) getActivity()).addCheckedPicPath(item.mImagePath);
                        item.mIsSelected = true;
                    } else {
                        ((CheckBox) v).setChecked(false);
                        Toast.makeText(getActivity(), getString(R.string.posting_max_photo_select), Toast.LENGTH_LONG).show();
                    }
                } else {
                    ((PostingTourActivity) getActivity()).removeChechedPicPath(item.mImagePath);
                    item.mIsSelected = false;
                }

                setFinishButtonText(((PostingTourActivity) getActivity()).getCheckedPicNum());
            }
        });
        List<String> paths = ((PostingTourActivity) getActivity()).getCurrentSelectedPicPaths();
        mSelectedPhotoPaths = paths;
        mSelectedNum = paths.size();
        mViewPager = (ViewPager) parent.findViewById(R.id.viewpager);
        mViewPager.setOnPageChangeListener(pageChangeListener);
        mAdapter = new GalleryPagerAdapter(this.getActivity().getApplicationContext(), getPhotoItemsPaths((ArrayList<PhotoItem>) mDataList));
        mViewPager.setAdapter(mAdapter);// 设置适配器
        int id = getArguments().getInt(AlbumConstant.REVIEW_PIC_INDEX);
        mViewPager.setCurrentItem(id);
    }

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        public void onPageSelected(int position) {// 页面选择响应函数
            mCurrentPosition = position;
            PhotoItem i = mDataList.get(position);
            mSelectCheckBox.setChecked(i.mIsSelected);
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {// 滑动中。。。

        }

        public void onPageScrollStateChanged(int arg0) {// 滑动状态改变
        }
    };

    private ArrayList<String> getPhotoItemsPaths(ArrayList<PhotoItem> photoItemArrayList) {
        ArrayList<String> ps = new ArrayList<String>();
        for (PhotoItem p : photoItemArrayList) {
            ps.add(p.mImagePath);
        }

        return ps;
    }

    private void setFinishButtonText(int selectNum) {
        if (selectNum > 0) {
            mFinishButton.setTextColor(getResources().getColor(R.color.titlebar_right_text_color_red));
            mFinishButton.setEnabled(true);
        } else {
            mFinishButton.setTextColor(getResources().getColor(R.color.titlebar_right_text_color_gray));
            mFinishButton.setEnabled(false);
        }
        mFinishButton.setText(AlbumUtils.getFinishBtnText(selectNum, (AlbumConstant.UPLOAD_PHOTO_MAX - ((PostingTourActivity)getActivity()).getCurrentSelectedPicNum())));
    }

    private View.OnClickListener mFinishBttonClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            PostingTourActivity a = ((PostingTourActivity) getActivity());
            a.selectFinished();
//            a.goToFistPager();
            a.loadingAndUpdate();
            getFragmentManager().popBackStack();
            getFragmentManager().popBackStack();
        }
    };

    private void setTitleBar() {
        CommonTitleBar titleBar = ((PostingTourActivity) getActivity()).mTitleBar;
        titleBar.setTitle(getString(R.string.posting_maint_album));
        titleBar.setBackgroundColor(getResources().getColor(R.color.titlebar_album_bg));
        titleBar.setBackImage(R.drawable.select_titlebar_black_back);
//        titleBar.setBackImage(R.drawable.title_bar_cancel);
        titleBar.setSettingTxt(getString(R.string.posting_maint_finish));
        mFinishButton = (TextView) titleBar.getRightButton();
        mFinishButton.setOnClickListener(mFinishBttonClickListener);
        mFinishButton.setEnabled(false);
    }
}
