package com.elong.tourpal.application;

import android.app.Application;
import android.content.Context;

import com.elong.tourpal.module.file.TagsFileManager;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.utils.SharedPref;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;

/**
 * TourPalApplication
 */
public class TourPalApplication extends Application {

    private static TourPalApplication sInstance;

    private boolean mHasLogin = false;

    private static final String WECHAT_APP_ID = "wxd6fb3bb381c0f8f5";
    private static final String QQ_APP_ID = "1104524037";
    private IWXAPI mWXapi;
    private Tencent mQQapi;

    public static Context getAppContext(){
        return sInstance.getApplicationContext();
    }

    public static TourPalApplication getInstance(){
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        initLoginState();
        regToWx();
        mQQapi = Tencent.createInstance(QQ_APP_ID, this);

        MessageProtos.UserInfo userInfo = MessageProtos.UserInfo.getUserInfo();
        SharedPref.getInstance().setMyUserInfo(userInfo);

        // 初始化帖子标签数据
        TagsFileManager.initPostTags();
        //打点数据
        Statistics.log(this, StatisticsEnv.APP_OPEN);

        AppProfile.sContext = getApplicationContext();

    }

    /**
     * 获取本地登录信息，粗略判断是否登录
     */
    private void initLoginState(){
//        SharedPref pref = SharedPref.getInstance();
//        if (!TextUtils.isEmpty(pref.getSessionId()) && !TextUtils.isEmpty(pref.getSessionId())){
//            mHasLogin = true;
//        } else {
//            mHasLogin = true;
//        }
    }

    /**
     * 是否已经登录（有sessionId和sessionToken）
     * @return bool
     */
    public boolean hasLogin() {
        return mHasLogin;
    }

    /**
     * 设置是否已经登录（条件：有sessionId和sessionToken）
     */
    public void setHasLogin(boolean hasLogin) {
        mHasLogin = hasLogin;
    }

    /**
     * 将应用注册到微信
     */
    private void regToWx() {
        mWXapi = WXAPIFactory.createWXAPI(this, WECHAT_APP_ID, true);
        // 将应用appId注册到微信
        mWXapi.registerApp(WECHAT_APP_ID);
    }

    public IWXAPI getWXapi() {
        return mWXapi;
    }

    public Tencent getQQapi() {
        return mQQapi;
    }

}
