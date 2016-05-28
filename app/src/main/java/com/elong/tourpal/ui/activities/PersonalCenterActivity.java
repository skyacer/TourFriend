package com.elong.tourpal.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.fragments.PersonalCenterFragment;
import com.elong.tourpal.utils.SharedPref;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;

public class PersonalCenterActivity extends ActivityBase {
    private static final String TAG = PersonalCenterActivity.class.getSimpleName();

    FragmentManager mFragmentManager = null;

    /**
     * 启动此activity
     *
     * @param userInfo MessageProtos.UserInfo 序列化后的字符串
     */
    public static void startActivity(Context context, String userInfo) {
        //先判断是否为自己的userInfo，若为自己的userInfo，则跳转到“我的”页面
        MessageProtos.UserInfo myInfo = SharedPref.getInstance().getMyUserInfo();
        MessageProtos.UserInfo newInfo = null;
        try {
            newInfo = MessageProtos.UserInfo.parseFrom(userInfo.getBytes());
        } catch (InvalidProtocolBufferMicroException e) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }
        if (myInfo != null && newInfo != null) {
            String myUid = myInfo.getId();
            if (myUid != null && myUid.equals(newInfo.getId())) {
                //跳转到“我的”页面
                Intent intent = new Intent(context, MainTabsActivity.class);
                intent.putExtra(Extras.EXTRA_MAIN_TAB_START_TAB_IDX, MainTabsActivity.IDX_PERSONAL_CENTER);
                context.startActivity(intent);
                return;
            }
        }
        Intent intent = new Intent(context, PersonalCenterActivity.class);
        intent.putExtra(Extras.EXTRA_USER_INFO, userInfo);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_center);
        mFragmentManager = getSupportFragmentManager();
        if (!getIntent().hasExtra(Extras.EXTRA_USER_INFO)) {
            finish();
        }
        String userInfo = getIntent().getStringExtra(Extras.EXTRA_USER_INFO);
        initFragment(PersonalCenterFragment.newInstance(userInfo));
        initTitle();
    }

    public void initTitle() {
        setTitleText(R.string.pc_title);
        setTitleRightBtnEnable(false);
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void initFragment(Fragment fragment) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.pc_fragment_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
