package com.elong.tourpal.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.db.DBManagerClient;
import com.elong.tourpal.model.AccountsInfo;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.ToastUtil;

/**
 * Created by LuoChangAn on 16/5/18.
 */
public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    TextView title_tv;
    EditText name_et;
    EditText passwd_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        findViewById(R.id.item_login).setOnClickListener(this);
        findViewById(R.id.add_new_account).setOnClickListener(this);
        title_tv = (TextView) findViewById(R.id.common_tv_title);
        title_tv.setText(R.string.login_title);

        name_et = (EditText) findViewById(R.id.login_name_et);
        passwd_et = (EditText) findViewById(R.id.login_passwd_et);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.item_login:
                if (!TextUtils.isEmpty(getNameEt())&&
                        !TextUtils.isEmpty(getPasswordEt())){
                    if (!login()){
                        ToastUtil.makeShortToast(R.string.login_account_error);
                    }else {
                        SharedPref.getInstance().setSessionId("123");
                        TourPalApplication.mHasLogin = true;
                        finish();
                    }
                }else {
                    ToastUtil.makeShortToast(R.string.login_account_not_complete);
                }
                break;
            case R.id.add_new_account:
                AddNewAccountActivity.start(LoginActivity.this);
                break;
            default:
                break;
        }
    }

    private String getNameEt(){
        return name_et.getText().toString();
    }

    private String getPasswordEt(){
        return passwd_et.getText().toString();
    }

    private boolean login(){
        AccountsInfo info = new AccountsInfo();
        info.mAccounts = getNameEt();
        info.mPassword = getPasswordEt();
        return DBManagerClient.ifAccountsExist(info);
    }

    public static void start(Activity activity){
        Intent intent = new Intent(activity,LoginActivity.class);
        activity.startActivity(intent);
    }
}
