package com.elong.tourpal.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.elong.tourpal.application.TourPalApplication;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

/**
 * Created by zhitao.xu on 2015/5/13.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TourPalApplication.getInstance().getWXapi().handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        TourPalApplication.getInstance().getWXapi().handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.e("wx", baseReq.toString());
        finish();

    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.e("wx", baseResp.toString());
        finish();
    }
}
