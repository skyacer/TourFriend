package com.elong.tourpal.ui.supports;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.imageasyncloader.impl.CustomFileCacheManager;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;
import com.elong.tourpal.protocal.MessageProtos;

import java.util.List;

/**
 * HotDestinationsAdapter
 *
 * @author chentao
 */
public class HotDestinationsAdapter extends BaseAdapter {

    private Context context = null;
    private List<MessageProtos.HotCity> datas = null;

    public HotDestinationsAdapter(Context context) {
        this.context = context;
    }

    public void setDatas(List<MessageProtos.HotCity> datas) {
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas != null ? datas.size() : 0;
    }

    @Override
    public MessageProtos.HotCity getItem(int position) {
        return datas != null ? datas.get(position) : null;
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
            convertView = inflater.inflate(R.layout.hot_destination_item, parent, false);
            holder.ivBackground = (CustomImageView) convertView.findViewById(R.id.hdi_iv_bg);
            holder.tvName = (TextView) convertView.findViewById(R.id.hdi_tv_name);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.hdi_description);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MessageProtos.HotCity destination = getItem(position);
        if (destination != null) {
            String name = destination.getName() == null ? "" : destination.getName();
            String description = destination.getDesc() == null ? "" : destination.getDesc();
            String imgUrl = destination.getUrl();
            holder.tvName.setText(name);
            holder.tvDescription.setText(description);
            if (!TextUtils.isEmpty(imgUrl)) {
                holder.ivBackground.setImageUrl(imgUrl);
                holder.ivBackground.setImageResource(R.color.common_gray2_bg);
                holder.ivBackground.setFileCacheManagerType(CustomFileCacheManager.TYPE_HOT_DESTINATION);
                holder.ivBackground.loadImage();
            }
        }
        return convertView;
    }

    class ViewHolder {
        CustomImageView ivBackground;
        TextView tvName;
        TextView tvDescription;
    }
}
