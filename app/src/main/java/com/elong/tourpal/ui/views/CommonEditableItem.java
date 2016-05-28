package com.elong.tourpal.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elong.tourpal.R;

/**
 * CommonEditableItem
 */
public class CommonEditableItem extends RelativeLayout {

    private TextView mTvLeft;
    private EditText mEtRight;
    private View mDividerTop;
    private View mDividerBottomFull;
    private View mDividerBottomPart;

    public CommonEditableItem(Context context) {
        super(context);
        initView();
    }

    public CommonEditableItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CommonEditableItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CommonEditableItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.common_editable_item, this);
        mTvLeft = (TextView) findViewById(R.id.cei_tv_left);
        mEtRight = (EditText) findViewById(R.id.cei_et_right);
        mDividerTop = findViewById(R.id.cei_divider_top);
        mDividerBottomFull = findViewById(R.id.cei_divider_bottom_full);
        mDividerBottomPart = findViewById(R.id.cei_divider_bottom_part);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEtRight.hasFocus()) {
                    mEtRight.requestFocusFromTouch();
                    mEtRight.setSelection(mEtRight.getText().length());
                }
                InputMethodManager inputManager = (InputMethodManager) mEtRight.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mEtRight, 0);
            }
        });
        setFullBottomDivider(false);
        setDividerTopEnable(false);
    }

    public void setDividerTopEnable(boolean enable) {
        mDividerTop.setVisibility(enable ? VISIBLE : GONE);
    }

    /**
     * 下方分割线是否是full
     *
     * @param isFull 分割线是否需要满屏
     */
    public void setFullBottomDivider(boolean isFull) {
        if (isFull) {
            mDividerBottomFull.setVisibility(VISIBLE);
            mDividerBottomPart.setVisibility(GONE);
        } else {
            mDividerBottomFull.setVisibility(GONE);
            mDividerBottomPart.setVisibility(VISIBLE);
        }
    }

    public void setTextLeft(String text) {
        mTvLeft.setText(text);
    }

    public void setTextLeft(int rid) {
        mTvLeft.setText(rid);
    }

    public void setEtRightHint(int rid) {
        mEtRight.setHint(rid);
    }

    public void setEtRightText(String text) {
        mEtRight.setText(text);
    }

    public String getEtRightText() {
        return mEtRight.getText().toString();
    }

    public void setEtRightFocusable(boolean focusable) {
        mEtRight.setFocusable(focusable);
    }

    public void setEtRightOnClickListener(OnClickListener onClickListener) {
        mEtRight.setOnClickListener(onClickListener);
    }

    /**
     * 设置输入类型
     *
     * @param type android.text.InputType.*
     */
    public void setEtRightInputType(int type) {
        mEtRight.setInputType(type);
    }

    /**
     * 设置inputFilter
     *
     * @param inputFilters InputFilter
     */
    public void setEtRightInputFilter(InputFilter[] inputFilters) {
        mEtRight.setFilters(inputFilters);
    }

}
