package com.elong.tourpal.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.ui.supports.album.AlbumUtils;
import com.elong.tourpal.ui.supports.album.SelectEditView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 显示标签的容器
 */
public class TagsContainer extends ScrollView {

    private int mItemMargin = 0;

    private Context mContext;
    private LayoutInflater mInflater;

    private LinearLayout mMainLayout;
    private LinearLayout mLastLinearLayout;
    private List<LinearLayout> mSubLayoutList = new ArrayList<>();

    private int mLineLayoutIndex = -1;
    private int mLineWidth;

    private int mCurrLineWidth = 0;

    private ArrayList<View> mItems = new ArrayList<>();
    private ArrayList<String> mPendingItems = new ArrayList<>();
    private SelectEditView.CallBack mCallBack;

    public TagsContainer(Context context) {
        super(context);
        init(context);
    }

    public TagsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TagsContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public TagsContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mItemMargin = getResources().getDimensionPixelSize(R.dimen.tag_margin);

        mMainLayout = new LinearLayout(mContext);
        mMainLayout.setOrientation(LinearLayout.VERTICAL);
        mMainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT
        ));

        this.addView(mMainLayout);
        //增加第一行
        LinearLayout firstLayout = newSubLinearLayout();
        mSubLayoutList.add(firstLayout);
        mLastLinearLayout = firstLayout;
        mMainLayout.addView(firstLayout);
    }

    /**
     * 创建一行
     *
     * @return
     */
    private LinearLayout newSubLinearLayout() {
        LinearLayout layout = new LinearLayout(mContext);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        lp1.gravity = Gravity.CENTER_VERTICAL;
        layout.setLayoutParams(lp1);
        mLineLayoutIndex++;
        return layout;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLineWidth = getMeasuredWidth();
        if (mPendingItems.size() > 0){
            Iterator<String> it = mPendingItems.iterator();
            while (it.hasNext()){
                String item = it.next();
                if (!TextUtils.isEmpty(item)){
                    addItemView(item);
                }
                it.remove();
            }
        }
    }

    public void addItemView(String tag) {
        if (mLineWidth <= 0){
            mPendingItems.add(tag);
            return;
        }
        TextView item = getTagView(tag);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setBackgroundResource(R.drawable.tv_bg_tag);
        item.setTextColor(getResources().getColor(R.color.tag_text));
        int textSize = getContext().getResources().getDimensionPixelSize(R.dimen.tag_text_size);
        int itemWidth = AlbumUtils.getFontTextWidth(tag, textSize) + item.getPaddingLeft() + item.getPaddingRight();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //先judge，若要换行，mLineLayoutIndex会+1
        judgeNewLine(itemWidth + mItemMargin);
        if (mLineLayoutIndex == 0) {
            params.setMargins(mItemMargin / 2, 0, mItemMargin / 2, 0);
        } else {
            params.setMargins(mItemMargin / 2, mItemMargin / 4, mItemMargin / 2, 0);
        }
        item.setLayoutParams(params);
        mLastLinearLayout.addView(item);
        mItems.add(item);
        mCurrLineWidth += itemWidth + mItemMargin;
        mLastLinearLayout.setTag(mLineLayoutIndex);
        item.setTag(mLastLinearLayout);
    }

    private TextView getTagView(String tag){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        TextView tagView = (TextView) inflater.inflate(R.layout.tag_view, null);
        tagView.setText(tag);
        return tagView;
    }

    private boolean judgeNewLine(int maxLeft) {
        if (mLineWidth - mCurrLineWidth < maxLeft) {
            mLastLinearLayout = newSubLinearLayout();
            mMainLayout.addView(mLastLinearLayout);

            mSubLayoutList.add(mLastLinearLayout);
            mCurrLineWidth = 0;
            return true;
        } else {
            return false;
        }
    }

    public void removeAllTags() {
        mMainLayout.removeAllViews();
        //重置属性
        mLineLayoutIndex = -1;
        mCurrLineWidth = 0;
        mItems.clear();
        mPendingItems.clear();
        mSubLayoutList.clear();
        //增加第一行
        LinearLayout firstLayout = newSubLinearLayout();
        mSubLayoutList.add(firstLayout);
        mLastLinearLayout = firstLayout;
        mMainLayout.addView(firstLayout);
    }
}
