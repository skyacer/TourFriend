package com.elong.tourpal.ui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elong.tourpal.R;

/**
 * Activity基类，包含各个页面共通的特性
 * <p/>
 * 注：继承这个类的Activity的布局根view须是一个viewGroup，否则会crash
 */
public class ActivityBase extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        //初始化title
        initTitle();
    }

    /**
     * 初始化title，并且为title再抽出一层布局
     */
    private void initTitle() {
        ViewGroup contentRoot = (ViewGroup) findViewById(android.R.id.content);
        ViewGroup root = (ViewGroup) contentRoot.getChildAt(0);
        contentRoot.removeView(root);
        RelativeLayout newRoot = new RelativeLayout(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View titleRoot = inflater.inflate(R.layout.title_common, newRoot, false);

        if (root instanceof RelativeLayout) {
            newRoot.addView(titleRoot, 0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.BELOW, titleRoot.getId());
            newRoot.addView(root, 1, params);
            super.setContentView(newRoot);
            newRoot.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            newRoot.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            //TODO 当继承ActivityBase的Activity的layout的root_view是别的layout时，在此增加case
        }

        initTitleViews();
    }

    /**
     * *********************************** TitleBar相关接口 **************************************
     */
    protected RelativeLayout mTitleBarRoot;
    private LinearLayout mTitleBarLeftContainer;
    private LinearLayout mTitleBarCenterContainer;
    protected LinearLayout mTitleBarRightContainer;
    private ImageView mIvTitleLeft;
    private TextView mTvTitleLeft;
    private ImageView mIvTitleRight;
    private TextView mTvTitleRight;
    private TextView mTvTitle;

    private void initTitleViews() {
        mTitleBarRoot = (RelativeLayout) findViewById(R.id.title_root);
        mTitleBarLeftContainer = (LinearLayout) findViewById(R.id.titlebar_left_container);
        mTitleBarCenterContainer = (LinearLayout) findViewById(R.id.titlebar_center_container);
        mTitleBarRightContainer = (LinearLayout) findViewById(R.id.titlebar_right_container);

        mIvTitleLeft = (ImageView) findViewById(R.id.title_iv_left);
        mTvTitleLeft = (TextView) findViewById(R.id.title_tv_left);
        mIvTitleRight = (ImageView) findViewById(R.id.title_iv_right);
        mTvTitleRight = (TextView) findViewById(R.id.title_tv_right);
        mTvTitle = (TextView) findViewById(R.id.title_tv_text);
    }

    /**
     * 设置title左边按钮是否可见
     *
     * @param enable
     */
    public void setTitleLeftBtnEnable(boolean enable) {
        if (enable) {
            mTitleBarLeftContainer.setVisibility(View.VISIBLE);
        } else {
            mTitleBarLeftContainer.setVisibility(View.INVISIBLE);
        }
    }

    public void setTitleBarVisiable(boolean visiable) {
        if (!visiable) {
            mTitleBarRoot.setVisibility(View.GONE);
        } else {
            mTitleBarRoot.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置title左边按钮的图片资源和点击事件
     *
     * @param rid
     * @param listener
     */
    public void setTitleLeftBtn(int rid, View.OnClickListener listener) {
        mIvTitleLeft.setImageResource(rid);
        mIvTitleLeft.setOnClickListener(listener);
    }

    /**
     * 设置title左边按钮的图片资源
     *
     * @param rid
     */
    public void setTitleLeftBtn(int rid) {
        mIvTitleLeft.setImageResource(rid);
    }

    /**
     * 设置title左边按钮的点击事件
     *
     * @param listener
     */
    public void setTitleLeftBtn(View.OnClickListener listener) {
        mIvTitleLeft.setOnClickListener(listener);
    }

    /**
     * 设置title右边按钮是否可见
     *
     * @param enable
     */
    public void setTitleRightBtnEnable(boolean enable) {
        if (enable) {
            mTitleBarRightContainer.setVisibility(View.VISIBLE);
        } else {
            mTitleBarRightContainer.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置title左侧的文字
     *
     * @param text 左侧文字
     */
    public void setTitleLeftText(String text) {
        mTvTitleLeft.setText(text);
    }

    public TextView getTitleRightTextView() {
        return mTvTitleRight;
    }

    /**
     * 设置title右边按钮的图片资源和点击事件
     *
     * @param rid
     * @param listener
     */
    public void setTitleRightBtn(int rid, View.OnClickListener listener) {
        mIvTitleRight.setImageResource(rid);
        mIvTitleRight.setOnClickListener(listener);
    }

    /**
     * 设置title右边按钮的图片资源
     *
     * @param rid
     */
    public void setTitleRightBtn(int rid) {
        if (rid >= 0) {
            mIvTitleRight.setImageResource(rid);
            mIvTitleRight.setVisibility(View.VISIBLE);
        } else {
            mIvTitleRight.setVisibility(View.GONE);
        }
    }

    /**
     * 设置title右边按钮的图片资源和点击事件
     *
     * @param listener
     */
    public void setTitleRightBtn(View.OnClickListener listener) {
        mIvTitleRight.setOnClickListener(listener);
    }

    public void setTitleText(String text) {
        if (text != null) {
            mTvTitle.setText(text);
        }
    }

    public void setTitleText(int stringId) {
        if (stringId >= 0) {
            mTvTitle.setText(stringId);
        }
    }

    public void setTitleTextGravity(int gravity) {
        mTvTitle.setGravity(gravity);
    }

    public void setTitleText(String text, int gravity) {
        setTitleText(text);
        setTitleTextGravity(gravity);
    }

    /**
     * 设置titlebar背景
     *
     * @param resId
     */
    public void setTitleBarBG(int resId) {
        mTitleBarRoot.setBackgroundResource(resId);
    }

    /**
     * 自定义titlebar左测
     *
     * @param v
     */
    public void setTitleLeftView(View v) {
        setView(mTitleBarLeftContainer, v);
    }

    /**
     * 自定义titlebar中间
     *
     * @param v
     */
    public void setTitleCenterView(View v) {
        setView(mTitleBarCenterContainer, v);
    }

    /**
     * 自定义titlebar右侧
     *
     * @param v
     */
    public void setTitleRightView(View v) {
        setView(mTitleBarRightContainer, v);
    }

    private void setView(ViewGroup parent, View v) {
        parent.removeAllViews();
        parent.addView(v);
    }

    /**
     * 为title左侧增加一个view
     *
     * @param v
     */
    public void addTitleLeftView(View v) {
        mTitleBarLeftContainer.addView(v);
    }
/************************************** TitleBar相关接口 END ***************************************/

}
