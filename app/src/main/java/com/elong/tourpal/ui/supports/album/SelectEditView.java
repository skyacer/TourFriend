package com.elong.tourpal.ui.supports.album;


import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.TourPalApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhitao.xu on 2015/4/7.
 */
public class SelectEditView extends ScrollView {
    private static final int DESTINATION_MARGIN = 20;
    private static final int SEARCH_EDIT_WIDTH = TourPalApplication.getAppContext().getResources().getDimensionPixelOffset(R.dimen.select_edit_search_width);

    private Context mContext;
    private LayoutInflater mInflater;

    private LinearLayout mMainLayout;                    //ScrollView下的主LinearLayout
    private LinearLayout mLastLinearLayout;          //记录最后一个LinearLayout
    private List<LinearLayout> mSubLayoutList = new ArrayList<LinearLayout>();  //LinearLayout数组(mainLayout除外)

    private View mSearchView;
    private EditText mSearchEV;        //自动匹配输入框

    private int mLineLayoutIndex = -1;                   //主LinaerLayout下的子LinearLayout总数:也就是总行数
    private int mLineWidth;

    private int mItemLayoutResourceId;               //每行的LinearLayout的布局文件id
    private int mCurrLineWidth = 0;
//    private HashMap<Integer, Integer> mItemViewPosition = new HashMap<Integer, Integer>();
    private ArrayList<View> mItems = new ArrayList<View>();
    private CallBack mCallBack;
//    private ArrayList<String> mDestinations;
//    public HashMap<Integer, CityDataManager.CityData> mSelectDestinations = new HashMap<Integer, CityDataManager.CityData>();

    public SelectEditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SelectEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectEditView(Context context) {
        super(context);
        init(context);
    }

    /**
     * 初始化ScrollView下的主LinearLayout,第一个LinearLayout,和AutoCompleteTextView
     *
     * @param context
     */
    private void init(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mMainLayout = new LinearLayout(mContext);
        mMainLayout.setOrientation(LinearLayout.VERTICAL);
        mMainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT
        ));

        this.addView(mMainLayout);
        //创建第一个horizontal 的 LinaerLayout
        LinearLayout firstLayout = newSubLinearLayout();
        mSubLayoutList.add(firstLayout);
        mLastLinearLayout = firstLayout;

        //将AutoCompleteTextView加入到第一个LinearLayout
        mSearchView = mInflater.inflate(R.layout.select_edit_view_search_item, null);

        mSearchEV = (EditText) mSearchView.findViewById(R.id.select_edit_view_search);
        firstLayout.addView(mSearchView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        //将创建的第一个linearLayout 加入到mainLayout中
        mMainLayout.addView(firstLayout);

//        for (CityDataManager.CityData c : mSelectDestinations.values()) {
//            addItemView(c);
//        }
    }

    /**
     * 新建一行:新建一个LinearLayout
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

    public void setHeight(int height) {

    }

    /**
     * 设置Item布局资源id
     *
     * @param resourceId
     */
    public void setItemLayout(int resourceId) {
        this.mItemLayoutResourceId = resourceId;
    }

    /**
     * 重新添加所有的Item
     *
     * @param itemList
     */
    private void reAddAllItem(List<View> itemList) {
        mLastLinearLayout.removeView(mSearchView);
        LinearLayout firstLayout = newSubLinearLayout();
        mSubLayoutList.add(firstLayout);
        mLastLinearLayout = firstLayout;
        mMainLayout.addView(firstLayout);
        mCurrLineWidth = 0;
        int length = itemList.size();
        for (int i = 0; i < length; i++) {
            View item = itemList.get(i);
            int childCount = mLastLinearLayout.getChildCount();
            int itemWidth = item.getWidth();
            if (childCount == 0) {
                mLastLinearLayout.addView(item, 0);
                item.setTag(mLastLinearLayout);
                mCurrLineWidth += itemWidth + DESTINATION_MARGIN;
            } else {
                int maxWidthRight = itemWidth + DESTINATION_MARGIN;
                boolean isNewLine = judgeNewLine(maxWidthRight);
                mLastLinearLayout.addView(item);
                item.setTag(mLastLinearLayout);
                mCurrLineWidth += itemWidth + DESTINATION_MARGIN;
            }
        }

        judgeNewLine(SEARCH_EDIT_WIDTH);
        mLastLinearLayout.addView(mSearchView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private boolean judgeNewLine(int maxLeft) {
        if (mLineWidth - mCurrLineWidth < maxLeft) {
            //将autoCompleteTextView从lastLinearLayout中remove掉
            mLastLinearLayout.removeView(mSearchView);
            //新建一个LinearLayout,将autoCompleteTextView添加到此LinaerLayout
            mLastLinearLayout = newSubLinearLayout();
            mMainLayout.addView(mLastLinearLayout);

            mSubLayoutList.add(mLastLinearLayout);
            mCurrLineWidth = 0;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 添加一个Item:自动去判断是在当前lastLinearLayout中添加，还是要再new 一个LinearLayout
     *
     * @param value
     */
    int currLayoutIndex = 0;

    private void addItemView(String des) {
        mLastLinearLayout.removeView(mSearchView);
        if (mLineWidth <= 0) {
            mLineWidth = mLastLinearLayout.getWidth();
            if (mLineWidth > 0) {
                mCallBack.setViewWidth(mLineWidth);
            }
        }
        final LinearLayout item = (LinearLayout) mInflater.inflate(mItemLayoutResourceId, null);

        final ImageView deleteIv = (ImageView) item.findViewById(R.id.select_edit_view_select_delete);
        deleteIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.removeItem(item, mItems.indexOf(item));
                mItems.remove(item);
                LinearLayout layout = (LinearLayout) item.getTag();
                layout.removeView(item);
                mLastLinearLayout.removeView(mSearchView);
                List<View> itemList = new ArrayList<View>();
                int mainLayoutChildCount = mMainLayout.getChildCount();
                for (int i = 0; i < mainLayoutChildCount; i++) {
                    LinearLayout linearLayout = (LinearLayout) mMainLayout.getChildAt(i);
                    int count = linearLayout.getChildCount();
                    for (int j = 0; j < count; j++) {
                        View itemLayout = (View) linearLayout.getChildAt(j);
                        if (!(itemLayout instanceof AutoCompleteTextView)) {
                            itemList.add(itemLayout);
                        }
                    }
                    linearLayout.removeAllViews();
                }
                mMainLayout.removeAllViews();
                mSubLayoutList.clear();
                reAddAllItem(itemList);
                setEditTextFocus();
            }
        });

        TextView itemTv = (TextView) item.findViewById(R.id.select_edit_view_select_data);
        ImageView itemIv = (ImageView) item.findViewById(R.id.select_edit_view_select_delete);
        itemIv.measure(MeasureSpec.EXACTLY, MeasureSpec.EXACTLY);
        int itemIvWidth = getResources().getDimensionPixelSize(R.dimen.select_edit_item_delete_width);
        int itemWidth = (int) AlbumUtils.getFontTextWidth(des, (int) itemTv.getTextSize()) + 5 + itemIvWidth;// + //itemIv.getMeasuredWidth();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        item.setLayoutParams(params);
        itemTv.setText(des);
        judgeNewLine(itemWidth + DESTINATION_MARGIN);
        mLastLinearLayout.addView(item);
        mItems.add(item);
        mCurrLineWidth += itemWidth + DESTINATION_MARGIN;
        mLastLinearLayout.setTag(mLineLayoutIndex);
        item.setTag(mLastLinearLayout);
        judgeNewLine(SEARCH_EDIT_WIDTH);
        mLastLinearLayout.addView(mSearchView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        setEditTextFocus();
    }

    private void setEditTextFocus() {
        mSearchEV.setFocusable(true);
        mSearchEV.setFocusableInTouchMode(true);
        mSearchEV.requestFocus();
        mSearchEV.findFocus();
    }

//    public ArrayList<String> getSelectDatas() {
////        ArrayList<String> datas = new ArrayList<String>();
////        List<View> itemList = new ArrayList<View>();
////        int mainLayoutChildCount = mMainLayout.getChildCount();
////
////        for (int i = 0; i < mainLayoutChildCount; i++) {
////            LinearLayout linearLayout = (LinearLayout) mMainLayout.getChildAt(i);
////            int count = linearLayout.getChildCount();
////            for (int j = 0; j < count; j++) {
////                View itemLayout = (View) linearLayout.getChildAt(j);
////                if (!(itemLayout instanceof AutoCompleteTextView)) {
////                    itemList.add(itemLayout);
////                }
////            }
////        }
////
////        for (View v : itemList) {
////            if (!v.equals(mSearchView)) {
////                TextView t = (TextView) v.findViewById(R.id.select_edit_view_select_data);
////                String data = t.getText().toString();
////                datas.add(data);
////            }
////        }
////        return datas;
//        return new ArrayList<String>(mSelectDestinations.values());
//    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        mSearchEV.addTextChangedListener(textWatcher);
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mSearchEV.setOnFocusChangeListener(l);
    }

    public void addDestination(String destination) {
        String des = destination;
        addItemView(destination);
    }

    public void clearInput() {
        mSearchEV.setText("");
    }

    public interface CallBack {
        public void removeItem(View item, int position);
        public void setViewWidth(int width);
    }

    public void setmCallBack(CallBack callBack) {
        mCallBack = callBack;
    }

    public void initItemViews(ArrayList<String> destinations, int width) {
        if (destinations == null) {
            return;
        }
        mLineWidth = width;
        for (String d : destinations) {
            addItemView(d);
        }
    }
}
