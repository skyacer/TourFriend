package com.elong.tourpal.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * RelativeLayoutExts
 *
 * 扩展的RelativeLayout，每行N个，超过N个自动换行
 */
public class RelativeGridLayout extends RelativeLayout {
    private static final String TAG = RelativeGridLayout.class.getSimpleName();

    /**
     * 每行默认的view个数
     */
    private static final int DEFAULT_NUM_OF_VIEWS_PER_LINE = 3;

    private int mMeasureWidth = -1;

    private ArrayList<View> mAddedChildViews = new ArrayList<>();

    private ArrayList<View> mPendingChildViews = new ArrayList<>();

    public RelativeGridLayout(Context context) {
        super(context);
    }

    public RelativeGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RelativeGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasureWidth = getMeasuredWidth();
        //把尚未添加的view添加进去
        if (mPendingChildViews.size() > 0) {
            if (mMeasureWidth > 0) {
                Iterator<View> it = mPendingChildViews.iterator();
                while (it.hasNext()) {
                    addChildView(it.next());
                    it.remove();
                }
            }
        }
    }

    public void addChildView(View view) {
        if (mMeasureWidth > 0) {
            int childrenCount = mAddedChildViews.size();
            int columnIdx = childrenCount % DEFAULT_NUM_OF_VIEWS_PER_LINE;//列号
            int rowIdx = childrenCount / DEFAULT_NUM_OF_VIEWS_PER_LINE;//行号
            int dividerMargin = getResources().getDimensionPixelSize(R.dimen.relative_grid_divider_margin);
            int viewWidth = (mMeasureWidth - dividerMargin * (DEFAULT_NUM_OF_VIEWS_PER_LINE - 1)) / DEFAULT_NUM_OF_VIEWS_PER_LINE;
            int viewHeight = viewWidth;

            RelativeLayout rlLine = null;
            if (columnIdx == 0) {
                //新起一行，增加一个RelativeLayout
                rlLine = new RelativeLayout(getContext());
                rlLine.setId(rowIdx);
                LayoutParams rlParams = new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, viewHeight);
                if (rowIdx == 0) {
                    rlParams.addRule(ALIGN_PARENT_TOP, TRUE);
                } else {
                    rlParams.addRule(BELOW, rowIdx - 1);
                }
                addView(rlLine, rlParams);
            } else {
                try {
                    rlLine = (RelativeLayout) getChildAt(rowIdx);
                } catch (Exception e) {
                    if (Env.DEBUG) {
                        Log.e(TAG, "e:", e);
                    }
                }
            }

            if (rlLine != null) {
                LayoutParams vParams = new LayoutParams(viewHeight, viewHeight);
                vParams.setMargins(columnIdx * (viewWidth + dividerMargin), 0, 0, 0);
//                if (columnIdx > 0 && columnIdx < DEFAULT_NUM_OF_VIEWS_PER_LINE){
//                    view.setPadding(padding, 0, padding, 0);
//                } else if (columnIdx == 0) {
//                    view.setPadding(0, 0, padding, 0);
//                } else if (columnIdx == DEFAULT_NUM_OF_VIEWS_PER_LINE){
//                    view.setPadding(padding, 0, 0, 0);
//                }
                rlLine.addView(view, vParams);
                mAddedChildViews.add(view);
            }
        } else if (mMeasureWidth < 0) {
            //还没onMeasure，因此先加入队列，等Measure完了之后再post一个任务来addView
            mPendingChildViews.add(view);
        }
    }

    public void resetChildViews(int startIdx) {
        int size = mAddedChildViews.size();
        if (startIdx >= 0 && startIdx < size) {
            for (int i = startIdx; i < size; i++) {
                View v = mAddedChildViews.get(i);
                if (v != null) {
                    v.setVisibility(GONE);
                }
            }
        }
    }

    public void removeChildView(int idx) {
        int columnIdx = idx % DEFAULT_NUM_OF_VIEWS_PER_LINE;//列号
        int rowIdx = idx / DEFAULT_NUM_OF_VIEWS_PER_LINE;//行号
        RelativeLayout rlLine = null;
        try {
            rlLine = (RelativeLayout) getChildAt(rowIdx);
        } catch (Exception e) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }
        if (rlLine != null) {
            rlLine.removeViewAt(columnIdx);
        }
    }

    public View getChildView(int idx) {
        if (mAddedChildViews != null) {
            if (idx <= mAddedChildViews.size() - 1) {
                return mAddedChildViews.get(idx);
            }
        }
        return null;
    }


}
