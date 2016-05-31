package com.elong.tourpal.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import com.elong.tourpal.R;

/**
 * Created by LuoChangAn on 16/5/18.
 */
public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    TextView title_tv;

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.item_login:
                break;
            case R.id.add_new_account:
                AddNewAccountActivity.start(LoginActivity.this);
                break;
            default:
                break;
        }
    }

    public static void start(Activity activity){
        Intent intent = new Intent(activity,LoginActivity.class);
        activity.startActivity(intent);
    }
}
