package com.elong.tourpal.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;
import com.elong.tourpal.net.GetMyUserInfoTask;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.supports.UiUtils;
import com.elong.tourpal.ui.views.CommonDialog;
import com.elong.tourpal.ui.views.CommonEditableItem;
import com.elong.tourpal.ui.views.CommonSettingItem;
import com.elong.tourpal.ui.views.CommonToastDialog;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.Utils;

import java.util.ArrayList;

public class EditUserInfoActivity extends ActivityBase {
    private static final String TAG = "EditUserInfoActivity";

    private CommonSettingItem mCsiAvatar;
    private CommonSettingItem mCsiNickName;
    private CommonSettingItem mCsiSex;
    private CommonSettingItem mCsiAge;
    private CommonSettingItem mCsiContactInfo;

    private MyUserInfoChangeReceiver mMyUserInfoChangeReceiver = null;

    private MessageProtos.UserInfo mCurUserInfo = null;

    private CommonToastDialog mProgressToastDlg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);
        initViews();
        initReceivers();
        getMyUserInfoAsync();
    }

    @Override
    protected void onDestroy() {
        if (mMyUserInfoChangeReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMyUserInfoChangeReceiver);
        }
        super.onDestroy();
    }

    private void initReceivers() {
        mMyUserInfoChangeReceiver = new MyUserInfoChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(Actions.ACTION_UPDATE_MY_USER_INFO);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMyUserInfoChangeReceiver, intentFilter);
    }

    private void initViews() {
        initTitle();
        mCsiAvatar = (CommonSettingItem) findViewById(R.id.eui_csi_avatar);
        mCsiNickName = (CommonSettingItem) findViewById(R.id.eui_csi_nick_name);
        mCsiSex = (CommonSettingItem) findViewById(R.id.eui_csi_sex);
        mCsiAge = (CommonSettingItem) findViewById(R.id.eui_csi_age);
        mCsiContactInfo = (CommonSettingItem) findViewById(R.id.eui_csi_contact_info);

        mCsiAvatar.setNoLeftIconMode(false);
        mCsiNickName.setNoLeftIconMode(false);
        mCsiSex.setNoLeftIconMode(false);
        mCsiAge.setNoLeftIconMode(false);
        mCsiContactInfo.setNoLeftIconMode(true);

        mCsiNickName.setIconRight(R.mipmap.ic_right_arrow);
        mCsiSex.setIconRight(R.mipmap.ic_right_arrow);
        mCsiAge.setIconRight(R.mipmap.ic_right_arrow);
        mCsiContactInfo.setIconRight(R.mipmap.ic_right_arrow);

        mCsiAvatar.setDividerTopEnable(true);

        mCsiAvatar.setContentText(R.string.eui_avatar);
        mCsiNickName.setContentText(R.string.eui_nick_name);
        mCsiSex.setContentText(R.string.eui_sex);
        mCsiAge.setContentText(R.string.eui_age);
        mCsiContactInfo.setContentText(R.string.eui_contact_info);

        OnItemClickListener onItemClickListener = new OnItemClickListener();
        mCsiAvatar.setOnClickListener(onItemClickListener);
        mCsiNickName.setOnClickListener(onItemClickListener);
        mCsiSex.setOnClickListener(onItemClickListener);
        mCsiAge.setOnClickListener(onItemClickListener);
        mCsiContactInfo.setOnClickListener(onItemClickListener);

        loadLocalUserInfo();

        mProgressToastDlg = new CommonToastDialog(this);
    }

    class OnItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.eui_csi_avatar:
                    Intent intent = new Intent(EditUserInfoActivity.this, AlbumActivity.class);
                    startActivity(intent);
                    break;
                case R.id.eui_csi_nick_name:
                    EditSpecItemActivity.startActivity(EditUserInfoActivity.this, EditSpecItemActivity.TYPE_NICK_NAME);
                    break;
                case R.id.eui_csi_sex:
                    CommonDialog sexDialog = new CommonDialog(EditUserInfoActivity.this);
                    sexDialog.setTitle(R.string.eui_dialog_title_choose_sex);
                    ArrayList<String> sexs = new ArrayList<>();
                    sexs.add(getString(R.string.male));
                    sexs.add(getString(R.string.female));
                    sexDialog.setContentList(sexs, new CommonDialog.ItemSelectListener() {
                        @Override
                        public void onItemSelect(ArrayList<String> datas, int position) {
                            String data = datas.get(position);
                            int sex = 2;
                            if (getString(R.string.male).equals(data)) {
                                sex = 1;
                            }
                            mCurUserInfo.setSex(sex);
                            ModifyMyUserInfoTask task = new ModifyMyUserInfoTask();
                            task.execute();
                        }
                    });
                    sexDialog.show();
                    break;
                case R.id.eui_csi_age:
                    EditSpecItemActivity.startActivity(EditUserInfoActivity.this, EditSpecItemActivity.TYPE_AGE);
                    break;
                case R.id.eui_csi_contact_info:
                    Intent editContactInfoIntent = new Intent(EditUserInfoActivity.this, EditContactInfoActivity.class);
                    startActivity(editContactInfoIntent);
                    break;
            }
        }
    }

    private void initTitle() {
        setTitleText(R.string.eui_title);
//        TextView rightTextView = getTitleRightTextView();
//        rightTextView.setText(R.string.eui_title_right_text);
//        int padding = getResources().getDimensionPixelSize(R.dimen.common_titlebar_text_padding);
//        rightTextView.setPadding(padding, 0, padding, 0);
//        setTitleRightBtn(-1);
//        rightTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isInfoComplete()) {
//                    if (!saveMyUserInfo()) {
//                        finish();
//                    }
//                }
//            }
//        });
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                leaveThisActivity();
                if (isInfoComplete()) {
                    finish();
                }
            }
        });
        setTitleRightBtnEnable(false);
    }

//    private void leaveThisActivity() {
//        if (!isSame()) {
//            CommonDialog dialog = new CommonDialog(this);
//            dialog.setTitle(R.string.common_tips);
//            dialog.setMessage(R.string.eui_msg_save_tips);
//            dialog.setLeftBtnOnclickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    finish();
//                }
//            });
//            dialog.setRightBtnOnclickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (isInfoComplete()) {
//                        if (!saveMyUserInfo()) {
//                            finish();
//                        }
//                    }
//                }
//            });
//            dialog.show();
//        } else {
//            finish();
//        }
//    }

    private void getMyUserInfoAsync() {
        GetMyUserInfoTask task = new GetMyUserInfoTask();
        task.execute();
    }

    private void loadLocalUserInfo() {
        mCurUserInfo = SharedPref.getInstance().getMyUserInfo();
        if (mCurUserInfo != null) {
            mCsiNickName.setRightText(mCurUserInfo.getNickName());
            String sex = mCurUserInfo.getSex() == 1 ? "男" : "女";
            mCsiSex.setRightText(sex);
            mCsiAge.setRightText(String.valueOf(mCurUserInfo.getAge()));
            boolean hasContactInfo = !TextUtils.isEmpty(mCurUserInfo.getWeixin()) || !TextUtils.isEmpty(mCurUserInfo.getQq()) || !TextUtils.isEmpty(mCurUserInfo.getPhone());
            if (!hasContactInfo) {
                mCsiContactInfo.setRightText("");
            }
            String avatarUrl = mCurUserInfo.getHeadImgUrl();
            if (!TextUtils.isEmpty(avatarUrl)) {
                mCsiAvatar.setCivRightImage(avatarUrl, true);
            } else {
                String id = mCurUserInfo.getId();
                if (!TextUtils.isEmpty(id)) {
                    int uid = Integer.parseInt(id);
                    mCsiAvatar.setCivRightImage(Utils.getAvatarId(uid));
                } else {
                    mCsiAvatar.setCivRightImage(R.mipmap.avatar_default);
                }
            }
        }
    }

//    private void loadLocalUserInfo() {
//        MessageProtos.UserInfo newUserInfo = SharedPref.getInstance().getMyUserInfo();
//        if (newUserInfo != null) {
//            boolean isCurUserInfoNull = mCurUserInfo != null;
//            String oldNickName = isCurUserInfoNull ? mCurUserInfo.getNickName() : "";
//            String newNickName = newUserInfo.getNickName();
//            if (mCsiNickName.getEtRightText().equals(oldNickName) && !TextUtils.isEmpty(newNickName)) {
//                mCsiNickName.setEtRightText(newNickName);
//            }
//            String oldSex = mCurUserInfo != null ? (mCurUserInfo.getSex() == 1 ? "男" : "女") : "";
//            String newSex = newUserInfo.getSex() == 1 ? "男" : "女";
//            if (mCsiSex.getEtRightText().equals(oldSex)) {
//                mCsiSex.setEtRightText(newSex);
//            }
//            String oldAge = mCurUserInfo != null ? String.valueOf(mCurUserInfo.getAge()) : "";
//            String newAge = String.valueOf(newUserInfo.getAge());
//            if (mCsiAge.getEtRightText().equals(oldAge)) {
//                mCsiAge.setEtRightText(String.valueOf(newAge));
//            }
//            String oldWechat = mCurUserInfo != null ? mCurUserInfo.getWeixin() : "";
//            String newWechat = newUserInfo.getWeixin();
//            if (mCsiWechat.getEtRightText().equals(oldWechat) && !TextUtils.isEmpty(newWechat)) {
//                mCsiWechat.setEtRightText(newWechat);
//            }
//            String oldQQ = mCurUserInfo != null ? mCurUserInfo.getQq() : "";
//            String newQQ = newUserInfo.getQq();
//            if (mCsiQq.getEtRightText().equals(oldQQ) && !TextUtils.isEmpty(newQQ)) {
//                mCsiQq.setEtRightText(newQQ);
//            }
//            String oldPhone = mCurUserInfo != null ? mCurUserInfo.getPhone() : "";
//            String newPhone = newUserInfo.getPhone();
//            if (mCsiPhone.getEtRightText().equals(oldPhone) && !TextUtils.isEmpty(newPhone)) {
//                mCsiPhone.setEtRightText(newPhone);
//            }
//            //指向新的userInfo
//            mCurUserInfo = newUserInfo;
//        }
//    }

//    /**
//     * 保存编辑的资料
//     *
//     * @return 资料是否与旧的一致
//     */
//    private boolean saveMyUserInfo() {
//        if (!isSame()) {
//            mCurUserInfo.setNickName(mCsiNickName.getEtRightText().trim());
//            int sex = mCsiSex.getEtRightText().equals("男") ? 1 : 2;
//            mCurUserInfo.setSex(sex);
//            mCurUserInfo.setAge(Integer.parseInt(mCsiAge.getEtRightText()));
//            mCurUserInfo.setWeixin(mCsiWechat.getEtRightText().trim());
//            mCurUserInfo.setQq(mCsiQq.getEtRightText().trim());
//            mCurUserInfo.setPhone(mCsiPhone.getEtRightText().trim());
//            ModifyMyUserInfoTask task = new ModifyMyUserInfoTask();
//            task.execute();
//            return true;
//        }
//        return false;
//    }

//    private boolean isSame() {
//        if (!mCsiNickName.getEtRightText().trim().equals(mCurUserInfo.getNickName())) {
//            return false;
//        }
//        if (!mCsiSex.getEtRightText().equals(mCurUserInfo.getSex() == 1 ? "男" : "女")) {
//            return false;
//        }
//        if (!mCsiAge.getEtRightText().equals(String.valueOf(mCurUserInfo.getAge()))) {
//            return false;
//        }
//        if (!mCsiWechat.getEtRightText().trim().equals(mCurUserInfo.getWeixin())) {
//            return false;
//        }
//        if (!mCsiQq.getEtRightText().trim().equals(mCurUserInfo.getQq())) {
//            return false;
//        }
//        if (!mCsiPhone.getEtRightText().trim().equals(mCurUserInfo.getPhone())) {
//            return false;
//        }
//        return true;
//    }

    class MyUserInfoChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadLocalUserInfo();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            leaveThisActivity();
            if (!isInfoComplete()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isInfoComplete() {
        if (TextUtils.isEmpty(mCurUserInfo.getNickName())) {
            Toast.makeText(this, R.string.eui_tips_empty_nick_name, Toast.LENGTH_SHORT).show();
            return false;
        }
//        if (TextUtils.isEmpty(mCsiAge.getRightText())) {
//            Toast.makeText(this, R.string.eui_tips_empty_nick_age, Toast.LENGTH_SHORT).show();
//            return false;
//        }
        boolean hasContactInfo = !TextUtils.isEmpty(mCurUserInfo.getWeixin()) || !TextUtils.isEmpty(mCurUserInfo.getQq()) || !TextUtils.isEmpty(mCurUserInfo.getPhone());
        if (!hasContactInfo) {
            Toast.makeText(this, R.string.eui_tips_at_least_on_contact_info, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
                Toast.makeText(EditUserInfoActivity.this, "资料已保存", Toast.LENGTH_SHORT).show();
                LocalBroadcastManager.getInstance(EditUserInfoActivity.this).sendBroadcast(new Intent(Actions.ACTION_UPDATE_MY_USER_INFO));
//                finish();
            } else {
                Toast.makeText(EditUserInfoActivity.this, "资料保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
