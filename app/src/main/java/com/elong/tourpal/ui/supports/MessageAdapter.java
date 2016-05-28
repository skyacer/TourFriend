package com.elong.tourpal.ui.supports;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.activities.PersonalCenterActivity;
import com.elong.tourpal.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageAdapter
 */
public class MessageAdapter extends BaseAdapter {

    private Context context;
    private List<MessageProtos.LikedMessage> datas;
    private OnMessageReadListener onMessageReadListener = null;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void setDatas(List<MessageProtos.LikedMessage> datas) {
        this.datas = datas;
    }

    public List<MessageProtos.LikedMessage> getDatas() {
        return datas;
    }

    public void addDatas(List<MessageProtos.LikedMessage> datas) {
        if (datas != null) {
            if (this.datas == null) {
                datas = new ArrayList<>();
            }
            datas.addAll(datas);
        }
    }

    public void setOnMessageReadListener(OnMessageReadListener onMessageReadListener) {
        this.onMessageReadListener = onMessageReadListener;
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public MessageProtos.LikedMessage getItem(int position) {
        return datas == null ? null : datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.message_item, parent, false);
            holder.civAvatar = (CustomImageView) convertView.findViewById(R.id.mi_iv_avatar);
            holder.ivNew = (ImageView) convertView.findViewById(R.id.mi_iv_new);
            holder.tvNickName = (TextView) convertView.findViewById(R.id.mi_tv_nick_name);
            holder.tvSexAndAge = (TextView) convertView.findViewById(R.id.mi_tv_sex_and_age);
            holder.tvWannerJoin = (TextView) convertView.findViewById(R.id.mi_tv_wanna_join);
            holder.tvMessageTime = (TextView) convertView.findViewById(R.id.mi_tv_message_time);
            holder.tvWechat = (TextView) convertView.findViewById(R.id.mi_tv_wechat);
            holder.tvQQ = (TextView) convertView.findViewById(R.id.mi_tv_qq);
            holder.tvPhone = (TextView) convertView.findViewById(R.id.mi_tv_phone);
            holder.rlContacts = (RelativeLayout) convertView.findViewById(R.id.mi_rl_contacts);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MessageProtos.LikedMessage likedMsg = getItem(position);
        if (likedMsg != null) {
            MessageProtos.UserInfo userInfo = likedMsg.getUserInfo();
            final String userInfoStr = new String(userInfo.toByteArray());
            if (userInfo != null) {
                String avatarThumbUrl = userInfo.getHeadImgUrl();
                if (!TextUtils.isEmpty(avatarThumbUrl)) {
                    holder.civAvatar.setImageUrl(avatarThumbUrl);
                    holder.civAvatar.loadImage();
                } else {
                    holder.civAvatar.setImageResource(Utils.getAvatarId(Integer.parseInt(userInfo.getId())));
                }
                holder.civAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //打点数据
                        Statistics.log(context, StatisticsEnv.COMMUNICATION_MESSAGE_HEADICON);

                        //先本地消除红点,再发送请求让服务端消除未读标识
                        likedMsg.setIsRead(1);
                        notifyDataSetChanged();
                        if (onMessageReadListener != null) {
                            onMessageReadListener.onMessageRead(likedMsg.getId(), position);
                        }
                        //跳转到这个用户的个人主页
                        PersonalCenterActivity.startActivity(context, userInfoStr);
                    }
                });
                holder.ivNew.setVisibility(likedMsg.getIsRead() == 0 ? View.VISIBLE : View.GONE);
                String nickName = userInfo.getNickName();
                if (nickName == null) {
                    nickName = "";
                }
                holder.tvNickName.setText(nickName);
                try {
                    long createdTime = Long.parseLong(likedMsg.getCreatedAt());
                    holder.tvMessageTime.setText(Utils.getFormattedTime(createdTime * 1000));
                } catch (Exception e){
                    holder.tvMessageTime.setText("");
                }
                String dest = likedMsg.getDest();
                if (dest == null) {
                    dest = "";
                } else {
                    dest = dest.replace(",", "、");
                }
                holder.tvWannerJoin.setText(context.getString(R.string.wanna_go_to, dest));
                holder.tvSexAndAge.setText(String.valueOf(userInfo.getAge()));
                if (userInfo.getSex() == 1) {
                    //男
                    holder.tvSexAndAge.setBackgroundResource(R.drawable.bg_male);
                    holder.tvSexAndAge.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_male, 0, 0, 0);
                } else if (userInfo.getSex() == 2) {
                    //女
                    holder.tvSexAndAge.setBackgroundResource(R.drawable.bg_female);
                    holder.tvSexAndAge.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_female, 0, 0, 0);
                }
                //联系方式
                final String wechat = userInfo.getWeixin();
                final String qq = userInfo.getQq();
                final String phone = userInfo.getPhone();
                String emptyStr = context.getString(R.string.mi_empty_contact_info);
                setContactInfoView(holder.tvWechat, wechat, emptyStr);
                setContactInfoView(holder.tvQQ, qq, emptyStr);
                setContactInfoView(holder.tvPhone, phone, emptyStr);
                holder.rlContacts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UiUtils.showHandleContactInfoDialog(context, wechat, qq, phone);
                    }
                });
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
        ImageView ivNew;
        TextView tvNickName;
        TextView tvSexAndAge;
        TextView tvWannerJoin;
        TextView tvMessageTime;
        TextView tvWechat;
        TextView tvQQ;
        TextView tvPhone;
        RelativeLayout rlContacts;
    }

    public interface OnMessageReadListener {
        public void onMessageRead(String messageId, int position);
    }

}
