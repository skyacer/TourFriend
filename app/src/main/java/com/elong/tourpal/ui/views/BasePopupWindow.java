package com.elong.tourpal.ui.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * BasePopupWindow
 */
public class BasePopupWindow {

    protected Context mContext;
    protected PopupWindow mWindow;
    protected View mRootView;
    protected Drawable mBackground = null;
    protected WindowManager mWindowManager;

    public BasePopupWindow(Context context) {
        mContext = context;
        mWindow = new PopupWindow(context);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);// 保证点击空白的地方popup window 消失
        mWindow.setOutsideTouchable(true);// 保证点击空白的地方popup window 消失
        //保证点击空白的地方popup window 消失
        mWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void setContentView(View root) {
        mRootView = root;
        mWindow.setContentView(root);
    }

    public void setContentView(int layoutResID) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflater.inflate(layoutResID, null, false));
    }

    public void dismiss() {
        if (mWindow != null && mWindow.isShowing()) {
            mWindow.dismiss();
        }
    }

    protected void preShow(boolean needFocus) {
        if (mRootView == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }
        if (mBackground == null) {
            mWindow.setBackgroundDrawable(new BitmapDrawable());
        } else {
            mWindow.setBackgroundDrawable(mBackground);
        }

        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setTouchable(true);
        if (needFocus){
            mWindow.setFocusable(true);//保证点击空白的地方有响应
        }else{
            mWindow.setFocusable(false);
        }
        mWindow.setOutsideTouchable(true);//保证点击空白的地方popup window 消失

        mWindow.setContentView(mRootView);
    }

}
