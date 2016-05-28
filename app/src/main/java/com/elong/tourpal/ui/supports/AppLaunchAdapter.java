package com.elong.tourpal.ui.supports;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.share.ShareManager;
import com.elong.tourpal.ui.supports.album.AlbumConstant;

import java.util.ArrayList;
import java.util.List;

public class AppLaunchAdapter extends BaseAdapter {
    private LayoutInflater mInflater; // 视图容器
    private Context mContext;
    private List<ShareManager.ShareAdapterData> mAppDatas;

    public AppLaunchAdapter(Context context, List<ShareManager.ShareAdapterData> data) {
        mContext = context;
        mAppDatas = data;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return (mAppDatas.size());
    }

    public Object getItem(int arg0) {

        return mAppDatas.get(arg0);
    }

    public long getItemId(int arg0) {

        return 0;
    }

    /**
     * ListView Item设置
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.item_app_launch,
                    parent, false);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView
                    .findViewById(R.id.item_app_icon);
            holder.name = (TextView) convertView
                    .findViewById(R.id.item_app_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ShareManager.ShareAdapterData d = mAppDatas.get(position);
        holder.icon.setImageDrawable(d.mDrawble);
        holder.name.setText(d.mName);

        return convertView;
    }

    public class ViewHolder {
        public ImageView icon;
        public TextView name;
    }

}
