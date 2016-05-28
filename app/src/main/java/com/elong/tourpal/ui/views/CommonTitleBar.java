
package com.elong.tourpal.ui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elong.tourpal.R;

public class CommonTitleBar extends LinearLayout {

    private ImageView mImgBack;

    private TextView mTvTitle;

    private TextView mTvSetting;

    private ImageView mImgSetting;

//    private ImageView mRedPoint;

    private View mRoot;

//    private View mShadow;

//    private String mTitleText;

    private enum SETTING_TYPE {
        /**
         * 有文字的设置按钮
         */
        SETTING_TYPE_TEXT,
        /**
         * 图标设置按钮
         */
        SETTING_TYPE_IMG
    }

    private SETTING_TYPE mSettingType = SETTING_TYPE.SETTING_TYPE_TEXT;

    public CommonTitleBar(Context context) {
        super(context);
        init();
    }

    private void init() {
        final Context context = getContext();
        inflate(context, R.layout.common_title_bar, this);
        mImgBack = (ImageView) findViewById(R.id.common_img_back);
        mTvTitle = (TextView) findViewById(R.id.common_tv_title);
        mTvSetting = (TextView) findViewById(R.id.common_tv_setting);
        mImgSetting = (ImageView) findViewById(R.id.common_img_setting);
//        mRedPoint = (ImageView) findViewById(R.id.common_red_point);
        mRoot = findViewById(R.id.common_titlebar_root);
//        mShadow = findViewById(R.id.common_title_bar_shadow);
        if (isInEditMode()) {
            if (mRoot != null) {
                mRoot.setBackgroundColor(0xe54f4a);
            }
        } else {
            mRoot.setBackgroundResource(R.drawable.default_title_bar_bg);
        }

//        if (!TextUtils.isEmpty(mTitleText)) {
//            setTitle(mTitleText);
//        }
        if (context instanceof Activity) {
            setOnBackListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    ((Activity) context).finish();
                }
            });
        }
    }

    public TextView getTitleView() {
        return mTvTitle;
    }

    /**
     * 设置背景为透明
     */
    public void setBackgroundTransparent() {
        setBackgroundTransparent(false);
    }

    public void setBackgroundTransparent(boolean showShadow) {
        mRoot.setBackgroundColor(0);

//        if (showShadow) {
//            mShadow.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public void setBackgroundColor(int color) {
        mRoot.setBackgroundColor(color);
//        mShadow.setVisibility(View.GONE);
    }

    @Override
    public void setBackgroundResource(int resid) {
        mRoot.setBackgroundResource(resid);
    }

    public CommonTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
//        mTitleText = getTextFromAttrs(context, attrs, "text");
        init();
    }

//    public void setRedPointVisibility(int visibility) {
//        mRedPoint.setVisibility(visibility);
//    }

    private void setSettingType(SETTING_TYPE type) {
        mSettingType = type;
    }

//    public void setRedPointResource(int resId) {
//        mRedPoint.setImageResource(resId);
//    }

//    public void setRedPointDrawable(Drawable drawable) {
//        mRedPoint.setImageDrawable(drawable);
//    }

    public void setMiddleView(View v) {
        setView(R.id.common_ll_middle, v);
    }

    public void setMiddleView(int layoutResId) {
        setView(R.id.common_ll_middle, layoutResId);
    }

    public void setLeftView(View v) {
        setView(R.id.common_ll_left, v);
    }

    public void setLeftView(int layoutResId) {
        setView(R.id.common_ll_left, layoutResId);
    }

    public void setRightView(View v) {
        setView(R.id.common_ll_right, v);
    }

    public void setRightView(int layoutResId) {
        setView(R.id.common_ll_right, layoutResId);
    }

    public void setContentView(int layoutResId) {
        setView(R.id.common_titlebar_root, layoutResId);
    }

    private void setView(int rootId, View v) {
        ViewGroup ll = (ViewGroup) findViewById(rootId);
        ll.removeAllViews();
        ll.addView(v);
    }

    private void setView(int rootId, int layoutId) {
        ViewGroup ll = (ViewGroup) findViewById(rootId);
        ll.removeAllViews();
        inflate(getContext(), layoutId, ll);
    }

    private ImageView getBackImageView() {
        return mImgBack;
    }

    public void setBackImage(int imgRes) {
        mImgBack.setImageResource(imgRes);
    }

    public View getRightButton() {
        switch (mSettingType) {
            case SETTING_TYPE_TEXT:
                return mTvSetting;
            case SETTING_TYPE_IMG:
                return mImgSetting;
        }
        return null;
    }

    public void setTitle(CharSequence title) {
        if (mTvTitle != null) {
            mTvTitle.setText(title);
        }
    }

    public void setTitle(int title) {
        mTvTitle.setText(title);
    }

    public void setSettingVisible(boolean visible) {
        mImgSetting.setVisibility(View.GONE);
        mTvSetting.setVisibility(View.GONE);
        if (visible) {
            switch (mSettingType) {
                case SETTING_TYPE_TEXT:
                    mTvSetting.setVisibility(View.VISIBLE);
                    break;
                case SETTING_TYPE_IMG:
                    mImgSetting.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    public void setBackVisible(boolean visible) {
        mImgBack.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setSettingTxt(int resId) {
        setSettingType(SETTING_TYPE.SETTING_TYPE_TEXT);
        setSettingVisible(true);
        mTvSetting.setText(resId);
    }

    public void setSettingTxt(CharSequence title) {
        setSettingType(SETTING_TYPE.SETTING_TYPE_TEXT);
        setSettingVisible(true);
        mTvSetting.setText(title);
    }

    public void setSettingImg(int resId) {
        setSettingType(SETTING_TYPE.SETTING_TYPE_IMG);
        setSettingVisible(true);
        mImgSetting.setImageResource(resId);
    }

    public void setSettingImg(Drawable drawable) {
        setSettingType(SETTING_TYPE.SETTING_TYPE_IMG);
        setSettingVisible(true);
        mImgSetting.setImageDrawable(drawable);
    }

    public void setOnBackListener(OnClickListener l) {
        mImgBack.setOnClickListener(l);
    }

    public void setOnSettingListener(OnClickListener l) {
        switch (mSettingType) {
            case SETTING_TYPE_TEXT:
                mTvSetting.setOnClickListener(l);
                break;
            case SETTING_TYPE_IMG:
                mImgSetting.setOnClickListener(l);
                break;
        }
    }

    public void setOnButtonListener(OnClickListener l) {
        setOnBackListener(l);
        setOnSettingListener(l);
    }

    public int getLeftButtonId() {
        return getBackImageView().getId();
    }

    public int getRightButtonId() {
        View v = getRightButton();
        return v != null ? v.getId() : 0;
    }

    public void setRightTVBG(int resId) {
        mTvSetting.setBackgroundResource(resId);
    }
}
