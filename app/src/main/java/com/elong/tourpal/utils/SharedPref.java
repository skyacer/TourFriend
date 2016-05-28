package com.elong.tourpal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.natives.NativeUtils;
import com.elong.tourpal.protocal.MessageProtos;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;
import com.igexin.sdk.PushManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认SharedPreference
 */
public class SharedPref extends SharedPrefBase {
    private static final String TAG = SharedPref.class.getSimpleName();

    protected SharedPref(Context context){
        super(context, DEFAULT_SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    private static final String DEFAULT_SHARED_PREF_NAME  = "default";
    public static final String KEY_SC_LOAD_DESTINATION_FLAG = "key_load_destination_filename";
    public static final String KEY_UPLOAD_STATISTICS_DATA_TIME = "key_upload_statistics_data_time";
    public static final String KEY_LAST_DOWNLOAD_DESTINATION_FILE = "key_last_download_destination_file";
    public static final String KEY_LAST_UPDATE_DESTINATION_FILE = "key_last_update_destination_file";
    public static final String KEY_LAST_DOWNLOAD_TAGS_FILE = "key_last_download_tags_file";

    private static SharedPref sInstance = null;

    private SharedPreferences mPreferences = null;

    public static SharedPref getInstance(){
        if (sInstance == null) {
            Context context = TourPalApplication.getAppContext();
            sInstance = new SharedPref(context);
        }
        return sInstance;
    }

    private static final String PREF_SESSION_ID = "pref_session_id";
    public void setSessionId(String sessionId){
        try {
            byte[] eData = CodecUtils.encode(NativeUtils.getStoreKey(), sessionId.getBytes());
            String bData = CodecUtils.encryptBASE64(eData);
            setString(PREF_SESSION_ID, bData);
        } catch (Exception e) {
            if (Env.DEBUG){
                Log.e(TAG, "e:", e);
            }
        }
    }

    public String getSessionId(){
        String bData = getString(PREF_SESSION_ID, "");
        byte[] eData = CodecUtils.decryptBASE64(bData);
        if (eData != null){
            try {
                byte[] rawData = CodecUtils.decode(NativeUtils.getStoreKey(), eData);
                return new String(rawData);
            } catch (Exception e) {
                if (Env.DEBUG){
                    Log.e(TAG, "e:", e);
                }
            }
        }
        return null;
    }

    private static final String PREF_SESSION_TOKEN = "pref_session_token";
    public void setSessionToken(String sessionToken){
        try {
            byte[] eData = CodecUtils.encode(NativeUtils.getStoreKey(), sessionToken.getBytes());
            String bData = CodecUtils.encryptBASE64(eData);
            setString(PREF_SESSION_TOKEN, bData);
        } catch (Exception e) {
            if (Env.DEBUG){
                Log.e(TAG, "e:", e);
            }
        }
    }

    public String getSessionToken(){
        String bData = getString(PREF_SESSION_TOKEN, "");
        byte[] eData = CodecUtils.decryptBASE64(bData);
        if (eData != null){
            try {
                byte[] rawData = CodecUtils.decode(NativeUtils.getStoreKey(), eData);
                return new String(rawData);
            } catch (Exception e) {
                if (Env.DEBUG){
                    Log.e(TAG, "e:", e);
                }
            }
        }
        return null;
    }

    private static final String PREF_LAST_HOT_DEST_UPDATE_TIME = "pref_last_hot_dest_update_time";
    public void setLastHotDestUpdateTime(long timeMilliSecond){
        setLong(PREF_LAST_HOT_DEST_UPDATE_TIME, timeMilliSecond);
    }

    public long getLastHotDestUpdateTime(){
        return getLong(PREF_LAST_HOT_DEST_UPDATE_TIME, 0);
    }

    private static final String PREF_LAST_HOT_DESTS = "pref_last_hot_dests";
    public void setLastHotDests(List<MessageProtos.HotCity> lastHotDests) {
        JSONArray array = new JSONArray();
        try {
            int len = lastHotDests.size();
            for (int i = 0; i < len; i++) {
                array.put(i, new String(lastHotDests.get(i).toByteArray()));
            }
        } catch (JSONException e) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }
        setString(PREF_LAST_HOT_DESTS, array.toString());
    }

    public List<MessageProtos.HotCity> getLastHotDests(){
        List<MessageProtos.HotCity> hotDests = new ArrayList<>();
        String destsStr = getString(PREF_LAST_HOT_DESTS, "");
        if (!TextUtils.isEmpty(destsStr)){
            try {
                JSONArray array = new JSONArray(destsStr);
                int len = array.length();
                for (int i = 0; i < len; i++) {
                    String destStr = array.getString(i);
                    MessageProtos.HotCity dest = MessageProtos.HotCity.parseFrom(destStr.getBytes());
                    hotDests.add(dest);
                }
            } catch (JSONException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            } catch (InvalidProtocolBufferMicroException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
        }
        return hotDests;
    }

    private static final String PREF_MY_USER_INFO = "pref_my_user_info";
    public void setMyUserInfo(MessageProtos.UserInfo myUserInfo) {
        try {
            byte[] eData = CodecUtils.encode(NativeUtils.getStoreKey(), myUserInfo.toByteArray());
            String bData = CodecUtils.encryptBASE64(eData);
            setString(PREF_MY_USER_INFO, bData);
        } catch (Exception e) {
            if (Env.DEBUG){
                Log.e(TAG, "e:", e);
            }
        }
    }

    public MessageProtos.UserInfo getMyUserInfo(){
        String bData = getString(PREF_MY_USER_INFO, "");
        byte[] eData = CodecUtils.decryptBASE64(bData);
        if (eData != null){
            try {
                byte[] rawData = CodecUtils.decode(NativeUtils.getStoreKey(), eData);
                return MessageProtos.UserInfo.parseFrom(rawData);
            } catch (Exception e) {
                if (Env.DEBUG){
                    Log.e(TAG, "e:", e);
                }
            }
        }
        return null;
    }

    private static final String PREF_SETTING_MSG_VOICE = "pref_setting_msg_voice";
    public void setSettingMsgVoiceOn(boolean isMsgVoiceOn){
        setBoolean(PREF_SETTING_MSG_VOICE, isMsgVoiceOn);
    }

    public boolean isSettingMsgVoiceOn(){
        return getBoolean(PREF_SETTING_MSG_VOICE, true);
    }

    private static final String PREF_SETTING_MSG_VIBRATION = "pref_setting_msg_vibration";
    public void setSettingMsgVibrationOn(boolean isMsgVoiceOn){
        setBoolean(PREF_SETTING_MSG_VIBRATION, isMsgVoiceOn);
    }

    public boolean isSettingMsgVibrationOn(){
        return getBoolean(PREF_SETTING_MSG_VIBRATION, true);
    }

    private static final String PREF_SETTING_PUSH = "pref_setting_push";
    public void setSettingPushOn(boolean isMsgVoiceOn){
        if (isMsgVoiceOn){
            PushManager.getInstance().turnOnPush(TourPalApplication.getAppContext());
        } else {
            PushManager.getInstance().turnOffPush(TourPalApplication.getAppContext());
        }
        setBoolean(PREF_SETTING_PUSH, isMsgVoiceOn);
    }

    public boolean isSettingPushOn(){
        return getBoolean(PREF_SETTING_PUSH, true);
    }

    private static final String PREF_PUSH_CLIENT_ID = "pref_push_client_id";
    public void setPushClientId(String pushClientId){
        setString(PREF_PUSH_CLIENT_ID, pushClientId);
    }

    public String getPushClientId(){
        return getString(PREF_PUSH_CLIENT_ID, "");
    }

    private static final String PREF_HAS_NEW_MESSAGE = "pref_has_new_message";
    public void setHasNewMessage(boolean has){
        setBoolean(PREF_HAS_NEW_MESSAGE, has);
    }
    public boolean hasNewMessage(){
        return getBoolean(PREF_HAS_NEW_MESSAGE, false);
    }

    //首次启动
    private static final String PREF_FIRST_LAUNCH = "pref_first_launch";
    public boolean isFirstLaunch(){
        return getBoolean(PREF_FIRST_LAUNCH, true);
    }
    public void setFirstLaunch(boolean val){
        setBoolean(PREF_FIRST_LAUNCH, val);
    }

    //是否弹过求同行时提示修改公开信息的弹窗
    private static final String PREF_HAS_SHOW_JOIN_TIP_DLG = "pref_has_show_join_tip_dlg";
    public boolean hasShowJoinTipDlg() {
        return getBoolean(PREF_HAS_SHOW_JOIN_TIP_DLG, false);
    }
    public void setHasShowJoinTipDlg() {
        setBoolean(PREF_HAS_SHOW_JOIN_TIP_DLG, true);
    }

}
