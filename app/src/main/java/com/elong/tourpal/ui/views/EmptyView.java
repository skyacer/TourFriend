package com.elong.tourpal.ui.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elong.tourpal.R;

public class EmptyView extends RelativeLayout {

    private ImageView mIvIcon;
    private TextView mTvText;

    public EmptyView(Context context) {
        super(context);
        initView();
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.empty_view, this);
        mIvIcon = (ImageView) findViewById(R.id.empty_view_icon);
        mTvText = (TextView) findViewById(R.id.empty_view_text);
    }

    public void setText(int rid) {
        mTvText.setText(rid);
    }

    public void setIcon(int rid) {
        mIvIcon.setImageResource(rid);
    }
}
