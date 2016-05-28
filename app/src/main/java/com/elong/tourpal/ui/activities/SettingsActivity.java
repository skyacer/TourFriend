package com.elong.tourpal.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.views.CommonDialog;
import com.elong.tourpal.ui.views.CommonSettingItem;
import com.elong.tourpal.update.UpdateManager;
import com.elong.tourpal.utils.SharedPref;

public class SettingsActivity extends ActivityBase {

    private static final String TAG = "SettingsActivity";

    private CommonSettingItem mCsiVoice;
    private CommonSettingItem mCsiVibration;
    private CommonSettingItem mCsiPush;
    private CommonSettingItem mCsiAbout;
    private CommonSettingItem mCsiCheckUpdate;
    private CommonSettingItem mCsiFeedback;
    private CommonSettingItem mCsiLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化各种开关的状态
        initSwitchData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //保存各个开关的设置
        saveSwitchData();
    }

    private void initViews() {
        initTitle();
        mCsiVoice = (CommonSettingItem) findViewById(R.id.settings_csi_voice);
        mCsiVibration = (CommonSettingItem) findViewById(R.id.settings_csi_vibration);
        mCsiPush = (CommonSettingItem) findViewById(R.id.settings_csi_push);
        mCsiAbout = (CommonSettingItem) findViewById(R.id.settings_csi_about);
        mCsiCheckUpdate = (CommonSettingItem) findViewById(R.id.settings_csi_check_update);
        mCsiFeedback = (CommonSettingItem) findViewById(R.id.settings_csi_feedback);
        mCsiLogout = (CommonSettingItem) findViewById(R.id.settings_csi_logoff);

        mCsiLogout.setVisibility(TourPalApplication.getInstance().hasLogin() ? View.VISIBLE : View.GONE);

        OnItemClickListener onItemClickListener = new OnItemClickListener();
        mCsiVoice.setOnClickListener(onItemClickListener);
        mCsiVibration.setOnClickListener(onItemClickListener);
        mCsiPush.setOnClickListener(onItemClickListener);
        mCsiAbout.setOnClickListener(onItemClickListener);
        mCsiCheckUpdate.setOnClickListener(onItemClickListener);
        mCsiFeedback.setOnClickListener(onItemClickListener);
        mCsiLogout.setOnClickListener(onItemClickListener);

        mCsiVoice.setDividerTopEnable(true);
        mCsiAbout.setDividerTopEnable(true);
        mCsiLogout.setDividerTopEnable(true);

        mCsiVoice.setNoLeftIconMode(false);
        mCsiVibration.setNoLeftIconMode(false);
        mCsiPush.setNoLeftIconMode(true);
        mCsiAbout.setNoLeftIconMode(false);
        mCsiCheckUpdate.setNoLeftIconMode(false);
        mCsiFeedback.setNoLeftIconMode(true);
        mCsiLogout.setNoLeftIconMode(true);

        mCsiVoice.setContentText(R.string.settings_msg_voice);
        mCsiVibration.setContentText(R.string.settings_msg_vibration);
        mCsiPush.setContentText(R.string.settings_msg_push);
        mCsiAbout.setContentText(R.string.settings_msg_about);
        mCsiCheckUpdate.setContentText(R.string.settings_msg_check_update);
        mCsiLogout.setContentText(R.string.settings_msg_logout);
        mCsiFeedback.setContentText(R.string.settings_msg_feedback);

        mCsiVoice.setType(CommonSettingItem.TYPE_CHECKABLE);
        mCsiVibration.setType(CommonSettingItem.TYPE_CHECKABLE);
        mCsiPush.setType(CommonSettingItem.TYPE_CHECKABLE);
    }

    class OnItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.settings_csi_voice:
                    //do nothing
                    break;
                case R.id.settings_csi_vibration:
                    //do nothing
                    break;
                case R.id.settings_csi_push:
                    //do nothing
                    break;
                case R.id.settings_csi_about:
                    Intent aboutIntent = new Intent(SettingsActivity.this, AboutActivity.class);
                    startActivity(aboutIntent);
                    break;
                case R.id.settings_csi_check_update:
                    UpdateManager updateManager = new UpdateManager(SettingsActivity.this);
                    updateManager.checkUpdate(false);
                    break;
                case R.id.settings_csi_feedback:
                    Intent feedbackIntent = new Intent(SettingsActivity.this, FeedbackActivity.class);
                    startActivity(feedbackIntent);
                    break;
                case R.id.settings_csi_logoff:
                    showLogoutConfirmDialog();
                    break;
            }
        }
    }

    private void initTitle() {
        setTitleText(R.string.settings_title);
        setTitleRightBtnEnable(false);
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initSwitchData() {
        SharedPref pref = SharedPref.getInstance();
        mCsiVoice.setSwitchOn(pref.isSettingMsgVoiceOn());
        mCsiVibration.setSwitchOn(pref.isSettingMsgVibrationOn());
        mCsiPush.setSwitchOn(pref.isSettingPushOn());
    }

    private void saveSwitchData() {
        SharedPref pref = SharedPref.getInstance();
        pref.setSettingMsgVoiceOn(mCsiVoice.isSwitchOn());
        pref.setSettingMsgVibrationOn(mCsiVibration.isSwitchOn());
        pref.setSettingPushOn(mCsiPush.isSwitchOn());
    }

    private void doLogout() {
        SharedPref pref = SharedPref.getInstance();
        final String sessionId = pref.getSessionId();
        final String sessionToken = pref.getSessionToken();
        //push解绑
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = RequestBuilder.buildUnbindPushClientIdRequest(sessionId, sessionToken);
                if (request != null) {
                    MessageProtos.ResponseInfo respInfo = request.get();
                    if (respInfo != null) {
                        if (Env.DEBUG) {
                            Log.d(TAG, "unbind push client id result: err_code=" + respInfo.getErrCode());
                        }
                    }
                }
            }
        });
        thread.start();
        //
        pref.setSessionId("");
        pref.setSessionToken("");
        pref.setHasNewMessage(false);
        TourPalApplication.getInstance().setHasLogin(false);
        mCsiLogout.setVisibility(View.GONE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Actions.ACTION_UPDATE_MY_USER_INFO));
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Actions.ACTION_LOGOUT));
    }

    private void showLogoutConfirmDialog() {
        CommonDialog commonDialog = new CommonDialog(this);
        commonDialog.setTitle(R.string.common_tips);
        commonDialog.setMessage(R.string.logout_confirm_dialog_message);
        commonDialog.setCanceledOnTouchOutside(false);
        commonDialog.setRightBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogout();
            }
        });
        commonDialog.show();
    }

}
