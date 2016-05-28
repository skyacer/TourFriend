package com.elong.tourpal.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.supports.UiUtils;
import com.elong.tourpal.ui.views.CommonDialog;
import com.elong.tourpal.ui.views.CommonToastDialog;
import com.elong.tourpal.utils.SharedPref;

public class EditSpecItemActivity extends ActivityBase {
    private static final String TAG = "EditSpecItemActivity";

    public static final int TYPE_NICK_NAME = 1;
    public static final int TYPE_AGE = 2;

    private int mType = TYPE_NICK_NAME;

    private EditText mEtContent;
    private CommonToastDialog mProgressToastDlg = null;


    private SharedPref mSharedPref = SharedPref.getInstance();
    private MessageProtos.UserInfo mCurUserInfo;

    public static void startActivity(Context context, int type) {
        Intent intent = new Intent(context, EditSpecItemActivity.class);
        intent.putExtra(Extras.EXTRA_EDIT_SPEC_ITEM_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_spec_item);
        resolveIntent();
        initViews();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        mType = intent.getIntExtra(Extras.EXTRA_EDIT_SPEC_ITEM_TYPE, TYPE_NICK_NAME);
    }

    private void initViews() {
        //title
        initTitle();

        mEtContent = (EditText) findViewById(R.id.esi_et_content);

        initContent();

        mProgressToastDlg = new CommonToastDialog(this);
    }

    private void initTitle() {
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leave();
            }
        });

        TextView rightTextView = getTitleRightTextView();
        rightTextView.setText(R.string.common_save);
        int padding = getResources().getDimensionPixelSize(R.dimen.common_titlebar_text_padding);
        rightTextView.setPadding(padding, 0, padding, 0);
        setTitleRightBtn(-1);

        rightTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItemInfo();
            }
        });

        switch (mType) {
            case TYPE_NICK_NAME:
                setTitleText(R.string.esi_title_nick_name);
                break;
            case TYPE_AGE:
                setTitleText(R.string.esi_title_age);
                break;
        }
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
                    saveItemInfo();
                }
            });
            dialog.show();
        } else {
            finish();
        }
    }

    private void initContent() {
        mCurUserInfo = mSharedPref.getMyUserInfo();
        switch (mType) {
            case TYPE_NICK_NAME:
                mEtContent.setInputType(InputType.TYPE_CLASS_TEXT);
                String nickName = mCurUserInfo.getNickName();
                if (!TextUtils.isEmpty(nickName)) {
                    mEtContent.setText(nickName);
                }
                break;
            case TYPE_AGE:
                mEtContent.setInputType(InputType.TYPE_CLASS_NUMBER);
                int age = mCurUserInfo.getAge();
                mEtContent.setText(String.valueOf(age));
                break;
        }
    }

    private void saveItemInfo() {

        switch (mType) {
            case TYPE_NICK_NAME:
                saveNickName();
                break;
            case TYPE_AGE:
                saveAge();
                break;
        }
    }

    private void saveNickName() {
        String nickName = mEtContent.getText().toString().trim();
        if (TextUtils.isEmpty(nickName)) {
            Toast.makeText(this, R.string.eui_tips_empty_nick_name, Toast.LENGTH_SHORT).show();
            return;
        }
        mCurUserInfo.setNickName(nickName);
        ModifyMyUserInfoTask task = new ModifyMyUserInfoTask();
        task.execute();
    }

    private void saveAge() {
        String age = mEtContent.getText().toString().trim();
        if (TextUtils.isEmpty(age.trim())) {
            Toast.makeText(this, R.string.eui_tips_empty_nick_age, Toast.LENGTH_SHORT).show();
            return;
        }
        mCurUserInfo.setAge(Integer.parseInt(age));
        ModifyMyUserInfoTask task = new ModifyMyUserInfoTask();
        task.execute();
    }

    private boolean isModified() {
        boolean result = false;
        switch (mType) {
            case TYPE_NICK_NAME:
                String nickName = mEtContent.getText().toString().trim();
                if (!nickName.equals(mCurUserInfo.getNickName())) {
                    result = true;
                }
                break;
            case TYPE_AGE:
                String age = mEtContent.getText().toString().trim();
                if (!age.equals(String.valueOf(mCurUserInfo.getAge()))) {
                    result = true;
                }
                break;
        }
        return result;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            leave();
        }
        return super.onKeyDown(keyCode, event);
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
                Toast.makeText(EditSpecItemActivity.this, "资料已保存", Toast.LENGTH_SHORT).show();
                LocalBroadcastManager.getInstance(EditSpecItemActivity.this).sendBroadcast(new Intent(Actions.ACTION_UPDATE_MY_USER_INFO));
                finish();
            } else {
                Toast.makeText(EditSpecItemActivity.this, "资料保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
