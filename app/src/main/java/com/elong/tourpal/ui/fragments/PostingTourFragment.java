package com.elong.tourpal.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.module.file.TagsFileManager;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.search.DestinationDataManager;
import com.elong.tourpal.search.DestinationSearchManager;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.activities.LoginWebviewActivity;
import com.elong.tourpal.ui.activities.PostListActivity;
import com.elong.tourpal.ui.activities.PostingTourActivity;
import com.elong.tourpal.ui.supports.SearchDestinationAdapter;
import com.elong.tourpal.ui.supports.TagsGridAdapter;
import com.elong.tourpal.ui.supports.UiUtils;
import com.elong.tourpal.ui.supports.album.AlbumConstant;
import com.elong.tourpal.ui.supports.album.AlbumUtils;
import com.elong.tourpal.ui.supports.album.PhotoReviewAdapter;
import com.elong.tourpal.ui.supports.album.SelectEditView;
import com.elong.tourpal.ui.views.CommonTitleBar;
import com.elong.tourpal.ui.views.CommonToastDialog;
import com.elong.tourpal.ui.views.FixedGridView;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.Utils;
import com.google.protobuf.micro.ByteStringMicro;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PostingTourFragment extends Fragment implements View.OnTouchListener {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = PostingTourFragment.class.getSimpleName();
    private ScrollView mScrollView;
    public SelectEditView mSelectEditView;
    private int mSelectEditViewWidth;
    private View mStartTimeLL;
    private View mEndTimeLL;
    private TextView mStartTime;
    private TextView mEndTime;
    private TagsGridAdapter mTagsGridViewAdapter;
    private FixedGridView mTagsGridView;
    private EditText mWeixinEt;
    private EditText mQQEt;
    private EditText mPhoneEt;
    private EditText mTourDetailEt;
    private GridView mGridView;
    private PhotoReviewAdapter mAdapter;
    /**
     * 与目的地搜索时显示的列表相对显示
     */
    private View mContentButDestination;
    /**
     * 目的地搜索提示列表
     */
    private View mDestinationSearchLL;
    private ListView mDestinationSearchListview;
    private SearchDestinationAdapter mDestinationSearchAdapter;
    // 标签
    private static final int TAG_SELECT_MAX = 3;
//    private ArrayList<String> mSelectTags = new ArrayList<String>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posting_tour, container, false);
        ((PostingTourActivity) getActivity()).clearSelectData();
        initView(view);
        return view;
    }

    private boolean mOnResume = false;

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.e(TAG, "onResume()");
        }
        mOnResume = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        if (DEBUG) {
            Log.e(TAG, "onPause()");
        }
        mOnResume = false;
        //隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSelectEditView.getWindowToken(), 0);
        super.onPause();
    }

    private void setTitleBar() {
        CommonTitleBar titleBar = ((PostingTourActivity) getActivity()).mTitleBar;
        titleBar.setTitle(R.string.posting_maint_title);
        titleBar.setBackgroundResource(R.drawable.default_title_bar_bg);
        titleBar.setBackImage(R.drawable.select_titlebar_back);
        titleBar.setSettingTxt(R.string.posting_maint_finish);
        ((TextView) titleBar.getRightButton()).setTextColor(getResources().getColor(R.color.white));
        titleBar.setRightTVBG(R.drawable.select_titlebar_block);
        titleBar.getRightButton().setEnabled(true);
        titleBar.setOnSettingListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打点数据
                Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.TOURPOST_FINISH);

                if (!TourPalApplication.getInstance().hasLogin()) {
                    //未登录，跳转到登录页面，让用户登录
                    Intent intent = new Intent(getActivity(), LoginWebviewActivity.class);
                    startActivity(intent);
                } else {
                    postTourPlan();
                }
            }
        });
    }

    private void initView(View parent) {
        setTitleBar();
        mScrollView = (ScrollView) parent.findViewById(R.id.posting_main_scroll);
        mScrollView.smoothScrollTo(0,0);
        //mScrollView.setOnTouchListener(this);
        mSelectEditView = (SelectEditView) parent.findViewById(R.id.posting_main_destination);
        mSelectEditView.setmCallBack(mCallBack);
        mSelectEditView.setItemLayout(R.layout.select_edit_view_item);
        mSelectEditView.setOnFocusChangeListener(mFocusChangeListener);
        mSelectEditView.addTextChangedListener(mTextWatcher);
        ArrayList<String> ds = ((PostingTourActivity) getActivity()).mSelectedDistinations;
        ArrayList<String> d = new ArrayList<String>();
        for (String s : ds) {
            d.add(s.substring(s.indexOf("_") + 1));
        }
        mSelectEditView.initItemViews(d, mSelectEditViewWidth);
        mContentButDestination = parent.findViewById(R.id.posting_main_content_but_destination);
        mDestinationSearchLL = parent.findViewById(R.id.posting_main_search_ll);
        mDestinationSearchListview = (ListView) parent.findViewById(R.id.posting_main_destination_search_listview);
        mDestinationSearchListview.setOnItemClickListener(mDestinationOnItemClickListener);
        mStartTimeLL = parent.findViewById(R.id.posting_main_start_time_ll);
        mEndTimeLL = parent.findViewById(R.id.posting_main_end_time_ll);
        mStartTime = (TextView) parent.findViewById(R.id.posting_main_start_time);
        mEndTime = (TextView) parent.findViewById(R.id.posting_main_end_time);
        PostingTourActivity pa = (PostingTourActivity) getActivity();
        Calendar sc = pa.getStartTime();//Calendar.getInstance();
        Calendar ec = pa.getEndTime();
        mStartTime.setText(getString(R.string.posting_main_date, sc.get(Calendar.MONTH) + 1, sc.get(Calendar.DATE)));
        mEndTime.setText(getString(R.string.posting_main_date, ec.get(Calendar.MONTH) + 1, ec.get(Calendar.DATE)));
        mTagsGridView = (FixedGridView) parent.findViewById(R.id.posting_main_tags);
        ArrayList<TagsGridAdapter.TagData> datas = new ArrayList<TagsGridAdapter.TagData>();

        for (String t : TagsFileManager.POST_TAGS) {
            TagsGridAdapter.TagData tagData = new TagsGridAdapter.TagData();
            tagData.mTitle = t;

            for (String s : ((PostingTourActivity)getActivity()).mSelectedTags) {
                if (tagData.mTitle.equals(s)) {
                    tagData.mIsChecked = true;
                    break;
                }
            }
            datas.add(tagData);
        }
        mTagsGridViewAdapter = new TagsGridAdapter(getActivity(), datas, mTagClickListener);
        mTagsGridView.setAdapter(mTagsGridViewAdapter);
        mWeixinEt = (EditText) parent.findViewById(R.id.posting_main_wechat_et);
        mQQEt = (EditText) parent.findViewById(R.id.posting_main_qq_et);
        mPhoneEt = (EditText) parent.findViewById(R.id.posting_main_phone_et);
        MessageProtos.UserInfo userInfo = SharedPref.getInstance().getMyUserInfo();

        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.getWeixin())) {
                mWeixinEt.setText(userInfo.getWeixin());
            }

            if (!TextUtils.isEmpty(userInfo.getQq())) {
                mQQEt.setText(userInfo.getQq());
            }
        }

        mTourDetailEt = (EditText) parent.findViewById(R.id.posting_main_detail);

        mGridView = (GridView) parent.findViewById(R.id.upload_pic_gridview);
        mGridView.requestDisallowInterceptTouchEvent(true);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new PhotoReviewAdapter(getActivity(), (ArrayList<Bitmap>) ((PostingTourActivity) getActivity()).getCurrentSelectedPics());
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long arg3) {
                if (position == ((PostingTourActivity) getActivity()).getCurrentSelectedPicNum()) {
                    //打点数据
                    Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.TOURPOST_ALBUM);

                    ((PostingTourActivity) getActivity()).albumPhoto();
                } else {
                    Fragment photoFrg = new PhotoPreviewFragment();
                    FragmentTransaction transaction = PostingTourFragment.this.getFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putInt(AlbumConstant.REVIEW_PIC_INDEX, position);
                    photoFrg.setArguments(bundle);
                    transaction.replace(R.id.upload_pic_fragment_contain, photoFrg,
                            AlbumConstant.FRAGMENT_IMAGE_DETAIL);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });
        mEndTimeLL.setOnTouchListener(this);
        mStartTimeLL.setOnTouchListener(this);
    }

    public void updateUI() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.posting_main_scroll) {
            Log.e("xuzhitao", "ScrollView onTouchEnvent()");
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Fragment calendarFrg = new PostingCalendarFragment();
            Bundle b = new Bundle();
            if (v.getId() == R.id.posting_main_start_time_ll) {
                b.putInt(AlbumConstant.EXTRA_TIME_TYPE, AlbumConstant.POSTING_TIME_TYPE_START);
            } else {
                b.putInt(AlbumConstant.EXTRA_TIME_TYPE, AlbumConstant.POSTING_TIME_TYPE_END);
            }
            calendarFrg.setArguments(b);

            FragmentTransaction transaction = PostingTourFragment.this.getFragmentManager().beginTransaction();
            transaction.replace(R.id.upload_pic_fragment_contain, calendarFrg,
                    AlbumConstant.FRAGMENT_IMAGE_DETAIL);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        return false;
    }

    private CommonToastDialog waitDialog;

    private void postTourPlan() {
        PostData postData = getPostData();
        boolean isvalidate = validateInputData(postData);
        // 对用户的输入没有校验过就返回
        if (!isvalidate) {
            return;
        }
        final MessageProtos.PostRequestInfo postTourPlanInfo = buildPostRequestInfo(postData);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Request r = RequestBuilder.buildSendPostRequest(postTourPlanInfo);
                boolean isSuccess = false;
                MessageProtos.ResponseInfo responseInfo = r.post();
                if (responseInfo != null) {
                    if (responseInfo.getErrCode() == MessageProtos.SUCCESS) {
                        // 成功
                        isSuccess = true;
                        mSelectEditView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                waitDialog.dismiss();
                                String desIdAndName = ((PostingTourActivity) getActivity()).mSelectedDistinations.get(0);
                                String desName = desIdAndName.substring(desIdAndName.indexOf("_") + 1);
                                DestinationDataManager.DestinationOrigData desData = DestinationSearchManager.getInstance(TourPalApplication.getAppContext()).findDestinationDataByName(desName);
                                if (DEBUG) {
                                    Log.d(TAG, String.format("level=%d, name=%s, grandparent name=%s", desData.mLevel,
                                            desData.mDesName,
                                            desData.mGrandparents == null ? "null" : desData.mGrandparents.get(0).mDesName));
                                }
                                if (desData.mLevel > 2) {
                                    if (desData.mGrandparents != null && desData.mGrandparents.get(0) != null) {
                                        desName = desData.mGrandparents.get(0).mDesName;
                                    }
                                }
                                PostListActivity.startActivityByDest(getActivity(), desName);
                                getActivity().finish();
                            }
                        }, 100);

                        return;
                    }
                }

                mSelectEditView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waitDialog.setIsLoading(false);
                        waitDialog.setmDialogIcon(R.drawable.icon_tip);
                        waitDialog.setDialogTitle(getString(R.string.posting_main_post_fail));
                        waitDialog.setDuration(CommonToastDialog.LENGTH_LONG);
                        waitDialog.show();
                    }
                }, 100);

            }
        }).start();
        waitDialog = new CommonToastDialog(getActivity());
        waitDialog.setIsLoading(true);
        waitDialog.setmDialogIcon(R.drawable.icon_loading);
        waitDialog.setDialogTitle(getString(R.string.posting_main_is_posting));
        waitDialog.setDuration(CommonToastDialog.LENGTH_LONG);
        waitDialog.show();
    }

    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {

        }
    };
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (TextUtils.isEmpty(text)) {
                CommonTitleBar titleBar = ((PostingTourActivity) getActivity()).mTitleBar;
                titleBar.getRightButton().setEnabled(true);
                ((TextView) titleBar.getRightButton()).setTextColor(getResources().getColor(R.color.white));
                mDestinationSearchLL.setVisibility(View.GONE);
                mContentButDestination.setVisibility(View.VISIBLE);
            } else {
                CommonTitleBar titleBar = ((PostingTourActivity) getActivity()).mTitleBar;
                titleBar.getRightButton().setEnabled(false);
                ((TextView) titleBar.getRightButton()).setTextColor(getResources().getColor(R.color.titlebar_right_text_color_white_disable));
                setSearchListViewHeight(false);
                mDestinationSearchLL.setVisibility(View.VISIBLE);
                mContentButDestination.setVisibility(View.GONE);
            }

            // 输入框有内容就进行搜索建议
            final ArrayList<DestinationDataManager.DestinationOrigData> citys = (ArrayList<DestinationDataManager.DestinationOrigData>) DestinationSearchManager
                    .getInstance(TourPalApplication.getAppContext()).search(s.toString());
            mDestinationSearchAdapter = new SearchDestinationAdapter(PostingTourFragment.this.getActivity(), citys);
            mDestinationSearchListview.setAdapter(mDestinationSearchAdapter);
        }
    };

    private void setSearchListViewHeight(boolean isRest) {
        int listHeight = 0;
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        if (!isRest) {
            int height = wm.getDefaultDisplay().getHeight();
            int topViewHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.posting_main_search_top_height);
            Rect frame = new Rect();
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            listHeight = height - topViewHeight - statusBarHeight - 2;
        }
        ViewGroup.LayoutParams lp = mDestinationSearchListview.getLayoutParams();
        lp.height = listHeight;
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mDestinationSearchListview.setLayoutParams(lp);
    }

    private OnItemClickListener mDestinationOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final DestinationDataManager.DestinationOrigData selectedDestination = mDestinationSearchAdapter.getSelectData(position);
            ArrayList<String> ds = ((PostingTourActivity) getActivity()).mSelectedDistinations;
            boolean hasAdd = false;
            for (String d : ds) {
                String[] idAndName = d.split("_");
                if (Long.valueOf(idAndName[0]) == selectedDestination.mDesId) {
                    hasAdd = true;
                    break;
                }
            }
            if (!hasAdd) {
                ds.add(selectedDestination.mDesId + "_" + selectedDestination.mDesName);
                mSelectEditView.addDestination(selectedDestination.mDesName);
            }
            mScrollView.scrollTo(0, 0);
            mSelectEditView.clearInput();
        }
    };

    private SelectEditView.CallBack mCallBack = new SelectEditView.CallBack() {
        @Override
        public void removeItem(View item, int position) {
            ((PostingTourActivity) getActivity()).mSelectedDistinations.remove(position);
        }

        @Override
        public void setViewWidth(int width) {
            mSelectEditViewWidth = width;
        }
    };

    /**
     * 返回键退出的特殊不退出处理
     *
     * @return true:退出 ， false:不退出
     */
    public boolean doOnBackWithoutFinish() {
        if (mOnResume && mDestinationSearchLL.getVisibility() == View.VISIBLE) {
            mSelectEditView.clearInput();
            return false;
        }
        return true;
    }

    private View.OnClickListener mTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox c = (CheckBox) v;
            String tag = c.getText().toString();
            if (!((PostingTourActivity)getActivity()).mSelectedTags.contains(tag)) {
                if (c.isChecked()) {
                    if (((PostingTourActivity)getActivity()).mSelectedTags.size() < TAG_SELECT_MAX) {
                        ((PostingTourActivity)getActivity()).mSelectedTags.add(tag);
                    } else {
                        c.setChecked(false);
                        Toast.makeText(PostingTourFragment.this.getActivity(), R.string.posting_main_above_max_tag_select, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                if (!c.isChecked()) {
                    ((PostingTourActivity)getActivity()).mSelectedTags.remove(tag);
                }
            }
            if (DEBUG) {
                Log.d(TAG, "tag num=" + ((PostingTourActivity)getActivity()).mSelectedTags.size());
            }
        }
    };

    /**
     * 获取发帖相关数据
     *
     * @return
     */
    private PostData getPostData() {
        PostData postData = new PostData();
        postData.mDestinationAndIds = ((PostingTourActivity) getActivity()).mSelectedDistinations;
        PostingTourActivity pta = (PostingTourActivity) getActivity();
        postData.mStartTime = pta.getStartTime();
        postData.mEndTime = pta.getEndTime();
        postData.mSelectTags = ((PostingTourActivity)getActivity()).mSelectedTags;
        postData.mWeixin = mWeixinEt.getText().toString().trim();
        postData.QQ = mQQEt.getText().toString().trim();
        postData.mPhone = mPhoneEt.getText().toString().trim();
        postData.mDetail = mTourDetailEt.getText().toString().trim();
        postData.mSelectPhotoes = pta.getCurrentSelectedPics();
        return postData;
    }

    /**
     * 校验用户的输入
     *
     * @param postData
     * @return
     */
    private boolean validateInputData(PostData postData) {
        ArrayList<String> destinations = postData.mDestinationAndIds;
        if (destinations.size() == 0) {
            Toast.makeText(this.getActivity(), R.string.posting_main_error_no_destination, Toast.LENGTH_SHORT).show();
            return false;
        }

        Calendar startTime = postData.mStartTime;
        Calendar endTime = postData.mEndTime;
        if (startTime == null || endTime == null) {
            Toast.makeText(this.getActivity(), R.string.posting_main_error_no_start_or_end_time, Toast.LENGTH_SHORT).show();
            return false;
        } else if (startTime.after(endTime)) {
            Toast.makeText(this.getActivity(), R.string.posting_main_error_invalid_time, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (postData.mSelectTags.size() == 0) {
            Toast.makeText(this.getActivity(), R.string.posting_main_error_no_select_tag, Toast.LENGTH_SHORT).show();
            return false;
        }

        String weixin = postData.mWeixin;
        String qq = postData.QQ;
        String phone = postData.mPhone;

        if (TextUtils.isEmpty(weixin) && TextUtils.isEmpty(qq) && TextUtils.isEmpty(phone)) {
            Toast.makeText(this.getActivity(), R.string.posting_main_error_no_contact, Toast.LENGTH_SHORT).show();
            return false;
        }

        String detail = postData.mDetail;

        if (TextUtils.isEmpty(detail)) {
            Toast.makeText(this.getActivity(), R.string.posting_main_error_no_detail, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Utils.isMyUserInfoHasNickname()) {
            UiUtils.showUserInfoIncompleteDlg(getActivity());
            return false;
        }

        return true;
    }

    private MessageProtos.PostRequestInfo buildPostRequestInfo(PostData postData) {
        final MessageProtos.PostRequestInfo postTourPlanInfo = new MessageProtos.PostRequestInfo();
        List<Bitmap> maps = postData.mSelectPhotoes;

        for (String d : postData.mDestinationAndIds) {
            String[] idDest = d.split("_");
            MessageProtos.DestInfo di = new MessageProtos.DestInfo();
            if (idDest.length == 2) {
                di.setDestId(Integer.valueOf(idDest[0]));
                di.setDest(idDest[1]);
            }
            postTourPlanInfo.addDestInfo(di);
        }
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        postTourPlanInfo.setStartTime(sf.format(postData.mStartTime.getTime()));
        long betweenTime = postData.mEndTime.getTimeInMillis() - postData.mStartTime.getTimeInMillis();
        int durDate = (int) (betweenTime / 1000 / 60 / 60 / 24) + 1;
        postTourPlanInfo.setDuration(durDate);

        for (String t : ((PostingTourActivity)getActivity()).mSelectedTags) {
            Log.d(TAG, "tag=" + t);
            postTourPlanInfo.addPostTag(t);
        }
        postTourPlanInfo.setWeixin(postData.mWeixin);
        postTourPlanInfo.setQq(postData.QQ);
        postTourPlanInfo.setPhone(postData.mPhone);
        postTourPlanInfo.setContent(postData.mDetail);

        for (Bitmap m : maps) {
            Bitmap compressBitmap = AlbumUtils.getScaledShareBitmap(m);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            postTourPlanInfo.addImgs(ByteStringMicro.copyFrom(baos.toByteArray()));
        }

        return postTourPlanInfo;
    }

    private class PostData{
        ArrayList<String> mDestinationAndIds;
        Calendar mStartTime;
        Calendar mEndTime;
        ArrayList<String> mSelectTags;
        String mWeixin;
        String QQ;
        String mPhone;
        String mDetail;
        List<Bitmap> mSelectPhotoes;
    }

    public boolean hasInput() {
        ArrayList<String> destinationAndIds = ((PostingTourActivity) getActivity()).mSelectedDistinations;

        if (destinationAndIds.size() > 0) {
            return true;
        }
        String detail = mTourDetailEt.getText().toString().trim();
        if (!TextUtils.isEmpty(detail)) {
            return true;
        }
        PostingTourActivity pta = (PostingTourActivity) getActivity();
        if (pta.getCurrentSelectedPics().size() > 0) {
            return true;
        }

        return false;
    }
}
