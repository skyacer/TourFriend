package com.elong.tourpal.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.elong.tourpal.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhitao.xu on 2015/4/14.
 */
public class CommonToastDialog extends Dialog {
    public static final int LENGTH_LONG = 3000;
    public static final int LENGTH_SHORT = 1000;
    private Context mContext;
    private ImageView mDialogIcon;
    private TextView mDialogTitle;
    private boolean mIsLoading = false;
    private long mShowDuration = -1;

    public CommonToastDialog(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public CommonToastDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        initView();
    }

    protected CommonToastDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        initView();
    }

    private void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.common_toast_dialog);
        mDialogIcon = (ImageView) findViewById(R.id.common_dialog_iv);
        mDialogTitle = (TextView) findViewById(R.id.common_dialog_tv);

        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        LayoutParams params = window.getAttributes();
        window.setGravity(Gravity.TOP);
        params.y = mContext.getResources().getDimensionPixelSize(R.dimen.common_dialog_margin_top);
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.WRAP_CONTENT;
        params.dimAmount = 0.0f;
        params.alpha = 0.7f;
        params.horizontalMargin = mContext.getResources().getDimensionPixelSize(R.dimen.common_dialog_margin_left_right);
//        params.verticalMargin = mContext.getResources().getDimensionPixelSize(R.dimen.common_dialog_margin_top);
        window.setAttributes(params);
    }

    @Override
    public void show() {
        if (mIsLoading) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.loading_anim);
            mDialogIcon.startAnimation(animation);
        } else {
            mDialogIcon.clearAnimation();
        }

        if (mShowDuration > 0) {
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    dismiss();
                }
            };
            Timer timer = new Timer();
            timer.schedule(tt, mShowDuration);
        }
        super.show();
    }

    public void setmDialogIcon(int resId) {
        mDialogIcon.setImageResource(resId);
    }

    public void setDialogTitle(String title) {
        mDialogTitle.setText(title);
    }

    public void setIsLoading(boolean isLoading) {
        mIsLoading = isLoading;
    }

    public void setDuration(long duration) {
        mShowDuration = duration;
    }
}
