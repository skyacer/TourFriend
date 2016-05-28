package com.elong.tourpal.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;

/**
 * PersonalCenterItem
 * <p/>
 * 个人中心的item
 */
public class CommonSettingItem extends RelativeLayout {

    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_CHECKABLE = 2;

    private ImageView mIvIconLeft;
    private ImageView mIvIconRight;
    private CustomImageView mCivRight;
    private CheckBox mCbSwitchRight;
    private TextView mTvContent;
    private TextView mTvRight;
    private View mDividerFull;
    private View mDividerPart;
    private View mDividerTop;
    private RelativeLayout mRlFirstLine;
    private TextView mTvSecondLine;

    private int mType;

    public CommonSettingItem(Context context) {
        super(context);
        initView();
    }

    public CommonSettingItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CommonSettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CommonSettingItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.common_setting_item, this);
        mIvIconLeft = (ImageView) findViewById(R.id.csi_iv_icon_left);
        mIvIconRight = (ImageView) findViewById(R.id.csi_iv_icon_right);
        mCivRight = (CustomImageView) findViewById(R.id.csi_civ_right);
        mTvContent = (TextView) findViewById(R.id.csi_tv_content);
        mDividerFull = findViewById(R.id.csi_divider_full);
        mDividerPart = findViewById(R.id.csi_divider_part);
        mDividerTop = findViewById(R.id.csi_divider_top);
        mCbSwitchRight = (CheckBox) findViewById(R.id.csi_rbtn_switch_right);
        mTvRight = (TextView) findViewById(R.id.csi_tv_right);
        mRlFirstLine= (RelativeLayout) findViewById(R.id.csi_rl_first_line);
        mTvSecondLine = (TextView) findViewById(R.id.csi_tv_second_line);
        setFullBottomDivider(false);
        setDividerTopEnable(false);
    }

    /**
     * 下方分割线是否是full
     *
     * @param isFull 分割线是否需要满屏
     */
    public void setFullBottomDivider(boolean isFull) {
        if (isFull) {
            mDividerFull.setVisibility(VISIBLE);
            mDividerPart.setVisibility(GONE);
        } else {
            mDividerFull.setVisibility(GONE);
            mDividerPart.setVisibility(VISIBLE);
        }
    }

    public void setDividerTopEnable(boolean enable) {
        mDividerTop.setVisibility(enable ? VISIBLE : GONE);
    }

    public void setIconLeft(int rid) {
        mIvIconLeft.setImageResource(rid);
    }

    public void setIconRight(int rid) {
        mIvIconRight.setImageResource(rid);
    }

    public void setContentText(int rid) {
        mTvContent.setText(rid);
    }

    public void setIconRightVisibility(int visibility) {
        mIvIconRight.setVisibility(visibility);
    }

    public void setNoLeftIconMode(boolean fullBottomDivider) {
        mIvIconLeft.setVisibility(GONE);
        if (!fullBottomDivider) {
            setFullBottomDivider(false);
            RelativeLayout.LayoutParams lpBottomDivider = (LayoutParams) mDividerPart.getLayoutParams();
            int marginLeft = getResources().getDimensionPixelSize(R.dimen.csi_divider_part_margin_left_no_left_icon);
            lpBottomDivider.setMargins(marginLeft, 0, 0, 0);
        } else {
            setFullBottomDivider(true);
        }
        int contentMarginLeft = getResources().getDimensionPixelOffset(R.dimen.csi_content_margin_left_no_left_icon);
        RelativeLayout.LayoutParams lpContent = (LayoutParams) mTvContent.getLayoutParams();
        lpContent.setMargins(contentMarginLeft, 0, 0, 0);
    }

    public boolean isSwitchOn() {
        return mCbSwitchRight.isChecked();
    }

    public void setSwitchOn(boolean isSwitchOn) {
        mCbSwitchRight.setChecked(isSwitchOn);
    }

    public void setOnCheckedChangeListener(final OnItemCheckChangedListener listener) {
        mCbSwitchRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listener.onItemCheckChanged(getId(), buttonView, isChecked);
            }
        });
    }

    /**
     * 设置type：TYPE_NORMAL，TYPE_CHECKABLE
     *
     * @param type item类型
     */
    public void setType(int type) {
        mType = type;
        switch (type) {
            case TYPE_NORMAL:
                //do nothing
                break;
            case TYPE_CHECKABLE:
                mIvIconRight.setVisibility(GONE);
                mCbSwitchRight.setVisibility(VISIBLE);
                setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCbSwitchRight.performClick();
                    }
                });
                break;
        }
    }

    /**
     * 设置item右边的text
     *
     * @param text text,若text为空，则显示“未填写”
     */
    public void setRightText(String text) {
        if (!TextUtils.isEmpty(text)) {
            mTvRight.setText(text);
        } else {
            mTvRight.setText(R.string.mi_empty_contact_info);
        }
    }

    /**
     * 获取item右边的text
     * @return item右边的text
     */
    public String getRightText() {
        return mTvRight.getText().toString();
    }

    /**
     * 设置item的高度
     * @param height 高度 px
     */
    public void setHeight(int height) {
        this.getLayoutParams();
    }

    public void setCivRightImage(String urlOrPath, boolean isUrl) {
        RelativeLayout.LayoutParams params = (LayoutParams) mRlFirstLine.getLayoutParams();
        int height = getResources().getDimensionPixelSize(R.dimen.csi_avatar_height);
        if (params != null) {
            params.height = height;
        }
        mCivRight.setVisibility(VISIBLE);
        if (!TextUtils.isEmpty(urlOrPath)) {
            if (isUrl) {
                mCivRight.setImageUrl(urlOrPath);
            } else {
                mCivRight.setImagePath(urlOrPath);
            }
            mCivRight.loadImage();
        }
    }

    public void setCivRightImage(int rid) {
        RelativeLayout.LayoutParams params = (LayoutParams) mRlFirstLine.getLayoutParams();
        int height = getResources().getDimensionPixelSize(R.dimen.csi_avatar_height);
        if (params != null) {
            params.height = height;
        }
        mCivRight.setVisibility(VISIBLE);
        mCivRight.setImageResource(rid);
    }

    public void setSecondLineText(int rid) {
        mTvSecondLine.setVisibility(VISIBLE);
        mTvSecondLine.setText(rid);
    }

    public interface OnItemCheckChangedListener{
        void onItemCheckChanged(int itemId, CompoundButton buttonView, boolean isChecked);
    }

}
