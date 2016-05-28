package com.elong.tourpal.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.activities.AlbumActivity;
import com.elong.tourpal.ui.activities.EditUserInfoActivity;
import com.elong.tourpal.ui.activities.LoginWebviewActivity;
import com.elong.tourpal.ui.activities.PersonalCenterActivity;
import com.elong.tourpal.ui.activities.PostListActivity;
import com.elong.tourpal.ui.activities.SettingsActivity;
import com.elong.tourpal.ui.views.CommonSettingItem;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.Utils;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;

/**
 * PersonalCenterFragment
 *
 * 个人中心fragment
 */
public class PersonalCenterFragment extends FragmentBase4Pager{
    private static final String TAG = PersonalCenterActivity.class.getSimpleName();
    private static final String ARG_PAGER_INDEX = "PAGER_INDEX";
    private static final String ARG_IS_MY = "IS_MY";
    private static final String ARG_USER_INFO = "USER_INFO";

    private Button mBtnLogin;
    private LinearLayout mLlUserInfo;
    private TextView mTvNickName;
    private TextView mTvSexAndAge;
    private CustomImageView mCivAvatar;
    private CommonSettingItem mPciPersonalInfo;
    private CommonSettingItem mPciPersonalPosts;
    private CommonSettingItem mPciPersonalJoined;
    private CommonSettingItem mPciSettings;

    private MyUserInfoChangeReceiver mMyUserInfoChangeReceiver = null;

    private boolean mIsMy = true;

    private MessageProtos.UserInfo mOthersUserInfo = null;

    /**
     * 创建一个我的个人中心的实例
     * @param pagerIndex 在mainTab中的idx
     * @return PersonalCenterFragment
     */
    public static PersonalCenterFragment newInstance(int pagerIndex) {
        PersonalCenterFragment fragment = new PersonalCenterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGER_INDEX, pagerIndex);
        args.putBoolean(ARG_IS_MY, true);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 创建一个别人个人中心的实例
     *
     * @param userInfo MessageProtos.UserInfo 序列化后的字符串
     * @return PersonalCenterFragment
     */
    public static PersonalCenterFragment newInstance(String userInfo) {
        PersonalCenterFragment fragment = new PersonalCenterFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_MY, false);
        args.putString(ARG_USER_INFO, userInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //打点数据
        Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.USERCENTER_ENTER);

        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            setPageIndex(arguments.getInt(ARG_PAGER_INDEX, 0));
            mIsMy = arguments.getBoolean(ARG_IS_MY);
            String uInfoStr = arguments.getString(ARG_USER_INFO);
            if (!TextUtils.isEmpty(uInfoStr)){
                try {
                    mOthersUserInfo = MessageProtos.UserInfo.parseFrom(uInfoStr.getBytes());
                } catch (InvalidProtocolBufferMicroException e) {
                    if (Env.DEBUG){
                        Log.e(TAG, "e:", e);
                    }
                }
            }
            if (!mIsMy && mOthersUserInfo == null){
                //他人的个人中心，若没带进来userInfo，则直接退出
                getActivity().finish();
            }
        }
        initReceivers();
    }

    private void initReceivers() {
        if (mIsMy){
            mMyUserInfoChangeReceiver = new MyUserInfoChangeReceiver();
            IntentFilter userInfoChangeFilter = new IntentFilter(Actions.ACTION_UPDATE_MY_USER_INFO);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMyUserInfoChangeReceiver, userInfoChangeFilter);
        }
    }

    @Override
    public void onDestroy() {
        if (mMyUserInfoChangeReceiver != null){
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMyUserInfoChangeReceiver);
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_personal_center, container, false);
        initViews(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initViews(View rootView){
        mBtnLogin = (Button) rootView.findViewById(R.id.fpc_btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打点数据
                Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.USERCENTER_LOGIN_PRESS);

                Intent intent = new Intent(getActivity(), LoginWebviewActivity.class);
                startActivity(intent);
            }
        });
        mTvNickName = (TextView) rootView.findViewById(R.id.fpc_tv_nick_name);
        mTvSexAndAge = (TextView) rootView.findViewById(R.id.fpc_tv_sex_and_age);
        mLlUserInfo = (LinearLayout) rootView.findViewById(R.id.fpc_ll_user_info);
        mCivAvatar = (CustomImageView) rootView.findViewById(R.id.fpc_iv_avatar);

        OnPersonalCenterItemClickListener onPciClickListener = new OnPersonalCenterItemClickListener();
        mPciPersonalInfo = (CommonSettingItem) rootView.findViewById(R.id.fpc_item_personal_info);
        mPciPersonalInfo.setIconLeft(R.mipmap.ic_personal_center_profile);
        mPciPersonalInfo.setIconRight(R.mipmap.ic_right_arrow);
        mPciPersonalInfo.setOnClickListener(onPciClickListener);
        mPciPersonalInfo.setDividerTopEnable(true);

        mPciPersonalPosts = (CommonSettingItem) rootView.findViewById(R.id.fpc_item_personal_posts);
        mPciPersonalPosts.setOnClickListener(onPciClickListener);
        mPciPersonalPosts.setIconLeft(R.mipmap.ic_personal_center_post);
        mPciPersonalPosts.setIconRight(R.mipmap.ic_right_arrow);

        mPciPersonalJoined = (CommonSettingItem) rootView.findViewById(R.id.fpc_item_personal_joined);
        mPciPersonalJoined.setOnClickListener(onPciClickListener);
        mPciPersonalJoined.setIconLeft(R.mipmap.ic_personal_center_joined);
        mPciPersonalJoined.setIconRight(R.mipmap.ic_right_arrow);

        mPciSettings = (CommonSettingItem) rootView.findViewById(R.id.fpc_item_settings);
        mPciSettings.setIconLeft(R.mipmap.ic_personal_center_setting);
        mPciSettings.setIconRight(R.mipmap.ic_right_arrow);
        mPciSettings.setOnClickListener(onPciClickListener);
        mPciSettings.setFullBottomDivider(true);

        if (mIsMy) {
            mCivAvatar.setOnClickListener(onPciClickListener);
            //我的个人主页
            mPciPersonalInfo.setContentText(R.string.fpc_item_content_personal_info);
            mPciPersonalPosts.setContentText(R.string.fpc_item_content_personal_posts);
            mPciPersonalJoined.setContentText(R.string.fpc_item_content_personal_joined);
            mPciSettings.setContentText(R.string.fpc_item_settings);
            mTvSexAndAge.setVisibility(View.GONE);
            refreshLoginInfo();
        } else {
            //别人的个人主页
            String avatarThumbUrl = mOthersUserInfo.getHeadImgUrl();
            if (!TextUtils.isEmpty(avatarThumbUrl)) {
                mCivAvatar.setImageUrl(avatarThumbUrl);
                mCivAvatar.loadImage();
            } else {
                mCivAvatar.setImageResource(Utils.getAvatarId(Integer.parseInt(mOthersUserInfo.getId())));
            }
            mPciPersonalInfo.setVisibility(View.GONE);
            mPciPersonalJoined.setVisibility(View.GONE);
            mPciSettings.setVisibility(View.GONE);
            mPciPersonalPosts.setContentText(R.string.fpc_item_content_personal_posts_others);
            mPciPersonalPosts.setDividerTopEnable(true);
            mPciPersonalPosts.setFullBottomDivider(true);
            mBtnLogin.setVisibility(View.GONE);
            mLlUserInfo.setVisibility(View.VISIBLE);
            mTvNickName.setText(mOthersUserInfo.getNickName());
            if (mOthersUserInfo.getSex() == 1){
                mTvSexAndAge.setBackgroundResource(R.drawable.bg_male);
            } else {
                mTvSexAndAge.setBackgroundResource(R.drawable.bg_female);
            }
            mTvSexAndAge.setText(String.valueOf(mOthersUserInfo.getAge()));
        }
    }

    private void refreshLoginInfo() {
        boolean hasLogin = TourPalApplication.getInstance().hasLogin();
        if (hasLogin){
            mBtnLogin.setVisibility(View.GONE);
            mLlUserInfo.setVisibility(View.VISIBLE);
            MessageProtos.UserInfo myUserInfo = SharedPref.getInstance().getMyUserInfo();
            if (myUserInfo != null){
                String nickName = myUserInfo.getNickName();
                if (nickName == null){
                    nickName = "";
                }
                mTvNickName.setText(nickName);
                String avatarThumbUrl = myUserInfo.getHeadImgUrl();
                if (!TextUtils.isEmpty(avatarThumbUrl)) {
                    mCivAvatar.setImageUrl(avatarThumbUrl);
                    mCivAvatar.loadImage();
                } else {
                    mCivAvatar.setImageResource(Utils.getAvatarId((Integer.parseInt(myUserInfo.getId()))));
                }
            } else {
                mCivAvatar.setImageResource(Utils.getAvatarId(-1));
            }
        } else {
            mBtnLogin.setVisibility(View.VISIBLE);
            mLlUserInfo.setVisibility(View.GONE);
            mCivAvatar.setImageResource(Utils.getAvatarId(-1));
        }
    }

    class OnPersonalCenterItemClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            boolean jump2Login = false;
            if (mIsMy && !TourPalApplication.getInstance().hasLogin()){
                jump2Login = true;
            }
            switch (v.getId()){
                case R.id.fpc_item_personal_info:
                    if (jump2Login){
                        break;
                    }
                    //打点数据
                    Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.USERCENTER_USER_INFO);

                    Intent euiIntent = new Intent(getActivity(), EditUserInfoActivity.class);
                    startActivity(euiIntent);
                    break;
                case R.id.fpc_item_personal_posts:
                    if (jump2Login){
                        break;
                    }
                    if (mIsMy){
                        //打点数据
                        Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.USERCENTER_POST);

                        PostListActivity.startActivityByUser(getActivity(), null);
                    } else {
                        //打点数据
                        Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.USERCENTER_OTHER_USER_INFO);

                        if (mOthersUserInfo != null){
                            PostListActivity.startActivityByUser(getActivity(), new String(mOthersUserInfo.toByteArray()));
                        }
                    }
                    break;
                case R.id.fpc_item_personal_joined:
                    if (jump2Login){
                        break;
                    }
                    if (mIsMy) {
                        PostListActivity.startActivityForUserJoinedPosts(getActivity(), null);
                    } else {
                        if (mOthersUserInfo != null) {
                            PostListActivity.startActivityForUserJoinedPosts(getActivity(), new String(mOthersUserInfo.toByteArray()));
                        }
                    }
                    break;
                case R.id.fpc_item_settings:
                    if (jump2Login){
                        jump2Login = false;
                    }
                    //打点数据
                    Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.USERCENTER_SETTINGS);

                    Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                case R.id.fpc_iv_avatar:
                    if (jump2Login){
                        break;
                    }

                    Intent intent = new Intent(PersonalCenterFragment.this.getActivity(), AlbumActivity.class);
                    startActivity(intent);
                    break;
            }

            if (jump2Login){
                //未登录则直接跳去登录页面
                Intent loginIntent = new Intent(getActivity(), LoginWebviewActivity.class);
                startActivity(loginIntent);
                return;
            }
        }
    }

    class MyUserInfoChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshLoginInfo();
        }
    }

}
