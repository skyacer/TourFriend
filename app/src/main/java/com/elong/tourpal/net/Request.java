package com.elong.tourpal.net;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.protocal.ProtocUtils;
import com.elong.tourpal.ui.activities.LoginWebviewActivity;
import com.elong.tourpal.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Request
 *
 * @author tao.chen1
 */
public class Request {
    private static final String TAG = "Request";

    private static final int CONNECTION_TIME_OUT = 10000;
    private static final int READ_TIME_OUT = 10000;

    private MessageProtos.RequestInfo requestInfo;
    private String requestUrl;

    public MessageProtos.RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(MessageProtos.RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * 发送get请求
     *
     * @return MessageProtos.ResponseInfo
     */
    public MessageProtos.ResponseInfo get() {
        if (requestInfo != null) {
            String httpGetData = ProtocUtils.getHttpGetData(requestInfo);
            if (httpGetData != null) {
                HttpURLConnection conn = null;
                InputStream is = null;
                ByteArrayOutputStream bos = null;
                try {
                    URL url = new URL(requestUrl + httpGetData);
                    if (Env.DEBUG) {
                        Log.e(TAG, url.toString());
                    }
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(CONNECTION_TIME_OUT);
                    conn.setReadTimeout(READ_TIME_OUT);
                    conn.connect();
                    is = conn.getInputStream();
                    bos = new ByteArrayOutputStream();
                    byte[] bytes = new byte[8192];
                    int count = 0;
                    while ((count = is.read(bytes)) > 0) {
                        bos.write(bytes, 0, count);
                    }
                    MessageProtos.ResponseInfo respInfo = ProtocUtils.parseResponseInfo(bos.toString());
                    handleCommonResponseInfo(respInfo);
                    return respInfo;
                } catch (Exception e) {
                    if (Env.DEBUG) {
                        Log.e(TAG, "e:", e);
                    }
                } finally {
                    Utils.closeInputStream(is);
                    Utils.closeOutputStream(bos);
                }
            }
        }
        return null;
    }

    /**
     * 发送post请求
     *
     * @return MessageProtos.ResponseInfo
     */
    public MessageProtos.ResponseInfo post() {
        if (requestInfo != null) {
            String httpPostData = ProtocUtils.getHttpPostData(requestInfo);
            if (httpPostData != null) {
                InputStream is = null;
                OutputStream os = null;
                HttpURLConnection conn = null;
                ByteArrayOutputStream bos = null;
                try {
                    URL url = new URL(requestUrl);
                    if (Env.DEBUG) {
                        Log.e(TAG, requestUrl);
                    }
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(CONNECTION_TIME_OUT);
                    conn.setReadTimeout(READ_TIME_OUT);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Length", String.valueOf(httpPostData.length()));
                    conn.setRequestMethod("POST");
                    os = conn.getOutputStream();
                    if (os != null) {
                        byte[] outputData = httpPostData.getBytes();
                        os.write(outputData, 0, outputData.length);
                        os.flush();
                        os.close();
                        int responseCode = conn.getResponseCode();
                        if (responseCode == 200) {
                            is = conn.getInputStream();
                            bos = new ByteArrayOutputStream();
                            byte[] bytes = new byte[8192];
                            int count = 0;
                            while ((count = is.read(bytes)) > 0) {
                                bos.write(bytes, 0, count);
                            }
                            MessageProtos.ResponseInfo respInfo = ProtocUtils.parseResponseInfo(bos.toString());
                            handleCommonResponseInfo(respInfo);
                            return respInfo;
                        }
                    }
                } catch (Exception e) {
                    if (Env.DEBUG) {
                        Log.e(TAG, "e:", e);
                    }
                } finally {
                    Utils.closeInputStream(is);
                    Utils.closeOutputStream(os);
                    Utils.closeOutputStream(bos);
                }
            }
        }
        return null;
    }

    private void handleCommonResponseInfo(MessageProtos.ResponseInfo respInfo) {
        if (respInfo != null) {
            if (respInfo.getErrCode() == MessageProtos.ERROR_NOT_LOGIN) {
                //未登录的时候直接跳转到登录页面
                Context appContext = TourPalApplication.getAppContext();
                Intent intent = new Intent(appContext, LoginWebviewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(intent);
            }
        }
    }

}
