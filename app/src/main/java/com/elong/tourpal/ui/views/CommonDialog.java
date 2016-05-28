package com.elong.tourpal.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elong.tourpal.R;

import java.util.ArrayList;

/**
 * 通用弹窗类
 * <p/>
 * CommonDialog
 */
public class CommonDialog extends Dialog {

    private TextView mTvTitle;
    private RelativeLayout mRlContent;
    private TextView mTvContent;
    private ListView mLvContent;
    private LinearLayout mLlButtons;
    private Button mBtnLeft;
    private Button mBtnRight;
    private View mBtnTopDivider;

    private View.OnClickListener mLeftBtnClickListener;
    private View.OnClickListener mRightBtnClickListener;
    private ItemSelectListener mItemSelectListener;

    private CommonDialogContentListAdapter mContentListAdapter;
    private ArrayList<String> mContentListDatas = new ArrayList<>();

    private boolean mAutoDismiss = true;

    public CommonDialog(Context context) {
        super(context, R.style.CommonDialog);
        init(context);
    }

    private void init(Context context) {
        setContentView(R.layout.common_dialog);
        mTvTitle = (TextView) findViewById(R.id.common_dialog_tv_title);
        mRlContent = (RelativeLayout) findViewById(R.id.common_dialog_rl_content);
        mTvContent = (TextView) findViewById(R.id.common_dialog_tv_content);
        mBtnLeft = (Button) findViewById(R.id.common_dialog_btn_left);
        mBtnRight = (Button) findViewById(R.id.common_dialog_btn_right);
        mLvContent = (ListView) findViewById(R.id.common_dialog_lv_content);
        mLlButtons = (LinearLayout) findViewById(R.id.common_dialog_ll_bottom);
        mBtnTopDivider = findViewById(R.id.common_dialog_bottom_divider);
        mContentListAdapter = new CommonDialogContentListAdapter(context);

        mLvContent.setAdapter(mContentListAdapter);
        mLvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mItemSelectListener != null && mContentListAdapter != null) {
                    mItemSelectListener.onItemSelect(mContentListDatas, position);
                    if (mAutoDismiss) {
                        dismiss();
                    }
                }
            }
        });

        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeftBtnClickListener != null) {
                    mLeftBtnClickListener.onClick(v);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });

        mBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRightBtnClickListener != null) {
                    mRightBtnClickListener.onClick(v);
                }
                if (mAutoDismiss) {
                    dismiss();
                }
            }
        });

        setCanceledOnTouchOutside(true);
    }

    public void show() {
        super.show();
    }

    public void setTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setTitle(int rid) {
        mTvTitle.setText(rid);
    }

    public void setMessage(int rid) {
        mTvContent.setVisibility(View.VISIBLE);
        mTvContent.setText(rid);
    }

    public void setMessage(String msg) {
        mTvContent.setVisibility(View.VISIBLE);
        mTvContent.setText(msg);
    }

    public void setLeftBtnText(int rid) {
        mBtnLeft.setText(rid);
    }

    public void setRightBtnText(int rid) {
        mBtnRight.setText(rid);
    }

    public void setLeftBtnOnclickListener(View.OnClickListener listener) {
        mLeftBtnClickListener = listener;
    }

    public void setRightBtnOnclickListener(View.OnClickListener listener) {
        mRightBtnClickListener = listener;
    }

    public void setContentList(ArrayList<String> datas, ItemSelectListener listener) {
        if (datas != null) {
            this.mContentListDatas.addAll(datas);
            this.mItemSelectListener = listener;
        }
//        int paddingBottom = mRlContent.getPaddingBottom();
        mRlContent.setPadding(0, 0, 0, 0);
        setButtonsVisibility(false);
    }

    public void setDialogContentView(int layoutRes) {
        if (mRlContent != null) {
            mRlContent.removeAllViews();
            mRlContent.inflate(getContext(), layoutRes, mRlContent);
        }
    }

    public void setButtonsVisibility(boolean isVisible) {
        if (isVisible) {
            mLlButtons.setVisibility(View.VISIBLE);
            mRlContent.setBackgroundResource(R.color.white);
            mBtnTopDivider.setVisibility(View.VISIBLE);
        } else {
            mRlContent.setBackgroundResource(R.drawable.bg_common_dialog_bottom);
            mLlButtons.setVisibility(View.GONE);
            mBtnTopDivider.setVisibility(View.INVISIBLE);
        }
    }

    public void setAutoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
    }

    class CommonDialogContentListAdapter extends BaseAdapter {

        private Context context;

        public CommonDialogContentListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mContentListDatas != null ? mContentListDatas.size() : 0;
        }

        @Override
        public String getItem(int position) {
            return mContentListDatas != null ? mContentListDatas.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.common_dialog_list_item, parent, false);
                holder.tvItem = (TextView) convertView.findViewById(R.id.common_dialog_tv_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (mContentListDatas != null) {
                if (position == mContentListDatas.size() - 1) {
                    convertView.setBackgroundResource(R.drawable.white_clickable_bg_bottom_round_corner);
                } else {
                    convertView.setBackgroundResource(R.drawable.white_clickable_bg);
                }
                String item = mContentListDatas.get(position);
                if (item != null) {
                    holder.tvItem.setText(item);
                }
            }
            return convertView;
        }
    }

    class ViewHolder {
        TextView tvItem;
    }

    public interface ItemSelectListener {
        public void onItemSelect(ArrayList<String> datas, int position);
    }

    public void setDialogMargin(int left, int top, int right, int bottom) {
        View v = findViewById(R.id.common_dialog_root);
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) lp;
        p.setMargins(left, top, right, bottom);
        v.requestLayout();
    }

}
