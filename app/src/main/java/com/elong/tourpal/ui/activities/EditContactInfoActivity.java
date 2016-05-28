package com.elong.tourpal.ui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.views.CommonDialog;
import com.elong.tourpal.ui.views.CommonEditableItem;
import com.elong.tourpal.ui.views.CommonSettingItem;
import com.elong.tourpal.ui.views.CommonToastDialog;
import com.elong.tourpal.utils.SharedPref;

public class EditContactInfoActivity extends ActivityBase {

    private static final String TAG = "EditContactInfoActivity";

    private CommonEditableItem mCsiWechat;
    private CommonEditableItem mCsiQQ;
    private CommonEditableItem mCsiPhone;

    private CommonSettingItem mCsiPrivacyWechat;
    private CommonSettingItem mCsiPrivacyQQ;
    private CommonSettingItem mCsiPrivacyPhone;
    private CommonSettingItem mCsiPrivacyShareWithGroup;

    private MessageProtos.UserInfo mCurUserInfo;

    private CommonToastDialog mProgressToastDlg = null;

    private boolean mIsFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact_info);
        initViews();
    }

    private void initViews() {
        mCsiWechat = (CommonEditableItem) findViewById(R.id.eci_cei_wechat);
        mCsiQQ = (CommonEditableItem) findViewById(R.id.eci_cei_qq);
        mCsiPhone = (CommonEditableItem) findViewById(R.id.eci_cei_phone);
        mCsiPrivacyWechat = (CommonSettingItem) findViewById(R.id.eci_csi_privacy_wechat);
        mCsiPrivacyQQ = (CommonSettingItem) findViewById(R.id.eci_csi_privacy_qq);
        mCsiPrivacyPhone = (CommonSettingItem) findViewById(R.id.eci_csi_privacy_phone);
        mCsiPrivacyShareWithGroup = (CommonSettingItem) findViewById(R.id.eci_csi_privacy_share_with_group);

        mCsiPhone.setFullBottomDivider(true);

        mCsiWechat.setTextLeft(R.string.eci_wechat);
        mCsiQQ.setTextLeft(R.string.eci_qq);
        mCsiPhone.setTextLeft(R.string.eci_phone);

        mCsiWechat.setEtRightHint(R.string.eci_empty_info);
        mCsiQQ.setEtRightHint(R.string.eci_empty_info);
        mCsiPhone.setEtRightHint(R.string.eci_empty_info);

        mCsiPrivacyShareWithGroup.setDividerTopEnable(true);

        mCsiPrivacyWechat.setNoLeftIconMode(false);
        mCsiPrivacyQQ.setNoLeftIconMode(false);
        mCsiPrivacyPhone.setNoLeftIconMode(true);
        mCsiPrivacyShareWithGroup.setNoLeftIconMode(true);

        mCsiPrivacyWechat.setType(CommonSettingItem.TYPE_CHECKABLE);
        mCsiPrivacyQQ.setType(CommonSettingItem.TYPE_CHECKABLE);
        mCsiPrivacyPhone.setType(CommonSettingItem.TYPE_CHECKABLE);
        mCsiPrivacyShareWithGroup.setType(CommonSettingItem.TYPE_CHECKABLE);

        mCsiPrivacyWechat.setContentText(R.string.eci_privacy_wechat_title);
        mCsiPrivacyQQ.setContentText(R.string.eci_privacy_qq_title);
        mCsiPrivacyPhone.setContentText(R.string.eci_privacy_phone_title);
        mCsiPrivacyShareWithGroup.setContentText(R.string.eci_privacy_share_with_group_title);

        mCsiPrivacyPhone.setSecondLineText(R.string.eci_contact_info_hint);
        mCsiPrivacyShareWithGroup.setSecondLineText(R.string.eci_privacy_share_with_group_hint);

        OnItemCheckListener onItemCheckListener = new OnItemCheckListener();
        mCsiPrivacyWechat.setOnCheckedChangeListener(onItemCheckListener);
        mCsiPrivacyQQ.setOnCheckedChangeListener(onItemCheckListener);
        mCsiPrivacyPhone.setOnCheckedChangeListener(onItemCheckListener);

        mProgressToastDlg = new CommonToastDialog(this);

        loadData();

        mIsFirstLoad = false;

        initTitle();
    }

    private void initTitle() {
        setTitleText(R.string.eci_title);
        TextView rightTextView = getTitleRightTextView();
        rightTextView.setText(R.string.common_save);
        int padding = getResources().getDimensionPixelSize(R.dimen.common_titlebar_text_padding);
        rightTextView.setPadding(padding, 0, padding, 0);
        setTitleRightBtn(-1);
        rightTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContactInfo();
            }
        });
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leave();
            }
        });
    }

    private void loadData() {
        mCurUserInfo = SharedPref.getInstance().getMyUserInfo();

        String wechat = mCurUserInfo.getWeixin();
        String qq = mCurUserInfo.getQq();
        String phone = mCurUserInfo.getPhone();

        if (!TextUtils.isEmpty(wechat)) {
            mCsiWechat.setEtRightText(wechat);
        }
        if (!TextUtils.isEmpty(qq)) {
            mCsiQQ.setEtRightText(qq);
        }
        if (!TextUtils.isEmpty(phone)) {
            mCsiPhone.setEtRightText(phone);
        }

        int visibleItemsInt = mCurUserInfo.getVisibleItem();
        boolean wechatVisible = (visibleItemsInt & 1) == 1;
        boolean qqVisible = (visibleItemsInt & 2) == 2;
        boolean phoneVisible = (visibleItemsInt & 4) == 4;
        mCsiPrivacyWechat.setSwitchOn(wechatVisible);
        mCsiPrivacyQQ.setSwitchOn(qqVisible);
        mCsiPrivacyPhone.setSwitchOn(phoneVisible);

        mCsiPrivacyShareWithGroup.setSwitchOn(mCurUserInfo.getGroupVisible());
    }

    private void saveContactInfo() {
        if (mCurUserInfo != null) {
            String wechat = mCsiWechat.getEtRightText().trim();
            String qq = mCsiQQ.getEtRightText().trim();
            String phone = mCsiPhone.getEtRightText().trim();

            boolean hasWechat = !TextUtils.isEmpty(wechat);
            boolean hasQQ = !TextUtils.isEmpty(qq);
            boolean hasPhone = !TextUtils.isEmpty(phone);
            if (!hasWechat && !hasQQ && !hasPhone) {
                Toast.makeText(this, R.string.eci_toast_at_least_one_info, Toast.LENGTH_SHORT).show();
                return;
            }

            int visibleItemsInt = 0;
            if (hasWechat) {
                visibleItemsInt += mCsiPrivacyWechat.isSwitchOn() ? 1 : 0;
            }
            if (hasQQ) {
                visibleItemsInt += mCsiPrivacyQQ.isSwitchOn() ? 2 : 0;
            }
            if (hasPhone) {
                visibleItemsInt += mCsiPrivacyPhone.isSwitchOn() ? 4 : 0;
            }
            if (visibleItemsInt == 0) {
                Toast.makeText(this, R.string.eci_toast_privacy_at_least_one_info, Toast.LENGTH_SHORT).show();
                return;
            }

            mCurUserInfo.setWeixin(wechat);
            mCurUserInfo.setQq(qq);
            mCurUserInfo.setPhone(phone);
            mCurUserInfo.setVisibleItem(visibleItemsInt);
            mCurUserInfo.setGroupVisible(mCsiPrivacyShareWithGroup.isSwitchOn());

            ModifyMyUserInfoTask task = new ModifyMyUserInfoTask();
            task.execute();
        }
    }

    private boolean isModified() {
        boolean result = false;
        if (mCurUserInfo != null) {
            String wechat = mCsiWechat.getEtRightText();
            String qq = mCsiQQ.getEtRightText();
            String phone = mCsiPhone.getEtRightText();
            int visibleItemsInt = 0;
            visibleItemsInt += mCsiPrivacyWechat.isSwitchOn() ? 1 : 0;
            visibleItemsInt += mCsiPrivacyQQ.isSwitchOn() ? 2 : 0;
            visibleItemsInt += mCsiPrivacyPhone.isSwitchOn() ? 4 : 0;
            boolean groupVisible = mCsiPrivacyShareWithGroup.isSwitchOn();
            if (!wechat.equals(mCurUserInfo.getWeixin()) || !qq.equals(mCurUserInfo.getQq()) || !phone.equals(mCurUserInfo.getPhone())
                    || visibleItemsInt != mCurUserInfo.getVisibleItem() || groupVisible != mCurUserInfo.getGroupVisible()) {
                result = true;
            }
        }
        return result;
    }

    private void leave() {
        if (isModified()) {
            CommonDialog dialog = new CommonDialog(this);
            dialog.setTitle(R.string.common_tips);
            dialog.setMessage(R.string.eui_msg_save_tips);
            dialog.setLeftBtnOnclickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            dialog.setRightBtnOnclickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveContactInfo();
                }
            });
            dialog.show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            leave();
        }
        return super.onKeyDown(keyCode, event);
    }

    class OnItemCheckListener implements CommonSettingItem.OnItemCheckChangedListener {

        @Override
        public void onItemCheckChanged(int itemId, CompoundButton buttonView, boolean isChecked) {
            if (mIsFirstLoad) {
                //首次加载会setCheck，不检查
                return;
            }
            if (!isChecked) {
                if (!mCsiPrivacyWechat.isSwitchOn() && !mCsiPrivacyQQ.isSwitchOn() && !mCsiPrivacyPhone.isSwitchOn()) {
                    Toast.makeText(EditContactInfoActivity.this, R.string.eci_toast_at_least_check_one_info, Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(true);
                }
            } else {
                switch (itemId) {
                    case R.id.eci_csi_privacy_wechat:
                        if (TextUtils.isEmpty(mCsiWechat.getEtRightText())) {
                            Toast.makeText(EditContactInfoActivity.this, R.string.eci_toast_enter_wechat_before_check, Toast.LENGTH_SHORT).show();
                            buttonView.setChecked(false);
                        }
                        break;
                    case R.id.eci_csi_privacy_qq:
                        if (TextUtils.isEmpty(mCsiQQ.getEtRightText())){
                            Toast.makeText(EditContactInfoActivity.this, R.string.eci_toast_enter_qq_before_check, Toast.LENGTH_SHORT).show();
                            buttonView.setChecked(false);
                        }
                        break;
                    case R.id.eci_csi_privacy_phone:
                        if (TextUtils.isEmpty(mCsiPhone.getEtRightText())){
                            Toast.makeText(EditContactInfoActivity.this, R.string.eci_toast_enter_phone_before_check, Toast.LENGTH_SHORT).show();
                            buttonView.setChecked(false);
                        }
                        break;
                }
            }
        }
    }

    class ModifyMyUserInfoTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressToastDlg.setDialogTitle(getString(R.string.toast_saving_in_progress));
            mProgressToastDlg.setIsLoading(true);
            mProgressToastDlg.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
            Request request = RequestBuilder.buildModifyMyUserInfoRequest(mCurUserInfo);
            MessageProtos.ResponseInfo respInfo = request.get();
            if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                if (Env.DEBUG) {
                    Log.d(TAG, "modify userInfo success");
                }
                return true;
            } else {
                Log.d(TAG, "modify userInfo failed");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressToastDlg.dismiss();
            if (result) {
                SharedPref.getInstance().setMyUserInfo(mCurUserInfo);
                Toast.makeText(EditContactInfoActivity.this, "资料已保存", Toast.LENGTH_SHORT).show();
                LocalBroadcastManager.getInstance(EditContactInfoActivity.this).sendBroadcast(new Intent(Actions.ACTION_UPDATE_MY_USER_INFO));
                finish();
            } else {
                Toast.makeText(EditContactInfoActivity.this, "资料保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
