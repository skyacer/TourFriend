package com.elong.tourpal.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.protocal.MessageProtos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * 通用工具类
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static byte[] MD5(byte[] input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }
        if (md != null) {
            md.update(input);
            return md.digest();
        } else {
            return null;
        }
    }

    public static String getMD5(byte[] input) {
        return Utils.bytesToHexString(MD5(input));
    }

    public static String getMD5(String input) {
        return getMD5(input.getBytes());
    }

    /**
     * Converts a byte array into a String hexidecimal characters null returns
     * null
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        String table = "0123456789abcdef";
        int len = 2 * bytes.length;
        char[] cchars = new char[len];//不再使用StringBuilder,使用char数组优化速度，dmtrace 发现string.append最耗时
        for (int i = 0, k = 0; i < bytes.length; i++, k++) {
            int b;
            b = 0x0f & (bytes[i] >> 4);
            cchars[k] = table.charAt(b);
            b = 0x0f & bytes[i];
            k++;
            cchars[k] = table.charAt(b);
        }
        String sret = String.valueOf(cchars);
        cchars = null;
        return sret;
    }

    public static void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
        }
    }

    public static void closeOutputStream(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
        }
    }

    /**
     * 检测网络是否可用
     *
     * @param context context
     * @return true则网络可用
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean canListScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return view.canScrollVertically(-1);
        }
    }

    public static boolean canListPullDown(View view) {
        return !canListScrollUp(view);
    }

    public static boolean isApplicationOnTop(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT < 21) {
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            String packageName = cn.getPackageName();
            if (packageName != null) {
                return packageName.equals(context.getPackageName());
            }
        } else {
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            int myPid = android.os.Process.myPid();
            int topPid = tasks.get(0).pid;
            if (Env.DEBUG) {
                Log.i(TAG, "top_pid" + String.valueOf(topPid));
                Log.i(TAG, "my_pid" + String.valueOf(myPid));
            }
            return myPid == topPid;
        }
        return false;
    }

    /**
     * 根据用户id来分配一个头像
     *
     * @param userId 用户id
     * @return 头像的资源id
     */
    public static int getAvatarId(int userId) {
        if (userId == -1) {
            //使用默认头像
            return R.mipmap.avatar_default;
        } else {
            int id = userId % 4;
            switch (id) {
                case 0:
                    return R.mipmap.avatar0;
                case 1:
                    return R.mipmap.avatar1;
                case 2:
                    return R.mipmap.avatar2;
                case 3:
                    return R.mipmap.avatar3;
            }
            return 0;
        }
    }

    /**
     * 跳转到系统拨号界面
     *
     * @param context     context
     * @param phoneNumber 电话号码
     */
    public static void dial(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    /**
     * 复制到剪切板
     *
     * @param context context
     * @param text    文本
     */
    public static void copy2ClipBoard(Context context, String text) {
        if (Build.VERSION.SDK_INT < 11) {
            android.text.ClipboardManager cm = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(text);
        } else {
            android.content.ClipboardManager cm = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(text);
        }
    }

    /**
     * 当前用户资料中昵称是否不为空
     *
     * @param userInfo 若传进来的为空，则取SharedPref中记录的
     * @return 昵称不为空，返回true
     */
    public static boolean isMyUserInfoHasNickname(MessageProtos.UserInfo userInfo) {
        MessageProtos.UserInfo info = null;
        if (userInfo == null) {
            info = SharedPref.getInstance().getMyUserInfo();
        }
        if (info != null) {
            if (!TextUtils.isEmpty(info.getNickName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当前用户资料中昵称是否不为空
     *
     * @return 昵称不为空，返回true
     */
    public static boolean isMyUserInfoHasNickname() {
        return isMyUserInfoHasNickname(null);
    }

    public static String getFormattedTime(long timeStamp){
        Timestamp time = new Timestamp(timeStamp);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        return dateFormat.format(time);
    }

    public static boolean isUrl(String path) {
        try {
            URL url = new URL(path);
            if (Env.DEBUG) {
                Log.d(TAG, "是url路径");
            }
            return true;
        } catch (MalformedURLException e) {
            if (Env.DEBUG) {
                Log.d(TAG, "是本地file路径");
            }
        }
        return false;
    }

}
