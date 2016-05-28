package com.elong.tourpal.protocal;

import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.natives.NativeUtils;
import com.elong.tourpal.utils.CodecUtils;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 */
public class ProtocUtils {
    private static final String TAG = ProtocUtils.class.getSimpleName();
    private static final String HTTP_GET_PARAM_FORMAT = "?d=%s&v=%s";

    public static String encryptData(byte[] data) {
        String key = NativeUtils.getReqDESKey8();
        try {
            byte[] enData = CodecUtils.encode(key, data);
            String baseData = CodecUtils.encryptBASE64(enData);
            return baseData;
        } catch (Exception e) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }
        return null;
    }

    public static byte[] decryptData(String data) {
        try {
            byte[] enData = CodecUtils.decryptBASE64(data);
            byte[] rawData = CodecUtils.decode(NativeUtils.getReqDESKey8(), enData);
            return rawData;
        }  catch (Exception e) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }
        return null;
    }

    public static String getHttpPostData(MessageProtos.RequestInfo requestInfo) {
        String data = ProtocUtils.encryptData(requestInfo.toByteArray());
        String vStr = NativeUtils.getVerifyString(data);
        if (data != null && vStr != null) {
            try {
                String urlData = URLEncoder.encode(data, "UTF-8");
                String urlVStr = URLEncoder.encode(vStr, "UTF-8");
                StringBuilder sb = new StringBuilder();
                sb.append("d=");
                sb.append(urlData);
                sb.append("&v=");
                sb.append(urlVStr);
                return sb.toString();
            } catch (UnsupportedEncodingException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
        }
        return null;
    }

    public static String getHttpGetData(MessageProtos.RequestInfo requestInfo) {
        String data = ProtocUtils.encryptData(requestInfo.toByteArray());
        String vStr = NativeUtils.getVerifyString(data);
        if (data != null && vStr != null) {
            try {
                String urlData = URLEncoder.encode(data, "UTF-8");
                String urlVStr = URLEncoder.encode(vStr, "UTF-8");
                return String.format(HTTP_GET_PARAM_FORMAT, urlData, urlVStr);
            } catch (UnsupportedEncodingException e) {
                if (Env.DEBUG){
                    Log.e(TAG, "e:", e);
                }
            }
        }
        return null;
    }

    public static MessageProtos.ResponseInfo parseResponseInfo(String respData) {
        MessageProtos.ResponseInfo responseInfo = null;
        byte[] rawData = decryptData(respData);
        if (rawData != null) {
            try {
                responseInfo = MessageProtos.ResponseInfo.parseFrom(rawData);
            } catch (InvalidProtocolBufferMicroException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
        }
        return responseInfo;
    }

}
