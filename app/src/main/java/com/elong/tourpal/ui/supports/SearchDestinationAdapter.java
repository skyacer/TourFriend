package com.elong.tourpal.ui.supports;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.search.DestinationDataManager;

import java.util.ArrayList;

/**
 * Created by tao.chen1 on 2015/2/9.
 */
public class SearchDestinationAdapter extends BaseAdapter {

    public SearchDestinationAdapter(Context context) {
        mContext = context;
    }

    public SearchDestinationAdapter(Context context, ArrayList<DestinationDataManager.DestinationOrigData> datas) {
        mContext = context;
        this.datas = datas;
    }

    private Context mContext = null;
    private ArrayList<DestinationDataManager.DestinationOrigData> datas = null;

    public ArrayList<DestinationDataManager.DestinationOrigData> getDatas() {
        return datas;
    }

    public void setDatas(ArrayList<DestinationDataManager.DestinationOrigData> datas) {
        this.datas = datas;
    }

    public DestinationDataManager.DestinationOrigData getSelectData(int position) {
        return this.datas.get(position);
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public String getItem(int position) {
        return datas == null ? null : datas.get(position).mDesName;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.search_destination_item, null, false);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tvDes = (TextView) convertView.findViewById(R.id.tv_des);
            holder.tvParent = (TextView) convertView.findViewById(R.id.tv_parent);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        DestinationDataManager.DestinationOrigData destination = datas.get(position);
        if (!TextUtils.isEmpty(destination.mDesName)) {
            holder.tvDes.setText(destination.mDesName);
        }

        if (destination.mGrandparents != null) {
            DestinationDataManager.DestinationOrigData p = destination.mGrandparents.get(0);
            if (p != null) {
                holder.tvParent.setText(p.mDesName);
            } else {
                holder.tvParent.setText("");
            }
        } else {
            holder.tvParent.setText("");
        }
        return convertView;
    }

    public static class ViewHolder {
        ImageView ivIcon;
        TextView tvDes;
        TextView tvParent;
    }
}
