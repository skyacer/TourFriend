package com.elong.tourpal.ui.supports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.elong.tourpal.R;

import java.util.ArrayList;

/**
 * Created by zhitao.xu on 2015/5/19.
 */
public class TagsGridAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<TagData> mData;
    private View.OnClickListener mOnClickListener;

    public TagsGridAdapter (Context context, ArrayList<TagData> datas, View.OnClickListener listener) {
        mContext = context;
        mData = datas;
        mOnClickListener = listener;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.tag_layout, null);
            holder = new ViewHolder();
            holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.tag_checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TagData d = mData.get(position);
        holder.mCheckBox.setOnClickListener(mOnClickListener);
        if (d.mIsChecked) {
            holder.mCheckBox.setChecked(true);
        } else {
            holder.mCheckBox.setChecked(false);
        }
        holder.mCheckBox.setText(d.mTitle);

        return convertView;
    }

    private class ViewHolder {
        CheckBox mCheckBox;
    }

    public static class TagData {
        public String mTitle;
        public boolean mIsChecked = false;
    }
}
