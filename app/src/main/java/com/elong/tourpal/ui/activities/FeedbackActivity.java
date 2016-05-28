package com.elong.tourpal.ui.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.views.CommonEditableItem;
import com.elong.tourpal.ui.views.CommonToastDialog;
import com.elong.tourpal.utils.SharedPref;

public class FeedbackActivity extends ActivityBase {

    private static final String TAG = "FeedbackActivity";

    private CommonEditableItem mCeiEmail;
    private CommonEditableItem mCeiQq;
    private CommonEditableItem mCeiPhone;
    private EditText mEtContent;

    private CommonToastDialog mProgressToastDlg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initViews();
    }

    private void initViews() {
        initTitle();
        mCeiEmail = (CommonEditableItem) findViewById(R.id.feedback_cei_email);
        mCeiQq = (CommonEditableItem) findViewById(R.id.feedback_cei_qq);
        mCeiPhone = (CommonEditableItem) findViewById(R.id.feedback_cei_phone);
        mEtContent = (EditText) findViewById(R.id.feedback_et_content);
        mEtContent.setHint(R.string.feedback_content_hint);

        mCeiEmail.setEtRightInputType(InputType.TYPE_CLASS_TEXT);
        mCeiPhone.setEtRightInputType(InputType.TYPE_CLASS_TEXT);
        mCeiQq.setEtRightInputType(InputType.TYPE_CLASS_TEXT);

        mCeiEmail.setTextLeft(R.string.feedback_email);
        mCeiPhone.setTextLeft(R.string.feedback_phone);
        mCeiQq.setTextLeft(R.string.feedback_qq);

        mCeiPhone.setDividerTopEnable(false);
        mCeiQq.setDividerTopEnable(false);

        mCeiEmail.setFullBottomDivider(false);
        mCeiPhone.setFullBottomDivider(false);

        mCeiEmail.setEtRightHint(R.string.eui_et_hint);
        mCeiQq.setEtRightHint(R.string.eui_et_hint);
        mCeiPhone.setEtRightHint(R.string.eui_et_hint);

        mProgressToastDlg = new CommonToastDialog(this);

        MessageProtos.UserInfo myUserInfo = SharedPref.getInstance().getMyUserInfo();
        if (myUserInfo != null) {
            String myPhone = myUserInfo.getPhone();
            String myQQ = myUserInfo.getQq();
            if (!TextUtils.isEmpty(myPhone)) {
                mCeiPhone.setEtRightText(myPhone);
            }
            if (!TextUtils.isEmpty(myQQ)) {
                mCeiQq.setEtRightText(myQQ);
            }
        }
    }

    private void initTitle() {
        setTitleText(R.string.feedback_title);
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitleRightBtn(-1);
        TextView tvSend = getTitleRightTextView();
        tvSend.setText(R.string.feedback_send);
        int padding = getResources().getDimensionPixelSize(R.dimen.common_titlebar_text_padding);
        tvSend.setPadding(padding, 0, padding, 0);
        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });
    }

    private void sendFeedback() {
        String feedbackContent = mEtContent.getText().toString();
        if (TextUtils.isEmpty(feedbackContent)) {
            Toast.makeText(this, R.string.feedback_toast_empty_content, Toast.LENGTH_SHORT).show();
        } else {
            SendFeedbackTask sendFeedbackTask = new SendFeedbackTask();
            sendFeedbackTask.execute();
        }
    }

    class SendFeedbackTask extends AsyncTask<Integer, Integer, Boolean> {

        private String content;
        private String qq;
        private String email;
        private String phone;

        @Override
        protected void onPreExecute() {
            email = mCeiEmail.getEtRightText();
            phone = mCeiPhone.getEtRightText();
            qq = mCeiQq.getEtRightText();
            content = mEtContent.getText().toString();
            mProgressToastDlg.setDialogTitle(getString(R.string.feedback_uploading));
            mProgressToastDlg.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
            Request request = RequestBuilder.buildFeedBackRequest(content, qq, email, phone);
            if (request != null) {
                MessageProtos.ResponseInfo respInfo = request.post();
                if (respInfo != null) {
                    if (Env.DEBUG) {
                        Log.e(TAG, "send feedback result: err_code=" + respInfo.getErrCode());
                    }
                    return respInfo.getErrCode() == MessageProtos.SUCCESS;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressToastDlg.dismiss();
            Toast.makeText(FeedbackActivity.this, R.string.feedback_success, Toast.LENGTH_SHORT).show();
        }
    }

}
