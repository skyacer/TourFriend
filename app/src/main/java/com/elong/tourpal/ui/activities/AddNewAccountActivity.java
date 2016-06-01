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
 * Created by LuoChangAn on 16/5/31.
 */
public class AddNewAccountActivity extends ActionBarActivity implements View.OnClickListener {
    TextView title_tv;
    EditText account_et;
    EditText password_et;
    EditText password_again_et;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_acount);
        initView();
    }

    private void initView() {
        title_tv = (TextView) findViewById(R.id.common_tv_title);
        title_tv.setText(R.string.add_new_passwd_title);
        account_et = (EditText) findViewById(R.id.add_new_account_name_et);
        password_et = (EditText) findViewById(R.id.add_new_password_et);
        password_again_et = (EditText) findViewById(R.id.add_new_password_again_et);
        findViewById(R.id.item_add_account).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.item_add_account:
                if (!TextUtils.isEmpty(getName())&&!TextUtils.isEmpty(getPassword())
                        &&!TextUtils.isEmpty(getPasswordAgain())){
                    if (!getPassword().equals(getPasswordAgain())){
                        ToastUtil.makeShortToast(R.string.add_new_passwd_not_same);
                    }else {
                        register();
                        finish();
                    }
                }else {
                    ToastUtil.makeShortToast(R.string.add_new_passwd_not_complete);
                }
                break;
            default:
                break;
        }
    }

    private String getName(){
        return account_et.getText().toString();
    }

    private String getPassword(){
        return password_et.getText().toString();
    }

    private String getPasswordAgain(){
        return password_again_et.getText().toString();
    }

    public void register(){
        AccountsInfo info = new AccountsInfo();
        info.mAccounts = getName();
        info.mPassword = getPassword();
        DBManagerClient.insertAccounts(info);
        SharedPref.getInstance().setSessionId("123");
        TourPalApplication.mHasLogin = true;
    }

    public static void start(Activity activity){
        Intent intent = new Intent(activity,AddNewAccountActivity.class);
        activity.startActivity(intent);
    }
}
