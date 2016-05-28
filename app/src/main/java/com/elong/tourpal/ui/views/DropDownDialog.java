package com.elong.tourpal.ui.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elong.tourpal.R;

import java.util.ArrayList;

/**
 * 下拉的弹窗
 */
public class DropDownDialog extends BasePopupWindow {

    private ListView mContentList;
    private ArrayList<ListItem> mContentData = new ArrayList<>();
    private DropDownListContentAdapter mContentAdapter;

    private DropDownDialogCallback mDialogCallback;

    public DropDownDialog(Context context) {
        super(context);
        setContentView(R.layout.drop_down_dialog);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRootView.setLayoutParams(params);
        initViews();
    }

    private void initViews() {
        mContentList = (ListView) mRootView.findViewById(R.id.drop_down_dlg_list);
        mContentAdapter = new DropDownListContentAdapter();
        mContentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mDialogCallback != null) {
                    ListItem item = mContentAdapter.getItem(position);
                    if (item != null) {
                        mDialogCallback.onItemClick(item.itemId, item.itemDesc);
                    }
                }
                dismiss();
            }
        });
        mContentList.setAdapter(mContentAdapter);
        mWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mDialogCallback != null) {
                    mDialogCallback.onDismiss();
                }
            }
        });
        mWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int x = (int) event.getX();
                final int y = (int) event.getY();

                if (mRootView != null) {
                    int width = mContentList.getWidth();
                    int height = mContentList.getHeight();
                    int fullWidth = mRootView.getWidth();
                    if (width > 0 && height > 0) {
                        if ((event.getAction() == MotionEvent.ACTION_DOWN)
                                && ((x < fullWidth - width) || (x >= fullWidth) || (y < 0) || (y >= height))) {
                            dismiss();
                            return true;
                        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                            dismiss();
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * 增加一个下拉菜单项
     * @param id id
     * @param desc 显示的描述
     * @param iconRid 图标
     */
    public void addItem(int id, String desc, int iconRid) {
        ListItem item = new ListItem(id, desc, iconRid);
        if (mContentData == null){
            mContentData = new ArrayList<>();
        }
        mContentData.add(item);
        mContentAdapter.notifyDataSetChanged();
    }

    /**
     * 增加一个下拉菜单项
     * @param id id
     * @param descRid 显示的描述
     * @param iconRid 图标
     */
    public void addItem(int id, int descRid, int iconRid) {
        String desc = mContext.getString(descRid);
        addItem(id, desc, iconRid);
    }

    /**
     * 去掉所有菜单项
     */
    public void removeAllItems() {
        mContentData.clear();
        mContentAdapter.notifyDataSetChanged();
    }

    public void show(View anchor) {
        if (!isShowing()) {
            preShow(true);
            mWindow.setAnimationStyle(0);
            mWindow.showAsDropDown(anchor, 0, -25);
        }
    }

    public boolean isShowing() {
        return mWindow.isShowing();
    }

    public void setCallback(DropDownDialogCallback callback) {
        mDialogCallback = callback;
    }

    @Override
    protected void preShow(boolean needFocus) {
        if (mRootView == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }
        if (mBackground == null) {
            mWindow.setBackgroundDrawable(new BitmapDrawable());
        } else {
            mWindow.setBackgroundDrawable(mBackground);
        }

        mWindow.setWidth(WindowManager.LayoutParams.FILL_PARENT);
        mWindow.setHeight(WindowManager.LayoutParams.FILL_PARENT);
        mWindow.setTouchable(true);
        if (needFocus){
            mWindow.setFocusable(true);//保证点击空白的地方有响应
        }else{
            mWindow.setFocusable(false);
        }
        mWindow.setOutsideTouchable(true);//保证点击空白的地方popup window 消失

        mWindow.setContentView(mRootView);
    }

    class DropDownListContentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mContentData != null ? mContentData.size() : 0;
        }

        @Override
        public ListItem getItem(int position) {
            return mContentData != null ? mContentData.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null){
                holder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.drop_down_dialog_item, parent, false);
                holder.tvDesc = (TextView) convertView.findViewById(R.id.drop_down_dlg_item_desc);
                holder.ivIcon = (ImageView) convertView.findViewById(R.id.drop_down_dlg_item_icon);
                holder.divider = convertView.findViewById(R.id.drop_down_dlg_item_divider);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            int totalCount = getCount();
            if (totalCount <= 1) {
                holder.divider.setVisibility(View.GONE);
            } else {
                if (position < totalCount - 1) {
                    holder.divider.setVisibility(View.VISIBLE);
                } else {
                    holder.divider.setVisibility(View.GONE);
                }
            }
            ListItem item = getItem(position);
            if (item != null) {
                holder.tvDesc.setText(item.itemDesc);
                holder.ivIcon.setImageResource(item.itemIcon);
            }
            return convertView;
        }
    }

    class ListItem {
        ListItem(int id, String desc, int iconRid){
            itemId = id;
            itemDesc = desc;
            itemIcon = iconRid;
        }
        int itemId;
        String itemDesc;
        int itemIcon;
    }

    class ViewHolder {
        TextView tvDesc;
        ImageView ivIcon;
        View divider;
    }

    public interface DropDownDialogCallback {
        void onItemClick(int itemId, String desc);
        void onDismiss();
    }

}
