package com.elong.tourpal.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.activities.MainTabsActivity;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.Utils;

/**
 * NotificationHelper
 */
public class PushHelper {

    public static int REQUEST_CODE_PUSH_NOTIFICATION = 101;

    public static int NOTIFICATION_ID_SYSTEM_BASE = 1000;
    public static int NOTIFICATION_ID_WANNA_JOIN = 2000;

    public static int NOTIFICATION_ID_INC = 0;

    /**
     * 收到push消息的处理
     *
     * @param context context
     * @param msg     MessageProtos.PushMessage
     */
    public static void handlePushMsg(Context context, MessageProtos.PushMessage msg) {
        SharedPref pref = SharedPref.getInstance();
        int msgType = msg.getType();
        boolean isSettingMsgVoiceOn = pref.isSettingMsgVoiceOn();
        boolean isSettingMsgVibrationOn = pref.isSettingMsgVibrationOn();
        if (msgType == MessageProtos.WANNA_JOIN) {
            if (!TourPalApplication.getInstance().hasLogin()) {
                //未登录，若收到求同行消息，则抛弃，此处为边界处理
                return;
            }
            pref.setHasNewMessage(true);//记录有新消息
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Actions.ACTION_RECEIVE_NEW_MESSAGE));
            if (Utils.isApplicationOnTop(context)) {
                //点赞消息，在本app处于栈顶的时候，不弹出通知栏通知，但是根据设置来进行震动和声音播放
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (isSettingMsgVibrationOn) {
                    vibrator.vibrate(new long[]{0, 100, 300, 100}, -1);
                }
                if (isSettingMsgVoiceOn) {
                    playSound(context);
                }
                return;
            }
        }

        String msgTitle = msg.getTitle();
        if (msgTitle == null) {
            msgTitle = "";
        }
        String msgContent = msg.getContent();
        if (msgContent == null) {
            msgContent = "";
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        Intent intent = new Intent(context, MainTabsActivity.class);
        intent.putExtra(Extras.EXTRA_MAIN_TAB_START_TAB_IDX, MainTabsActivity.IDX_MSG);
        PendingIntent clickIntent = PendingIntent.getActivity(context, REQUEST_CODE_PUSH_NOTIFICATION, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_tourpal);

        int defaultNotifySetting = Notification.DEFAULT_LIGHTS;
        if (isSettingMsgVoiceOn) {
            playSound(context);
        }
        if (isSettingMsgVibrationOn) {
            defaultNotifySetting |= Notification.DEFAULT_VIBRATE;
        }
        mBuilder.setContentTitle(msgTitle)//设置通知栏标题
                .setContentText(msgContent) //设置通知栏显示内容
                .setContentIntent(clickIntent) //设置通知栏点击意图
//                .setNumber(number) //设置通知集合的数量
                .setTicker(msgContent) //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(defaultNotifySetting)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.ic_tourpal)//设置通知小ICON
                .setLargeIcon(icon);


        Notification notification = mBuilder.build();
        if (msgType == MessageProtos.WANNA_JOIN) {
            notificationManager.notify(NOTIFICATION_ID_WANNA_JOIN, notification);
        } else if (msgType == MessageProtos.SYSTEM) {
            notificationManager.notify(NOTIFICATION_ID_SYSTEM_BASE + NOTIFICATION_ID_INC, notification);
            NOTIFICATION_ID_INC++;
        }
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    private static void playSound(Context context) {
        final SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        final int soundId = soundPool.load(context, R.raw.msg, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        });
    }

}
