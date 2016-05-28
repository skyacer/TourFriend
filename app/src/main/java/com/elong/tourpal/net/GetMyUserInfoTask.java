package com.elong.tourpal.net;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.utils.SharedPref;

/**
 * GetMyUserInfoTask
 */
public class GetMyUserInfoTask extends AsyncTask<Integer, Integer, MessageProtos.UserInfo> {
    @Override
    protected MessageProtos.UserInfo doInBackground(Integer... params) {
        Request request = RequestBuilder.buildGetMyUserInfoRequest();
        MessageProtos.ResponseInfo responseInfo = request.get();
        if (responseInfo != null && responseInfo.getErrCode() == MessageProtos.SUCCESS) {
            MessageProtos.UserInfo userInfo = responseInfo.getUserInfo();
            if (userInfo != null) {
                SharedPref.getInstance().setMyUserInfo(userInfo);
                Intent updateUserInfoIntent = new Intent(Actions.ACTION_UPDATE_MY_USER_INFO);
                LocalBroadcastManager.getInstance(TourPalApplication.getAppContext()).sendBroadcast(updateUserInfoIntent);
                return userInfo;
            }
        }
        return null;
    }
}
