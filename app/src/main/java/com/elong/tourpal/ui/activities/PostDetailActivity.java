package com.elong.tourpal.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.share.ShareManager;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.supports.JoinGroupAdapter;
import com.elong.tourpal.ui.supports.UiUtils;
import com.elong.tourpal.ui.views.CommonDialog;
import com.elong.tourpal.ui.views.CommonToastDialog;
import com.elong.tourpal.ui.views.DropDownDialog;
import com.elong.tourpal.ui.views.RelativeGridLayout;
import com.elong.tourpal.ui.views.TagsContainer;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.Utils;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostDetailActivity extends ActivityBase {

    private static final String TAG = "PostDetailActivity";
    private static final int WHAT_SEND_WANNA_JOIN = 102;
    private static final int WHAT_DELETE_POST = 103;//TODO 删完要finish
    private static final int WHAT_GET_POST_DETAIL = 105;
    private static final int WHAT_REPORT_POST = 106;
    private static final int WHAT_CLOSE_POST = 107;

    private static final int WHAT_SHOW_DELETE_POST_RESULT = 202;
    private static final int WHAT_REFRESH_POST_DETAIL = 204;

    private static final int MENU_ID_REPORT = 1;
    private static final int MENU_ID_CLOSE = 2;
    private static final int MENU_ID_DELETE = 3;

    private CustomImageView mCivAvatar;
    private ImageView mIvStatus;
    private TextView mTvNickName;
    private TextView mTvSexAndAge;
    private TextView mTvCreateTime;
    private TextView mTvDest;
    private TextView mTvDuration;
    private TextView mTvContactInfo;
    private TextView mTvContent;
    private RelativeGridLayout mRglImgs;
    private TextView mTvLocation;
    private TextView mTvWannaJoinNum;
    private TextView mTvUv;
    private TagsContainer mTcTags;
    private RelativeLayout mRlTags;
    private TextView mTvWannaJoinGroupTitle;
    private RelativeLayout mRlWannaJoinGroup;
    private RelativeLayout mRlWannaJoinGroupEmpty;
    private GridView mGvWannaJoinGroup;
    private JoinGroupAdapter mJoinGroupAdapter;

    private Button mBtnWannaJoin;
    private Button mBtnShare;
    private RelativeLayout mRlBottomButtons;

    private CommonToastDialog mProgressDialog = null;
    private DropDownDialog mDropDownMenu = null;

    private MessageProtos.PostResponseInfo mPostInfo;
    private String mPostId;

    private HandlerThread mHandlerThread;
    private ExecHandler mExecHandler;
    private UIHandler mUIHandler;

    public static void startActivityForResult(Activity context, MessageProtos.PostResponseInfo postInfo, int reqCode) {
        if (postInfo != null){
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra(Extras.EXTRA_POST_INFO, postInfo.toByteArray());
            context.startActivityForResult(intent, reqCode);
        }
    }

    public static void startActivity(Context context, String postId) {
        if (!TextUtils.isEmpty(postId)) {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra(Extras.EXTRA_POST_ID, postId);
            context.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        resolveIntent();
        if (mPostId != null){
            initHandlerAndThread();
            initViews();
        } else {
            setResult(false);
            finish();
        }
    }

    private void initHandlerAndThread() {
        mHandlerThread = new HandlerThread("plaThread");
        mHandlerThread.start();
        mExecHandler = new ExecHandler(this, mHandlerThread.getLooper());
        mUIHandler = new UIHandler();
    }

    private void resolveIntent(){
        Intent intent = getIntent();
        if (intent != null){
            mPostId = intent.getStringExtra(Extras.EXTRA_POST_ID);
            byte[] postInfo = intent.getByteArrayExtra(Extras.EXTRA_POST_INFO);
            if (postInfo != null && postInfo.length > 0){
                try {
                    mPostInfo = MessageProtos.PostResponseInfo.parseFrom(postInfo);
                    if (mPostInfo != null) {
                        mPostId = mPostInfo.getId();
                    }
                } catch (InvalidProtocolBufferMicroException e) {
                    if (Env.DEBUG){
                        Log.e(TAG, "e:", e);
                    }
                }
            }
        }
    }

    private void initTitle(){
        setTitleText(R.string.pd_title);
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(false);
                finish();
            }
        });
        setTitleRightBtn(R.drawable.icon_title_more);
        setTitleRightBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDropDownMenu != null && mPostInfo != null) {
                    mDropDownMenu.removeAllItems();
                    if (mPostInfo.getIsMy()) {
                        if (mPostInfo.getStatus() == MessageProtos.OPENED) {
                            mDropDownMenu.addItem(MENU_ID_CLOSE, R.string.pd_menu_close_post, R.mipmap.ic_menu_close);
                        }
                        mDropDownMenu.addItem(MENU_ID_DELETE, R.string.pd_menu_delete_post, R.mipmap.ic_menu_delete);
                    } else {
                        mDropDownMenu.addItem(MENU_ID_REPORT, R.string.pd_menu_report_post, R.mipmap.ic_menu_report);
                    }
                    if (mDropDownMenu.isShowing()) {
                        mDropDownMenu.dismiss();
                    } else {
                        mDropDownMenu.show(mTitleBarRightContainer);
                    }
                }
            }
        });
    }

    private void initViews() {
        initTitle();

        mTcTags = (TagsContainer) findViewById(R.id.pd_tc_tags);
        mRlTags = (RelativeLayout) findViewById(R.id.pd_rl_tags);
        mCivAvatar = (CustomImageView) findViewById(R.id.pd_iv_avatar);
        mIvStatus = (ImageView) findViewById(R.id.pd_iv_status);
        mTvNickName = (TextView) findViewById(R.id.pd_tv_nick_name);
        mTvSexAndAge = (TextView) findViewById(R.id.pd_tv_sex_and_age);
        mTvCreateTime = (TextView) findViewById(R.id.pd_tv_create_time);
        mTvDest = (TextView) findViewById(R.id.pd_tv_dest);
        mTvDuration = (TextView) findViewById(R.id.pd_tv_duration);
        mTvContactInfo = (TextView) findViewById(R.id.pd_tv_contact_info);
        mTvContent = (TextView) findViewById(R.id.pd_tv_content);
        mRglImgs = (RelativeGridLayout) findViewById(R.id.pd_rgl_imgs);
        mTvLocation = (TextView) findViewById(R.id.pd_tv_location);
        mTvWannaJoinNum = (TextView) findViewById(R.id.pd_tv_wanna_join_num);
        mTvUv = (TextView) findViewById(R.id.pd_tv_uv);
        mTvWannaJoinGroupTitle = (TextView) findViewById(R.id.pd_tv_wanna_join_group_title);
        mRlWannaJoinGroupEmpty = (RelativeLayout) findViewById(R.id.pd_rl_wanna_join_group_empty);
        mRlWannaJoinGroup = (RelativeLayout) findViewById(R.id.pd_rl_wanna_join_group);
        mGvWannaJoinGroup = (GridView) findViewById(R.id.pd_gv_wanna_join_group);
        mBtnWannaJoin = (Button) findViewById(R.id.bottom_btn_left);
        mBtnShare = (Button) findViewById(R.id.bottom_btn_right);
        mRlBottomButtons = (RelativeLayout) findViewById(R.id.pd_rl_bottom_buttons);
        mGvWannaJoinGroup.setStretchMode(GridView.NO_STRETCH);
        mJoinGroupAdapter = new JoinGroupAdapter(this);
        mGvWannaJoinGroup.setAdapter(mJoinGroupAdapter);
//        mGvWannaJoinGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (mPostInfo != null) {
//                    WannaJoinListActivity.startActivityByPostId(PostDetailActivity.this, mPostInfo.getId());
//                }
//            }
//        });
        mGvWannaJoinGroup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mGvWannaJoinGroup.setBackgroundResource(R.color.white_pressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mPostInfo != null) {
                            if (TourPalApplication.getInstance().hasLogin()) {
                                if (mPostInfo != null) {
                                    boolean isMy = mPostInfo.getIsMy();
                                    if (mPostInfo.getIsLiked() || isMy) {
                                        String ownerName = "";
                                        if (!isMy) {
                                            MessageProtos.UserInfo ownerInfo = mPostInfo.getUserInfo();
                                            if (ownerInfo != null) {
                                                ownerName = ownerInfo.getNickName();
                                            }
                                        }
                                        WannaJoinListActivity.startActivityByPostId(PostDetailActivity.this, mPostInfo.getId(), mPostInfo.getDest(), isMy, ownerName);
                                    } else {
                                        CommonDialog dlg = new CommonDialog(PostDetailActivity.this);
                                        dlg.setTitle(R.string.common_tips);
                                        dlg.setMessage(R.string.pd_toast_join_before_view_join_group);
                                        dlg.setLeftBtnText(R.string.common_cancel);
                                        dlg.setRightBtnText(R.string.pd_wanna_join);
                                        dlg.setRightBtnOnclickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                sendWannaJoin(mPostInfo.getId(), 1);
                                            }
                                        });
                                        dlg.show();
                                    }
                                }
                            } else {
                                //未登录跳转到登录界面
                                Intent intent = new Intent(PostDetailActivity.this, LoginWebviewActivity.class);
                                startActivity(intent);
                            }
                        }
                        mGvWannaJoinGroup.setBackgroundResource(R.color.white);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        mGvWannaJoinGroup.setBackgroundResource(R.color.white);
                        break;
                }
                return false;
            }
        });
        showPostDetail();

        mProgressDialog = new CommonToastDialog(this);

        initDropDownMenu();

        //异步获取帖子详情
        getPostDetail();
    }

    private void initDropDownMenu() {
        mDropDownMenu = new DropDownDialog(this);
        mDropDownMenu.setCallback(new DropDownDialog.DropDownDialogCallback() {
            @Override
            public void onItemClick(int itemId, String desc) {
                if (!TourPalApplication.getInstance().hasLogin()) {
                    CommonDialog dlg = new CommonDialog(PostDetailActivity.this);
                    dlg.setTitle(R.string.common_tips);
                    dlg.setMessage(R.string.dlg_not_login_message);
                    dlg.setLeftBtnText(R.string.common_cancel);
                    dlg.setRightBtnText(R.string.common_go_2_login);
                    dlg.setRightBtnOnclickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(PostDetailActivity.this, LoginWebviewActivity.class);
                            startActivity(intent);
                        }
                    });
                    dlg.show();
                    return;
                }
                switch (itemId) {
                    case MENU_ID_CLOSE:
                        closePost();
                        break;
                    case MENU_ID_DELETE:
                        deletePostByPostId(mPostInfo.getId());
                        break;
                    case MENU_ID_REPORT:
                        reportPost();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onDismiss() {
                //do nothing
            }
        });
    }

    private void initBottomButtons() {
        int iconPadding = getResources().getDimensionPixelSize(R.dimen.bottom_btn_ic_padding);
        //求同行
        mBtnWannaJoin.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        mBtnWannaJoin.setCompoundDrawablePadding(iconPadding);
        switch (mPostInfo.getStatus()) {
            case MessageProtos.CLOSED:
                mRlBottomButtons.setVisibility(View.GONE);
                break;
            case MessageProtos.OPENED:
                mRlBottomButtons.setVisibility(View.VISIBLE);
                boolean isMy = mPostInfo.getIsMy();
                mBtnWannaJoin.setEnabled(!isMy);
                if (isMy) {
                    mBtnWannaJoin.setTextColor(getResources().getColor(R.color.pd_btn_join_disabled_text));
                    mBtnWannaJoin.setText(R.string.pd_wanna_join);
                    mBtnWannaJoin.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_join_disabled, 0, 0, 0);
                } else {
                    mBtnWannaJoin.setTextColor(getResources().getColor(R.color.pi_btn_wanna_btn_line));
                    if (mPostInfo.getIsLiked()) {
                        mBtnWannaJoin.setText(R.string.pd_cancel_wanna_join);
                        mBtnWannaJoin.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_joined, 0, 0, 0);
                    } else {
                        mBtnWannaJoin.setText(R.string.pd_wanna_join);
                        mBtnWannaJoin.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_join, 0, 0, 0);
                    }
                }
                break;
        }
        mBtnWannaJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先检查是否登录
                if (TourPalApplication.getInstance().hasLogin()) {
                    if (mPostInfo.getIsLiked()) {
                        //取消求同行
                        showUnJoinTipsDialog();
                    } else {
                        //求同行
                        SharedPref pref = SharedPref.getInstance();
                        if (!pref.hasShowJoinTipDlg()) {
                            showJoinTipsDialog();
                            pref.setHasShowJoinTipDlg();
                        } else {
                            sendWannaJoin(mPostInfo.getId(), 1);
                        }
                    }
                } else {
                    //未登录跳转到登录界面
                    Intent intent = new Intent(PostDetailActivity.this, LoginWebviewActivity.class);
                    startActivity(intent);
                }
            }
        });

        //分享
        mBtnShare.setText(R.string.pd_share);
        mBtnShare.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        mBtnShare.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_share, 0, 0, 0);
        mBtnShare.setCompoundDrawablePadding(iconPadding);
        mBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageProtos.PostDetailInfo postDetailInfo = mPostInfo.getPostDetailInfo();
                if (postDetailInfo != null) {
                    MessageProtos.ShareInfo shareInfo = postDetailInfo.getShareInfo();
                    if (!TextUtils.isEmpty(shareInfo.getShareDesc()) && !TextUtils.isEmpty(shareInfo.getShareLink()) && !TextUtils.isEmpty(shareInfo.getShareIco())) {
                        ShareManager.startShare(PostDetailActivity.this, shareInfo);
                    } else {
                        Toast.makeText(PostDetailActivity.this, "分享失败！", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void showJoinTipsDialog() {
        CommonDialog joinTipsDlg = new CommonDialog(PostDetailActivity.this);
        joinTipsDlg.setTitle(R.string.common_tips);
        joinTipsDlg.setMessage(R.string.pd_join_tips_dlg_message);
        joinTipsDlg.setLeftBtnText(R.string.common_modify);
        joinTipsDlg.setLeftBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailActivity.this, EditContactInfoActivity.class);
                startActivity(intent);
            }
        });
        joinTipsDlg.setRightBtnText(R.string.common_confirm);
        joinTipsDlg.setRightBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWannaJoin(mPostInfo.getId(), 1);
            }
        });
        joinTipsDlg.show();
    }

    private void showUnJoinTipsDialog() {
        CommonDialog dialog = new CommonDialog(PostDetailActivity.this);
        dialog.setTitle(R.string.common_tips);
        dialog.setMessage(R.string.pi_cancel_wanna_join_dialog_content);
        dialog.setRightBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWannaJoin(mPostInfo.getId(), 0);
            }
        });
        dialog.show();
    }

    private void showPostDetail() {
        if (mPostInfo == null) {
            return;
        }
        final MessageProtos.UserInfo uInfo = mPostInfo.getUserInfo();
        MessageProtos.PostDetailInfo postDetailInfo = mPostInfo.getPostDetailInfo();
        String avatarThumbUrl = uInfo.getHeadImgUrl();
        if (!TextUtils.isEmpty(avatarThumbUrl)) {
            mCivAvatar.setImageUrl(avatarThumbUrl);
            mCivAvatar.loadImage();
        } else {
            mCivAvatar.setImageResource(Utils.getAvatarId(Integer.parseInt(uInfo.getId())));
        }
        mCivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Statistics.log(PostDetailActivity.this, StatisticsEnv.DESTINATION_HEADICON);

                PersonalCenterActivity.startActivity(PostDetailActivity.this, new String(uInfo.toByteArray()));
            }
        });
        mTvNickName.setText(uInfo.getNickName());
        mTvSexAndAge.setText(String.valueOf(uInfo.getAge()));
        if (uInfo.getSex() == 1) {
            //男
            mTvSexAndAge.setBackgroundResource(R.drawable.bg_male);
            mTvSexAndAge.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_male, 0, 0, 0);
        } else if (uInfo.getSex() == 2) {
            //女
            mTvSexAndAge.setBackgroundResource(R.drawable.bg_female);
            mTvSexAndAge.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_female, 0, 0, 0);
        }
        try {
            long createdTime = Long.parseLong(mPostInfo.getCreatedTime());
            mTvCreateTime.setText(Utils.getFormattedTime(createdTime * 1000));
        } catch (Exception e){
            mTvCreateTime.setText("");
        }
        mTvUv.setText(getString(R.string.pi_uv_format, mPostInfo.getPv()));
        mTvDest.setText(mPostInfo.getDest().replace(",", "、"));
        mTvDuration.setText(getString(R.string.pi_duration_format, mPostInfo.getStartTime(), mPostInfo.getDays()));
        mTvContent.setText(mPostInfo.getContent());
        if (!TextUtils.isEmpty(mPostInfo.getPostPlace())) {
            mTvLocation.setText(mPostInfo.getPostPlace());
            mTvLocation.setVisibility(View.VISIBLE);
        } else {
            mTvLocation.setVisibility(View.GONE);
        }
        int likeNum = mPostInfo.getLikeNum();
        mTvWannaJoinNum.setText(getString(R.string.pi_wanna_join_num_format, String.valueOf(likeNum)));

        switch (mPostInfo.getStatus()) {
            case MessageProtos.OPENED:
                mIvStatus.setImageResource(R.mipmap.ic_post_status_recruiting);
                break;
            case MessageProtos.CLOSED:
                mIvStatus.setImageResource(R.mipmap.ic_post_status_finished);
                break;
        }

        //标签
        mTcTags.removeAllTags();
        List<String> tags = mPostInfo.getPostTagList();
        if (tags != null && tags.size() > 0) {
            mRlTags.setVisibility(View.VISIBLE);
            for (String tag : tags) {
                if (tag != null) {
                    mTcTags.addItemView(tag);
                }
            }
        } else {
            mRlTags.setVisibility(View.GONE);
        }

        //联系方式
        if (mPostInfo.getIsLiked() || mPostInfo.getIsMy()) {
            MessageProtos.UserInfo owner = mPostInfo.getUserInfo();
            if (owner != null) {
                //显示联系方式
                String wechat = getString(R.string.pi_contact_info_format, getString(R.string.pi_contact_info_type_wechat), mPostInfo.getWeixin());
                String qq = getString(R.string.pi_contact_info_format, getString(R.string.pi_contact_info_type_qq), mPostInfo.getQq());
                String phone = getString(R.string.pi_contact_info_format, getString(R.string.pi_contact_info_type_phone), mPostInfo.getPhone());
                StringBuilder sbContactInfo = new StringBuilder();
                if (!TextUtils.isEmpty(mPostInfo.getWeixin())) {
                    sbContactInfo.append(wechat);
                    sbContactInfo.append("\n");
                }
                if (!TextUtils.isEmpty(mPostInfo.getQq())) {
                    sbContactInfo.append(qq);
                    sbContactInfo.append("\n");
                }
                if (!TextUtils.isEmpty(mPostInfo.getPhone())) {
                    sbContactInfo.append(phone);
                    sbContactInfo.append("\n");
                }
                sbContactInfo.replace(sbContactInfo.length() - 1, sbContactInfo.length(), "");
                mTvContactInfo.setText(sbContactInfo.toString());
                mTvContactInfo.setTextColor(getResources().getColor(R.color.pi_contact_info));
                mTvContactInfo.setBackgroundResource(R.drawable.white_clickable_bg);
            }
        } else {
            mTvContactInfo.setText(R.string.pd_tips_contact_info_not_in_wanna_join_state);
            mTvContactInfo.setTextColor(getResources().getColor(R.color.pi_contact_info_hide));
            mTvContactInfo.setBackgroundResource(R.drawable.bg_contact_info_invisible);
        }
        mTvContactInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPostInfo.getIsLiked() || mPostInfo.getIsMy()) {
                    UiUtils.showHandleContactInfoDialog(PostDetailActivity.this, mPostInfo.getWeixin(), mPostInfo.getQq(), mPostInfo.getPhone());
                }
            }
        });

        //同行小组
        mRlWannaJoinGroup.setVisibility(View.GONE);
        mRlWannaJoinGroupEmpty.setVisibility(View.VISIBLE);
        if (postDetailInfo != null) {
            List<MessageProtos.LikedMessage> likedMessageList = postDetailInfo.getLikedMessageList();
            if (likedMessageList != null) {
                mTvWannaJoinGroupTitle.setText(getString(R.string.pd_wanna_join_group_title_format, mPostInfo.getLikeNum()));
                if (likedMessageList.size() > 0) {
                    mRlWannaJoinGroup.setVisibility(View.VISIBLE);
                    mRlWannaJoinGroupEmpty.setVisibility(View.GONE);

                    mGvWannaJoinGroup.setNumColumns(likedMessageList.size());
                    mJoinGroupAdapter.setData(likedMessageList);
                    mJoinGroupAdapter.notifyDataSetChanged();
                }
            }
        }

        //加载图片
        final ArrayList<String> previewUrls = new ArrayList<>();
        final ArrayList<String> thumbUrls = new ArrayList<>();
        int imgCount = mPostInfo.getImgsCount();
        for (int i = 0; i < imgCount; i++) {
            MessageProtos.PostImg postImg = mPostInfo.getImgs(i);
            if (postImg != null) {
                previewUrls.add(postImg.getPreview());
                thumbUrls.add(postImg.getThumb());
            }
        }
        if (imgCount > 0) {
            mRglImgs.setVisibility(View.VISIBLE);
            for (int i = 0; i < imgCount; i++) {
                MessageProtos.PostImg postImg = mPostInfo.getImgs(i);
                if (postImg != null) {
                    CustomImageView civ = (CustomImageView) mRglImgs.getChildView(i);
                    if (civ == null) {
                        civ = new CustomImageView(this);
                        civ.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        mRglImgs.addChildView(civ);
                    } else {
                        civ.setVisibility(View.VISIBLE);
                    }
                    civ.setImageUrl(postImg.getThumb());
                    civ.loadImage();
                    final int idx = i;
                    civ.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Statistics.log(PostDetailActivity.this, StatisticsEnv.DESTINATION_PHOTO);

                            GalleryActivity.startActivity(PostDetailActivity.this, idx, previewUrls, thumbUrls);
                        }
                    });
                }
                mRglImgs.resetChildViews(i + 1);
            }
        } else {
            mRglImgs.setVisibility(View.GONE);
        }

        initBottomButtons();
    }

    /**
     * 根据帖子id删除帖子
     * @param postId 帖子id
     */
    private void deletePostByPostId(String postId) {
        mProgressDialog.setDialogTitle(getString(R.string.pl_toast_deleting));
        mProgressDialog.show();
        Message msg = mExecHandler.obtainMessage(WHAT_DELETE_POST);
        Bundle data = new Bundle();
        data.putString("postId", postId);
        msg.setData(data);
        mExecHandler.sendMessage(msg);
    }

    /**
     * 发送求同行/取消同行
     * @param postId 帖子id
     * @param state 0是要取消求同行，1是要求同行
     */
    private void sendWannaJoin(String postId, int state) {
        //先置状态
        mPostInfo.setIsLiked(state == 1);
        showPostDetail();
        //异步请求
        Message msg = mExecHandler.obtainMessage(WHAT_SEND_WANNA_JOIN);
        Bundle data = new Bundle();
        data.putString("postId", postId);
        data.putInt("state", state);
        msg.setData(data);
        mExecHandler.sendMessage(msg);
    }

    /**
     * 获取帖子详情
     */
    private void getPostDetail() {
        if (!mExecHandler.hasMessages(WHAT_GET_POST_DETAIL)) {
            mExecHandler.sendEmptyMessage(WHAT_GET_POST_DETAIL);
        }
    }

    /**
     * 举报帖子
     */
    private void reportPost() {
        if (!mExecHandler.hasMessages(WHAT_REPORT_POST)) {
            mExecHandler.sendEmptyMessage(WHAT_REPORT_POST);
        }
    }

    private void closePost() {
        if (!mExecHandler.hasMessages(WHAT_REPORT_POST)) {
            mExecHandler.sendEmptyMessage(WHAT_CLOSE_POST);
        }
    }

    public static class ExecHandler extends Handler {

        WeakReference<PostDetailActivity> mContext;

        public ExecHandler(PostDetailActivity context, Looper looper) {
            super(looper);
            mContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            final PostDetailActivity context = mContext.get();
            if (context != null) {
                Bundle data = msg.getData();
                switch (msg.what) {
                    case WHAT_SEND_WANNA_JOIN:
                        int reqState = data.getInt("state");
                        String postId = data.getString("postId");
                        if (!Utils.isNetworkConnected(context)) {
                            Toast.makeText(context, R.string.network_disable, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        if (!TextUtils.isEmpty(postId)) {
                            Request sendJoinReq = RequestBuilder.buildSendWannaJoinRequest(postId, reqState);
                            if (sendJoinReq != null) {
                                MessageProtos.ResponseInfo respInfo = sendJoinReq.get();
                                if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                    MessageProtos.WannaJoinResponse wjr = respInfo.getWannaJoinResponse();
                                    if (wjr != null) {
                                        if (!hasMessages(WHAT_SEND_WANNA_JOIN)) {
                                            //当点击了多次求同行和取消求同行，这个时候只在最后一个求同行请求返回后才置界面的状态，否则按钮状态会乱跳
                                            int num = wjr.getWannaJoinNum();
                                            int state = wjr.getWannaJoinState();
                                            MessageProtos.PostResponseInfo pri = context.mPostInfo;
                                            if (pri != null) {
                                                pri.setLikeNum(num);
                                                pri.setIsLiked(state == 1);//1为已点赞
                                                context.mUIHandler.sendEmptyMessage(WHAT_REFRESH_POST_DETAIL);
                                                context.getPostDetail();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case WHAT_DELETE_POST:
                        String delPostId = data.getString("postId");
                        Message showResultMsg = context.mUIHandler.obtainMessage(WHAT_SHOW_DELETE_POST_RESULT);
                        showResultMsg.arg1 = 1;//默认失败
                        if (!Utils.isNetworkConnected(context)) {
                            Toast.makeText(context, R.string.network_disable, Toast.LENGTH_SHORT).show();
                        } else {
                            if (!TextUtils.isEmpty(delPostId)) {
                                Request request = RequestBuilder.buildDeletePostRequest(delPostId);
                                if (request != null) {
                                    MessageProtos.ResponseInfo respInfo = request.get();
                                    if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                        showResultMsg.arg1 = 0;
                                    }
                                    showResultMsg.setData(data);
                                }
                            }
                        }
                        context.mUIHandler.sendMessage(showResultMsg);
                        break;
                    case WHAT_GET_POST_DETAIL:
                        Request getDetailRequest = RequestBuilder.buildGetPostDetailRequest(context.mPostId);
                        if (getDetailRequest != null) {
                            MessageProtos.ResponseInfo respInfo = getDetailRequest.get();
                            if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                MessageProtos.PostResponseInfo postResponseInfo = respInfo.getPostInfo();
                                if (postResponseInfo != null) {
                                    context.mPostInfo = postResponseInfo;
                                    context.mUIHandler.sendEmptyMessage(WHAT_REFRESH_POST_DETAIL);
                                }
                            }
                        }
                        break;
                    case WHAT_REPORT_POST:
                        Request reportRequest = RequestBuilder.buildReportPostRequest(context.mPostInfo.getId());
                        if (reportRequest != null) {
                            reportRequest.get();
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, R.string.pd_report_succeed, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;
                    case WHAT_CLOSE_POST:
                        Request closeRequest = RequestBuilder.buildClosePostRequest(context.mPostInfo.getId());
                        if (closeRequest != null) {
                            MessageProtos.ResponseInfo respInfo = closeRequest.get();
                            if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                context.mPostInfo.setStatus(MessageProtos.CLOSED);
                                context.mUIHandler.sendEmptyMessage(WHAT_REFRESH_POST_DETAIL);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what) {
                case WHAT_SHOW_DELETE_POST_RESULT:
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        if (Env.DEBUG) {
                            Log.e(TAG, "e:", e);
                        }
                    }
                    boolean deleteSuccess = msg.arg1 == 0;
                    String deletePostId = data.getString("postId");
                    mProgressDialog.dismiss();
                    if (deleteSuccess) {
                        Toast.makeText(PostDetailActivity.this, R.string.pl_delete_succeed, Toast.LENGTH_SHORT).show();
                        setResult(true);
                        finish();
                    } else {
                        Toast.makeText(PostDetailActivity.this, R.string.pl_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case WHAT_REFRESH_POST_DETAIL:
                    showPostDetail();
                    break;
                default:
                    break;
            }
        }
    }

    private void setResult(boolean isDelete) {
        Intent intent = new Intent();
        if (isDelete) {
            intent.putExtra(Extras.EXTRA_IS_POST_DELETE, isDelete);
        } else {
            if (mPostInfo != null) {
                intent.putExtra(Extras.EXTRA_POST_INFO, mPostInfo.toByteArray());
            }
        }
        setResult(RESULT_OK, intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(false);
        }
        return super.onKeyDown(keyCode, event);
    }

}
