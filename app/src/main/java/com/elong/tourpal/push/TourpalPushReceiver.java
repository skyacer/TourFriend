package com.elong.tourpal.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.utils.SharedPref;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;
import com.igexin.sdk.PushConsts;

/**
 * PushInitFinishReceiver
 * <p/>
 * 用于监听个推的初始化完成广播
 */
public class TourpalPushReceiver extends BroadcastReceiver {

    private static final String TAG = "TourpalPushReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SharedPref pref = SharedPref.getInstance();
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_CLIENTID:
                final String clientId = bundle.getString("clientid");
                if (!TextUtils.isEmpty(clientId)) {
                    pref.setPushClientId(clientId);
                    if (TourPalApplication.getInstance().hasLogin()) {
                        //若已经处于登录状态，则绑定
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Request request = RequestBuilder.buildBindPushClientIdRequest();
                                if (request != null) {
                                    MessageProtos.ResponseInfo respInfo = request.get();
                                    if (respInfo != null) {
                                        if (Env.DEBUG) {
                                            Log.d(TAG, "bind push client id result: err_code=" + respInfo.getErrCode());
                                        }
                                    }
                                }
                            }
                        });
                        thread.start();
                    }
                    if (Env.DEBUG) {
                        Log.e(TAG, clientId);
                    }
                } else {
                    if (Env.DEBUG) {
                        Log.e(TAG, "clientId == null");
                    }
                }
                break;
            case PushConsts.GET_MSG_DATA:
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null && pref.isSettingPushOn()) {
                    MessageProtos.PushMessage msg = null;
                    try {
                        msg = MessageProtos.PushMessage.parseFrom(payload);
                    } catch (InvalidProtocolBufferMicroException e) {
                        if (Env.DEBUG) {
                            Log.e(TAG, "e:", e);
                        }
                    }
                    if (msg != null) {
                        if (Env.DEBUG) {
                            Log.e(TAG, "msg title:" + msg.getTitle() + " content:" + msg.getContent());
                        }
                        PushHelper.handlePushMsg(context.getApplicationContext(), msg);
                    }
                }
                break;
        }
    }
}
