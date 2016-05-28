package com.elong.tourpal.ui.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.ui.views.CommonDialog;
import com.elong.tourpal.utils.Utils;

import java.util.ArrayList;

public class AboutActivity extends ActivityBase {

    private TextView mTvAppName;
    private TextView mTvAppVersion;
    private TextView mTvWechat;
    private TextView mTvWeibo;
    private ImageView mIvLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initViews();
    }

    private void initViews() {
        initTitle();
        mIvLogo = (ImageView) findViewById(R.id.about_iv_logo);
        mIvLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Env.DEBUG) {
                    showChangeRequestEnvDlg();
                }
            }
        });
        mTvAppName = (TextView) findViewById(R.id.about_app_name);
        mTvAppVersion = (TextView) findViewById(R.id.about_app_version);
        mTvAppVersion.setText(Env.getVersionName(getApplicationContext()));
        mTvWechat = (TextView) findViewById(R.id.about_tv_wechat);
        mTvWechat.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                Utils.copy2ClipBoard(AboutActivity.this, getString(R.string.about_wechat_id));
                Toast.makeText(AboutActivity.this, R.string.about_copy_done, Toast.LENGTH_SHORT).show();
            }
        });
        mTvWeibo = (TextView) findViewById(R.id.about_tv_weibo);
        mTvWeibo.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                Utils.copy2ClipBoard(AboutActivity.this, getString(R.string.about_weibo_id));
                Toast.makeText(AboutActivity.this, R.string.about_copy_done, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initTitle() {
        setTitleText(R.string.about_title);
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitleRightBtnEnable(false);
    }

    private void showChangeRequestEnvDlg() {
        CommonDialog dlg = new CommonDialog(this);
        dlg.setTitle(R.string.dlg_title_change_env);
        ArrayList<String> datas = new ArrayList<>();
        datas.add("测试");
        datas.add("线上");
        dlg.setContentList(datas, new CommonDialog.ItemSelectListener() {
            @Override
            public void onItemSelect(ArrayList<String> datas, int position) {
                String itemData = datas.get(position);
                if (itemData.equals("测试")) {
                    RequestBuilder.isDebugEnv = true;
                } else {
                    RequestBuilder.isDebugEnv = false;
                }
            }
        });
        dlg.show();
    }

}
