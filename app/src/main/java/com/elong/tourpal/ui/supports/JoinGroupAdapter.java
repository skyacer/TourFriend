package com.elong.tourpal.ui.supports;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.elong.tourpal.R;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.utils.Utils;

import java.util.List;

/**
 * JoinGroupAdapter
 */
public class JoinGroupAdapter extends BaseAdapter {

    private Context context;
    private List<MessageProtos.LikedMessage> data;

    public JoinGroupAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<MessageProtos.LikedMessage> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public MessageProtos.LikedMessage getItem(int position) {
        return data != null ? data.get(position) : null;
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
            convertView = inflater.inflate(R.layout.pd_gv_wanna_join_group_item, parent, false);
            holder.civAvatar = (CustomImageView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MessageProtos.LikedMessage likedMessage = getItem(position);
        if (likedMessage != null) {
            MessageProtos.UserInfo info = likedMessage.getUserInfo();
            if (info != null) {
                String avatarThumbUrl = info.getHeadImgUrl();
                if (!TextUtils.isEmpty(avatarThumbUrl)) {
                    holder.civAvatar.setImageUrl(avatarThumbUrl);
                    holder.civAvatar.loadImage();
                } else {
                    holder.civAvatar.setImageResource(Utils.getAvatarId(Integer.parseInt(info.getId())));
                }
            }
        }
        return convertView;
    }

    class ViewHolder{
        CustomImageView civAvatar;
    }
}
