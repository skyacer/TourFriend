package com.elong.tourpal.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.utils.Utils;
import com.tencent.open.utils.Util;

/**
 * Created by zhitao.xu on 2015/5/25.
 */
public class SplashActivity extends Activity {
    private ImageView mImageView;
    private View m360ZhushouLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        m360ZhushouLayout = findViewById(R.id.splash_360zhushou);
        if (Env.CHANNEL_QIHOO360.equals(Env.getChannelId(this))) {
            // 是360手机助手渠道
            m360ZhushouLayout.setVisibility(View.VISIBLE);
        } else {
            m360ZhushouLayout.setVisibility(View.GONE);
        }


        mImageView = (ImageView) findViewById(R.id.splash_iv);
        mImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainTabsActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1500);

    }
}
