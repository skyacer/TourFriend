package com.elong.tourpal.ui.supports;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.ui.activities.EditUserInfoActivity;
import com.elong.tourpal.ui.views.CommonDialog;
import com.elong.tourpal.utils.Utils;

import java.util.ArrayList;

/**
 * UiUtils
 * <p/>
 * ui工具类，例如各种公用ui弹窗逻辑
 */
public class UiUtils {

    /**
     * 弹窗处理联系信息
     */
    public static void showHandleContactInfoDialog(final Context context, final String wechat, final String qq, final String phone) {
        ArrayList<String> contentList = new ArrayList<>();
        final String copyWechat = context.getString(R.string.dlg_item_copy_wechat);
        final String copyQq = context.getString(R.string.dlg_item_copy_qq);
        final String dialPhone = context.getString(R.string.dlg_item_dial);
        if (!TextUtils.isEmpty(wechat)) {
            contentList.add(copyWechat);
        }
        if (!TextUtils.isEmpty(qq)) {
            contentList.add(copyQq);
        }
        if (!TextUtils.isEmpty(phone)) {
            contentList.add(dialPhone);
        }
        if (contentList.size() == 0) {
            return;
        }

        CommonDialog dlg = new CommonDialog(context);
        dlg.setTitle(R.string.common_tips);
        dlg.setContentList(contentList, new CommonDialog.ItemSelectListener() {
            @Override
            public void onItemSelect(ArrayList<String> datas, int position) {
                if (position >= 0 && position < datas.size()) {
                    String data = datas.get(position);
                    if (!TextUtils.isEmpty(data)) {
                        if (data.equals(dialPhone)) {
                            Utils.dial(context, phone);
                        } else if (data.equals(copyWechat)) {
                            Utils.copy2ClipBoard(context, wechat);
                            Toast.makeText(context, R.string.about_copy_done, Toast.LENGTH_SHORT).show();
                        } else if (data.equals(copyQq)) {
                            Utils.copy2ClipBoard(context, qq);
                            Toast.makeText(context, R.string.about_copy_done, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        dlg.show();
    }

    /**
     * 弹出需要完善个人资料的弹窗
     *
     * @param context Activity
     */
    public static void showUserInfoIncompleteDlg(final Context context) {
        CommonDialog dlg = new CommonDialog(context);
        dlg.setTitle(R.string.common_tips);
        dlg.setMessage(R.string.dlg_message_user_info_incomplete);
        dlg.setRightBtnOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditUserInfoActivity.class);
                context.startActivity(intent);
            }
        });
        dlg.setRightBtnText(R.string.go_2_complete);
        dlg.setLeftBtnText(R.string.common_cancel);
        dlg.show();
    }
}
