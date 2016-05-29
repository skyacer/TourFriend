package com.elong.tourpal.utils;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.AppProfile;


/**
 * Created by LuoChangAn on 16/4/1.
 */
public class ToastUtil {
    private static Toast toast = Toast.makeText(AppProfile.getContext(), "", Toast.LENGTH_LONG);
    private static View toastView;
    private static TextView toastTextView;
    private static boolean isShowing = false;
    private static String lastToastContent = "";

    private static int shortDuration = 1000;
    private static int longDuration = 2000;

    // 显示短提示
    public static void makeShortToast(String content) {
        makeToast(content, shortDuration);
    }

    // 显示短提示
    public static void makeShortToast(int stringId) {
        makeShortToast(ResourcesUtil.getString(stringId));
    }

    // 显示短提示
    public static void makeShortToast(int stringId, Object... objects) {
        String content = ResourcesUtil.stringFormat(stringId, objects);
        makeShortToast(content);
    }

    // 显示长提示
    public static void makeLongToast(String content) {
        makeToast(content, longDuration);
    }

    private static void makeToast(String content, int duration) {
        // 如果是非主线程并且不是
        if (!ThreadUtil.isMainLoop()) {
            return;
        }

        if (!initToastView(content)) {
            return;
        }
        toastTextView.setText(content);
        handleHide(duration);
    }

    // 显示长提示
    public static void makeLongToast(int stringId) {
        makeLongToast(ResourcesUtil.getString(stringId));
    }

    // 显示长提示
    public static void makeLongToast(int stringId, Object... objects) {
        String content = ResourcesUtil.stringFormat(stringId, objects);
        makeLongToast(content);
    }

    private static boolean initToastView(String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        if (toastView == null) {
            toastView = View.inflate(AppProfile.getContext(), R.layout.view_toast, null);
            toastTextView = (TextView) toastView.findViewById(R.id.toast_text);
            toast.setView(toastView);
        }
        if (content.equals(lastToastContent) && isShowing) {
            return false;
        } else {
            lastToastContent = content;
            return true;
        }
    }

    private static void handleHide(final long delay) {
        //直接调用会导致显示不了
        if (isShowing) {
            toast.cancel();
        }
        //为了让上一个先消失，不然的话，可能下一个show不出来
        HandlerUtil.doDelay(new Runnable() {
            @Override
            public void run() {
                isShowing = true;
                toast.show();
                HandlerUtil.doDelay(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                        isShowing = false;
                    }
                }, delay);
            }
        }, 300);
    }
}
