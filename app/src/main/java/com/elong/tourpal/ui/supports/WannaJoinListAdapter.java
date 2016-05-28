package com.elong.tourpal.ui.supports;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.activities.PersonalCenterActivity;
import com.elong.tourpal.utils.Utils;

import java.util.List;

/**
 * WannaJoinListAdapter
 */
public class WannaJoinListAdapter extends BaseAdapter {

    private Context context;
    private List<MessageProtos.LikedMessage> data;
    private boolean isPostOwner = false;

    public WannaJoinListAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<MessageProtos.LikedMessage> data) {
        this.data = data;
    }

    public void setIsPostOwner(boolean isPostOwner) {
        this.isPostOwner = isPostOwner;
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
            convertView = inflater.inflate(R.layout.wanna_join_list_item, parent, false);
            holder.civAvatar = (CustomImageView) convertView.findViewById(R.id.wjli_iv_avatar);
            holder.tvNickName = (TextView) convertView.findViewById(R.id.wjli_tv_nick_name);
            holder.tvSexAndAge = (TextView) convertView.findViewById(R.id.wjli_tv_sex_and_age);
            holder.tvWechat = (TextView) convertView.findViewById(R.id.wjli_tv_wechat);
            holder.tvQQ = (TextView) convertView.findViewById(R.id.wjli_tv_qq);
            holder.tvPhone = (TextView) convertView.findViewById(R.id.wjli_tv_phone);
            holder.rlContactInfos = (RelativeLayout) convertView.findViewById(R.id.wjli_rl_contact_infos);
            holder.divider = convertView.findViewById(R.id.wjli_separator);
            holder.tvCreatedTime = (TextView) convertView.findViewById(R.id.wjli_tv_created_time);
            holder.tvContactInfoHide = (TextView) convertView.findViewById(R.id.wjli_tv_contact_info_hide);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MessageProtos.LikedMessage likedMessage = getItem(position);
        if (likedMessage != null) {
            MessageProtos.UserInfo userInfo = likedMessage.getUserInfo();
            final String userInfoStr = new String(userInfo.toByteArray());
            if (userInfo != null) {
                String avatarThumbUrl = userInfo.getHeadImgUrl();
                if (!TextUtils.isEmpty(avatarThumbUrl)) {
                    holder.civAvatar.setImageUrl(avatarThumbUrl);
                    holder.civAvatar.loadImage();
                } else {
                    holder.civAvatar.setImageResource(Utils.getAvatarId((Integer.parseInt(userInfo.getId()))));
                }
                holder.civAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PersonalCenterActivity.startActivity(context, userInfoStr);
                    }
                });
                String nickName = userInfo.getNickName();
                if (nickName == null) {
                    nickName = "";
                }
                holder.tvNickName.setText(nickName);
                holder.tvSexAndAge.setText(String.valueOf(userInfo.getAge()));
                int sex = userInfo.getSex();
                if (sex == 1) {
                    //男
                    holder.tvSexAndAge.setBackgroundResource(R.drawable.bg_male);
                    holder.tvSexAndAge.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_male, 0, 0, 0);
                } else if (sex == 2) {
                    //女
                    holder.tvSexAndAge.setBackgroundResource(R.drawable.bg_female);
                    holder.tvSexAndAge.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_female, 0, 0, 0);
                }
                try {
                    long createdTime = Long.parseLong(likedMessage.getCreatedAt());
                    holder.tvCreatedTime.setText(Utils.getFormattedTime(createdTime * 1000));
                } catch (Exception e){
                    holder.tvCreatedTime.setText("");
                }
                //联系方式
                int visibleItemsInt = userInfo.getVisibleItem();
                boolean shareContactInfo = userInfo.getGroupVisible();
                if (shareContactInfo || isPostOwner) {
                    holder.rlContactInfos.setVisibility(View.VISIBLE);
                    holder.tvContactInfoHide.setVisibility(View.GONE);
                    boolean wechatVisible = (visibleItemsInt & 1) == 1;
                    boolean qqVisible = (visibleItemsInt & 2) == 2;
                    boolean phoneVisible = (visibleItemsInt & 4) == 4;
                    final String wechat = wechatVisible ? userInfo.getWeixin() : "";
                    final String qq = qqVisible ? userInfo.getQq() : "";
                    final String phone = phoneVisible ? userInfo.getPhone() : "";
                    String emptyStr = context.getString(R.string.mi_empty_contact_info);
                    setContactInfoView(holder.tvWechat, wechat, emptyStr);
                    setContactInfoView(holder.tvQQ, qq, emptyStr);
                    setContactInfoView(holder.tvPhone, phone, emptyStr);
                    holder.rlContactInfos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UiUtils.showHandleContactInfoDialog(context, wechat, qq, phone);
                        }
                    });
                } else {
                    holder.rlContactInfos.setVisibility(View.GONE);
                    holder.tvContactInfoHide.setVisibility(View.VISIBLE);
                }
            }
        }
        return convertView;
    }

    private void setContactInfoView(TextView tvContact, final String contactInfo, String emptyStr) {
        if (TextUtils.isEmpty(contactInfo)) {
            tvContact.setText(emptyStr);
            tvContact.setTextColor(context.getResources().getColor(R.color.mi_item_contacts_text_empty));
        } else {
            tvContact.setText(contactInfo);
            tvContact.setTextColor(context.getResources().getColor(R.color.mi_item_contacts_text_normal));
        }
    }

    class ViewHolder {
        CustomImageView civAvatar;
        TextView tvNickName;
        TextView tvSexAndAge;
        TextView tvWechat;
        TextView tvQQ;
        TextView tvPhone;
        RelativeLayout rlContactInfos;
        View divider;
        TextView tvCreatedTime;
        TextView tvContactInfoHide;
    }
}
