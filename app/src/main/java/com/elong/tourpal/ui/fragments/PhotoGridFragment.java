package com.elong.tourpal.ui.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.ui.activities.PostingTourActivity;
import com.elong.tourpal.ui.supports.album.AlbumConstant;
import com.elong.tourpal.ui.supports.album.AlbumHelper;
import com.elong.tourpal.ui.supports.album.AlbumUtils;
import com.elong.tourpal.ui.supports.album.PhotoGridAdapter;
import com.elong.tourpal.ui.supports.album.PhotoItem;
import com.elong.tourpal.ui.views.CommonTitleBar;

import java.util.List;

public class PhotoGridFragment extends Fragment {
    private GridView mGridView;
    private PhotoGridAdapter mAdapter;
    private AlbumHelper mHelper;
    private TextView mFinishButton;
    private int SELECT_PIC_NUM;
    private List<PhotoItem> mDataList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mHelper = new AlbumHelper(activity.getApplicationContext());//AlbumHelper.getHelper();
        ((PostingTourActivity) getActivity()).setAllPhotoItem(mHelper.getImageData());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_grid, container,
                false);
        initView(view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SELECT_PIC_NUM = ((PostingTourActivity) getActivity()).getCurrentSelectedPicNum();
        mDataList = ((PostingTourActivity) getActivity()).getAllPhotoItem();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView(View parent) {
        setTitleBar();
        mGridView = (GridView) parent.findViewById(R.id.pic_gridview);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new PhotoGridAdapter(getActivity(), mDataList, mCheckBoxClickListener);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Fragment photoFrg = new AlbumPhotoPreviewFragment();
                    FragmentTransaction transaction = PhotoGridFragment.this.getFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putInt(AlbumConstant.REVIEW_PIC_INDEX, position - AlbumConstant.GRID_HEADER_SIZE);
                    photoFrg.setArguments(bundle);
                    transaction.replace(R.id.upload_pic_fragment_contain, photoFrg,
                            AlbumConstant.FRAGMENT_IMAGE_DETAIL);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    ((PostingTourActivity) getActivity()).cameraPhoto();
                }
            }
        });

//        mFinishButton = (Button) parent.findViewById(R.id.bt);
        setFinishButtonText(((PostingTourActivity) getActivity()).getCheckedPicNum());
        mFinishButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                PostingTourActivity a = ((PostingTourActivity) getActivity());
                a.selectFinished();
                a.loadingAndUpdate();
                getFragmentManager().popBackStack();
//                getActivity().onBackPressed();

            }

        });

    }


//    private OnClickListener mCameraClickListener = new OnClickListener() {
//
//        @Override
//        public void onClick(View arg0) {
//            ((PostingTourActivity) getActivity()).cameraPhoto();
//        }
//    };

    private OnClickListener mCheckBoxClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag() - AlbumConstant.GRID_HEADER_SIZE;
            if (position < 0) {
                return;
            }

            String path = mDataList.get(position).mImagePath;
            PhotoItem item = mDataList.get(position);
            int checkNum = ((PostingTourActivity) getActivity()).getCheckedPicNum();

            if (!item.mIsSelected) {
                if (SELECT_PIC_NUM + checkNum < AlbumConstant.UPLOAD_PHOTO_MAX) {
                    ((PostingTourActivity) getActivity()).addCheckedPicPath(path);
                    item.mIsSelected = true;
                } else {
                    ((CheckBox) v).setChecked(false);
                    Toast.makeText(getActivity(), getString(R.string.posting_max_photo_select), Toast.LENGTH_LONG).show();
                }
            } else {
                ((PostingTourActivity) getActivity()).removeChechedPicPath(path);
                item.mIsSelected = false;
            }
            setFinishButtonText(((PostingTourActivity) getActivity()).getCheckedPicNum());
        }
    };

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

    private void setTitleBar() {
        CommonTitleBar titleBar = ((PostingTourActivity) getActivity()).mTitleBar;
        titleBar.setTitle(getString(R.string.posting_maint_album));
        titleBar.setBackgroundColor(getResources().getColor(R.color.titlebar_album_bg));
        titleBar.setBackImage(R.drawable.select_titlebar_black_back);
        titleBar.setRightTVBG(R.drawable.select_titlebar_black_block);
//        titleBar.setBackImage(R.drawable.title_bar_cancel);
        titleBar.setSettingTxt(getString(R.string.posting_maint_finish));
        mFinishButton = (TextView) titleBar.getRightButton();
        mFinishButton.setEnabled(false);
    }
}