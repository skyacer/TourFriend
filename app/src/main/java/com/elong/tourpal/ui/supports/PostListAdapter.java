package com.elong.tourpal.ui.supports;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.activities.EditContactInfoActivity;
import com.elong.tourpal.ui.activities.GalleryActivity;
import com.elong.tourpal.ui.activities.LoginWebviewActivity;
import com.elong.tourpal.ui.activities.PersonalCenterActivity;
import com.elong.tourpal.ui.views.CommonDialog;
import com.elong.tourpal.ui.views.RelativeGridLayout;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * PostListAdapter 帖子列表数据的adapter
 *
 * @author tao.chen1
 */
public class PostListAdapter extends BaseAdapter {

    private static final String TAG = "PostListAdapter";

    private Context context;
    private List<MessageProtos.PostResponseInfo> datas = new ArrayList<>();
    private OnWannaJoinListener onWannaJoinListener = null;
    private HashMap<String, MessageProtos.PostResponseInfo> mPostInfoMap = new HashMap<>();
    private boolean avatarClickEnable = true;

    public PostListAdapter(Context context) {
        this.context = context;
    }

    public List<MessageProtos.PostResponseInfo> getDatas() {
        return datas;
    }

    public void setAvatarClickEnable(boolean avatarClickEnable) {
        this.avatarClickEnable = avatarClickEnable;
    }

    public void setDatas(List<MessageProtos.PostResponseInfo> datas) {
        this.datas = datas;
        mPostInfoMap.clear();
        if (datas != null) {
            addDatas2Map(datas);
        }
    }

    public void addDatas(List<MessageProtos.PostResponseInfo> datas) {
        if (datas != null) {
            if (this.datas == null) {
                this.datas = new ArrayList<>();
            }
            this.datas.addAll(datas);
            addDatas2Map(datas);
        }
    }

    public void removeData(String postId) {
        if (datas != null && !TextUtils.isEmpty(postId)) {
            if (mPostInfoMap != null) {
                MessageProtos.PostResponseInfo post = mPostInfoMap.remove(postId);
                if (post != null) {
                    datas.remove(post);
                }
            }
        }
    }

    public MessageProtos.PostResponseInfo getItemByPostId(String postId) {
        if (postId != null) {
            return mPostInfoMap.get(postId);
        }
        return null;
    }

    private void addDatas2Map(List<MessageProtos.PostResponseInfo> datas) {
        for (MessageProtos.PostResponseInfo info : datas) {
            if (info != null) {
                mPostInfoMap.put(info.getId(), info);
            }
        }
    }

    public void setOnWannaJoinListener(OnWannaJoinListener onWannaJoinListener) {
        this.onWannaJoinListener = onWannaJoinListener;
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public MessageProtos.PostResponseInfo getItem(int position) {
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
            convertView = inflater.inflate(R.layout.post_item, parent, false);
            holder.civAvatar = (CustomImageView) convertView.findViewById(R.id.pi_iv_avatar);
            holder.tvNickName = (TextView) convertView.findViewById(R.id.pi_tv_nick_name);
            holder.tvSexAndAge = (TextView) convertView.findViewById(R.id.pi_tv_sex_and_age);
            holder.tvCreateTime = (TextView) convertView.findViewById(R.id.pi_tv_create_time);
            holder.tvBtnWannaJoin = (Button) convertView.findViewById(R.id.pi_iv_wanna_join);
            holder.ivStatus = (ImageView) convertView.findViewById(R.id.pi_iv_status);
            holder.tvDest = (TextView) convertView.findViewById(R.id.pi_tv_dest);
            holder.tvDuration = (TextView) convertView.findViewById(R.id.pi_tv_duration);
//            holder.tvContactInfo = (TextView) convertView.findViewById(R.id.pi_tv_contact_info);
            holder.tvContent = (TextView) convertView.findViewById(R.id.pi_tv_content);
            holder.tvLocation = (TextView) convertView.findViewById(R.id.pi_tv_location);
            holder.tvWannaJoinNum = (TextView) convertView.findViewById(R.id.pi_tv_wanna_join_num);
            holder.rglImgs = (RelativeGridLayout) convertView.findViewById(R.id.pi_rgl_imgs);
            holder.tvUv = (TextView) convertView.findViewById(R.id.pi_tv_uv);
//            holder.tvDelete = (TextView) convertView.findViewById(R.id.pi_tv_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MessageProtos.PostResponseInfo info = getItem(position);
        if (info != null) {
            final MessageProtos.UserInfo uInfo = info.getUserInfo();
            if (uInfo != null) {
                String avatarThumbUrl = uInfo.getHeadImgUrl();
                if (!TextUtils.isEmpty(avatarThumbUrl)) {
                    holder.civAvatar.setImageUrl(avatarThumbUrl);
                    holder.civAvatar.loadImage();
                } else {
                    holder.civAvatar.setImageResource(Utils.getAvatarId(Integer.parseInt(uInfo.getId())));
                }
                holder.civAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //打点数据
                        Statistics.log(context, StatisticsEnv.DESTINATION_HEADICON);

                        if (avatarClickEnable) {
                            PersonalCenterActivity.startActivity(context, new String(uInfo.toByteArray()));
                        }
                    }
                });
                holder.tvNickName.setText(uInfo.getNickName());
                holder.tvSexAndAge.setText(String.valueOf(uInfo.getAge()));
                if (uInfo.getSex() == 1) {
                    //男
                    holder.tvSexAndAge.setBackgroundResource(R.drawable.bg_male);
                    holder.tvSexAndAge.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_male, 0, 0, 0);
                } else if (uInfo.getSex() == 2) {
                    //女
                    holder.tvSexAndAge.setBackgroundResource(R.drawable.bg_female);
                    holder.tvSexAndAge.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_female, 0, 0, 0);
                }
                try {
                    long createdTime = Long.parseLong(info.getCreatedTime());
                    holder.tvCreateTime.setText(Utils.getFormattedTime(createdTime * 1000));
                } catch (Exception e){
                    holder.tvCreateTime.setText("");
                }
                holder.tvBtnWannaJoin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onWannaJoinListener != null) {
                            if (TourPalApplication.getInstance().hasLogin()) {
                                if (!Utils.isMyUserInfoHasNickname()) {
                                    UiUtils.showUserInfoIncompleteDlg(context);
                                } else {
                                    //打点数据
                                    Statistics.log(context, StatisticsEnv.COMMUNICATION_INVITATION);

                                    int state = info.getIsLiked() ? 0 : 1;//0是要取消求同行，1是要求同行
                                    if (state == 1) {
                                        SharedPref pref = SharedPref.getInstance();
                                        if (!pref.hasShowJoinTipDlg()) {
                                            showDialog2ConfirmWannaJoin(position);
                                            pref.setHasShowJoinTipDlg();
                                        } else {
                                            sendWannaJoin(position);
                                        }
                                    } else if (state == 0) {
                                        showDialog2ConfirmCancelWannaJoin(position);
                                    }
                                }
                            } else {
                                //未登录，跳转到登录页面，让用户登录
                                Intent intent = new Intent(context, LoginWebviewActivity.class);
                                context.startActivity(intent);
                            }
                        }
                    }
                });
                if (info.getIsLiked()) {
                    holder.tvBtnWannaJoin.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_joined, 0, 0, 0);
                    holder.tvBtnWannaJoin.setText(R.string.pi_btn_text_wanna_join_cancel);
                } else {
                    holder.tvBtnWannaJoin.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_join, 0, 0, 0);
                    holder.tvBtnWannaJoin.setText(R.string.pi_btn_text_wanna_join);
                }
                holder.tvUv.setText(context.getString(R.string.pi_uv_format, info.getPv()));
                switch (info.getStatus()) {
                    case MessageProtos.CLOSED:
                        holder.tvBtnWannaJoin.setVisibility(View.GONE);
                        holder.ivStatus.setImageResource(R.mipmap.ic_post_status_finished);
                        break;
                    case MessageProtos.OPENED:
                        if (info.getIsMy()){
                            holder.tvBtnWannaJoin.setVisibility(View.GONE);
                        } else {
                            holder.tvBtnWannaJoin.setVisibility(View.VISIBLE);
                        }
                        holder.ivStatus.setImageResource(R.mipmap.ic_post_status_recruiting);
                        break;
                }
//                if (info.getIsMy()) {
//                    holder.tvDelete.setVisibility(View.VISIBLE);
//                } else {
//                    holder.tvDelete.setVisibility(View.INVISIBLE);
//                }
//                holder.tvDelete.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        showDialog2ConfirmPostDelete(info.getId());
//                    }
//                });
                holder.tvDest.setText(info.getDest().replace(",", "、"));
                holder.tvDuration.setText(context.getString(R.string.pi_duration_format, info.getStartTime(), info.getDays()));
//                String wechat = String.format(context.getString(R.string.pi_contact_info_format), context.getString(R.string.pi_contact_info_type_wechat), info.getWeixin());
//                String qq = String.format(context.getString(R.string.pi_contact_info_format), context.getString(R.string.pi_contact_info_type_qq), info.getQq());
//                String phone = String.format(context.getString(R.string.pi_contact_info_format), context.getString(R.string.pi_contact_info_type_phone), info.getPhone());
//                StringBuilder sbContactInfo = new StringBuilder();
//                if (!TextUtils.isEmpty(info.getWeixin())) {
//                    sbContactInfo.append(wechat);
//                    sbContactInfo.append("，");
//                }
//                if (!TextUtils.isEmpty(info.getQq())) {
//                    sbContactInfo.append(qq);
//                    sbContactInfo.append("，");
//                }
//                if (!TextUtils.isEmpty(info.getPhone())) {
//                    sbContactInfo.append(phone);
//                    sbContactInfo.append("，");
//                }
//                sbContactInfo.replace(sbContactInfo.length() - 1, sbContactInfo.length(), "");
//                holder.tvContactInfo.setText(sbContactInfo);
                holder.tvContent.setText(info.getContent());
                if (!TextUtils.isEmpty(info.getPostPlace())) {
                    holder.tvLocation.setText(info.getPostPlace());
                    holder.tvLocation.setVisibility(View.VISIBLE);
                } else {
                    holder.tvLocation.setVisibility(View.GONE);
                }
                holder.tvWannaJoinNum.setText(context.getString(R.string.pi_wanna_join_num_format, String.valueOf(info.getLikeNum())));
//                holder.tvWannaJoinNum.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        WannaJoinListActivity.startActivityByPostId(context, info.getId());
//                    }
//                });

                //图片加载
                final ArrayList<String> previewUrls = new ArrayList<>();
                final ArrayList<String> thumbUrls = new ArrayList<>();
                int imgCount = info.getImgsCount();
                for (int i = 0; i < imgCount; i++) {
                    MessageProtos.PostImg postImg = info.getImgs(i);
                    if (postImg != null) {
                        previewUrls.add(postImg.getPreview());
                        thumbUrls.add(postImg.getThumb());
                    }
                }
                if (imgCount > 0) {
                    holder.rglImgs.setVisibility(View.VISIBLE);
                    for (int i = 0; i < imgCount; i++) {
                        MessageProtos.PostImg postImg = info.getImgs(i);
                        if (postImg != null) {
                            CustomImageView civ = (CustomImageView) holder.rglImgs.getChildView(i);
                            if (civ == null) {
                                civ = new CustomImageView(context);
                                civ.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                holder.rglImgs.addChildView(civ);
                            } else {
                                civ.setVisibility(View.VISIBLE);
                            }
                            civ.setImageUrl(postImg.getThumb());
                            civ.setImageResource(R.mipmap.thumb_default);
                            civ.loadImage();
                            //设置缩略图点击事件（预览）
                            final int idx = i;
                            civ.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //打点数据
                                    Statistics.log(context, StatisticsEnv.DESTINATION_PHOTO);

                                    GalleryActivity.startActivity(context, idx, previewUrls, thumbUrls);
                                }
                            });
                        }
                        holder.rglImgs.resetChildViews(i + 1);
                    }
                } else {
                    holder.rglImgs.setVisibility(View.GONE);
                }
            }
        }
        return convertView;
    }

    /**
     * 设置状态，同行人数加/减1
     *
     * @param position
     * @param isLiked
     */
    private void changeWannaJoinState(int position, boolean isLiked) {
        MessageProtos.PostResponseInfo info = getItem(position);
        if (info != null) {
            info.setIsLiked(isLiked);
            int num = info.getLikeNum();
            info.setLikeNum(isLiked ? num + 1 : num - 1);
        }
    }

    private void showDialog2ConfirmCancelWannaJoin(final int position) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setTitle(R.string.common_tips);
        dialog.setMessage(R.string.pi_cancel_wanna_join_dialog_content);
        dialog.setRightBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageProtos.PostResponseInfo info = getItem(position);
                String postId = info.getId();
                changeWannaJoinState(position, !info.getIsLiked());
                notifyDataSetChanged();
                onWannaJoinListener.onWannaJoin(postId, 0);
            }
        });
        dialog.show();
    }

    private void showDialog2ConfirmWannaJoin(final int position) {
        CommonDialog joinTipsDlg = new CommonDialog(context);
        joinTipsDlg.setTitle(R.string.common_tips);
        joinTipsDlg.setMessage(R.string.pd_join_tips_dlg_message);
        joinTipsDlg.setLeftBtnText(R.string.common_modify);
        joinTipsDlg.setLeftBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditContactInfoActivity.class);
                context.startActivity(intent);
            }
        });
        joinTipsDlg.setRightBtnText(R.string.common_confirm);
        joinTipsDlg.setRightBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWannaJoin(position);
            }
        });
        joinTipsDlg.show();
    }

    private void sendWannaJoin(int position) {
        MessageProtos.PostResponseInfo info = getItem(position);
        String postId = info.getId();
        changeWannaJoinState(position, !info.getIsLiked());
        notifyDataSetChanged();
        onWannaJoinListener.onWannaJoin(postId, 1);
    }

    private void showDialog2ConfirmPostDelete(final String postId) {
        CommonDialog dialog = new CommonDialog(context);
        dialog.setTitle(R.string.common_tips);
        dialog.setMessage(R.string.pl_delete_post_dialog_content);
        dialog.setRightBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onWannaJoinListener != null) {
                    onWannaJoinListener.onPostDelete(postId);
                }
            }
        });
        dialog.show();
    }

    class ViewHolder {
        CustomImageView civAvatar;
        TextView tvNickName;
        TextView tvSexAndAge;
        TextView tvCreateTime;
        ImageView ivStatus;
        Button tvBtnWannaJoin;
        TextView tvDest;
        TextView tvDuration;
//        TextView tvContactInfo;
        TextView tvContent;
        RelativeGridLayout rglImgs;
        TextView tvLocation;
        TextView tvWannaJoinNum;
        TextView tvUv;
//        TextView tvDelete;
    }

    public interface OnWannaJoinListener {
        public void onWannaJoin(String postId, int state);

        public void onPostDelete(String postId);
    }
}
