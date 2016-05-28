package com.elong.tourpal.share;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.imageasyncloader.other.Utils;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.supports.AppLaunchAdapter;
import com.elong.tourpal.ui.views.CommonDialog;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhitao.xu on 2015/5/12.
 */
public class ShareManager {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = ShareManager.class.getSimpleName();
    private static final String SINA_WEIBO_PACKAGENAME = "com.sina.weibo";
    private static final String QQ_PACKAGENAME = "com.tencent.mobileqq";

    private static List<ResolveInfo> getShareApps(Context context) {
        List<ResolveInfo> mShareApps = new ArrayList<ResolveInfo>();
        List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("text/plain");
//      intent.setType("*/*");
        PackageManager pManager = context.getPackageManager();
        mApps = pManager.queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

        for (int i = 0; i < mApps.size(); i++) {
            ResolveInfo r = mApps.get(i);
            if (DEBUG) {
                Log.d(TAG, "package name=" + r.activityInfo.packageName);
            }
            if (SINA_WEIBO_PACKAGENAME.equals(r.activityInfo.packageName) || QQ_PACKAGENAME.equals(r.activityInfo.packageName)) {
                mShareApps.add(r);
            }
        }

        if (DEBUG) {
            for (int i = 0; i < mApps.size(); i++) {
                ResolveInfo r = mApps.get(i);

                if (DEBUG) {
                    Log.d(TAG, "package name=" + r.activityInfo.packageName);
                }
            }
        }
        return mShareApps;
    }

    /**
     * 无微信SDK进行分享微信朋友圈的接口
     *
     * @param activity
     * @param text
     * @param file
     */
    private static void shareWeChatToTimeLine(Context activity, String text, File file) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm",
                "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction("android.intent.action.SEND");
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra("Kdescription", text);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        activity.startActivity(intent);
    }

    /**
     * 分享调用总接口
     *
     * @param activity 界面Context
     */
    public static void startShare(final Context activity, MessageProtos.ShareInfo shareInfo) {
        final List<ResolveInfo> shareApps = ShareManager.getShareApps(TourPalApplication.getAppContext());
        final List<ShareAdapterData> shareAppsData = new ArrayList<ShareAdapterData>();

        if (isWeChatInstalled()) {
            ShareAdapterData d1 = new ShareAdapterData(activity.getResources().getString(R.string.share_wechat), activity.getResources().getDrawable(R.drawable.share_wechat), ShareType.WECHAT);
            shareAppsData.add(d1);
            if (isWeChatTimelineSupport()) {
                ShareAdapterData d2 = new ShareAdapterData(activity.getResources().getString(R.string.share_wechat_timeline), activity.getResources().getDrawable(R.drawable.share_wechat_timeline), ShareType.WECHAT_TIMELINE);
                shareAppsData.add(d2);
            }
        }

        if (isQQInstalled(shareApps)) {
            ShareAdapterData d3 = new ShareAdapterData(activity.getResources().getString(R.string.share_QQ), activity.getResources().getDrawable(R.drawable.share_qq), ShareType.QQ);
            shareAppsData.add(d3);
            ShareAdapterData d4 = new ShareAdapterData(activity.getResources().getString(R.string.share_Qzone), activity.getResources().getDrawable(R.drawable.share_qzone), ShareType.QZONE);
            shareAppsData.add(d4);
        }

        if (isSinaWeiboInstalled(shareApps)) {
            ShareAdapterData d5 = new ShareAdapterData(activity.getResources().getString(R.string.share_weibo), activity.getResources().getDrawable(R.drawable.share_weibo), ShareType.SINA_WEIBO);
            shareAppsData.add(d5);
        }

        CommonDialog shareDialog = new CommonDialog(activity);
        int margin = activity.getResources().getDimensionPixelSize(R.dimen.share_dialog_margin);
        shareDialog.setDialogMargin(margin, 0, margin, 0);
        shareDialog.setTitle("分享到");
        shareDialog.setButtonsVisibility(false);
        shareDialog.setDialogContentView(R.layout.share_dialog_content);
        GridView gv = (GridView) shareDialog.findViewById(R.id.share_dialog_content_gv);
        final AppLaunchAdapter adapter = new AppLaunchAdapter(activity.getApplicationContext(), shareAppsData);
        gv.setAdapter(adapter);

        final String title = shareInfo.getShareTitle();
        final String summary = shareInfo.getShareDesc();
        final String iconUrl = shareInfo.getShareIco();
        final String tagetUrl = shareInfo.getShareLink();

//        Calendar c = Calendar.getInstance();
//        if (startDate != null) {
//            c.setTime(startDate);
//        }
//        int month = c.get(Calendar.MONTH) + 1;
//        int day = c.get(Calendar.DATE);
//
//        final String tiltle = "找驴友啦！";
//        final String summary = "想和" + authorName + ",在" + month + "月" + day + "日" + ",一起去" + firstCity + "。自由行，不跟团，挺靠谱的，约吗？";
//        final String tagetUrl = shareUrl;
//        final String iconUrl = "http://kibey-fair.b0.upaiyun.com/2013/05/03/fd7bccb1d489dbc3dbe51c37119ee179.png";//getShareImagePath();
        if (DEBUG) {
            Log.d(TAG, "iconUrl=" + iconUrl);
        }

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShareAdapterData d = (ShareAdapterData) adapter.getItem(position);

                switch (d.mType) {
                    case WECHAT:
                        shareToWeChat(activity, (title == null ? "" : title), summary, BitmapFactory.decodeResource(TourPalApplication.getAppContext().getResources(), R.mipmap.icon_share_wechat), tagetUrl, false);
                        break;
                    case WECHAT_TIMELINE:
                        shareToWeChat(activity, (title == null ? "" : title), summary, BitmapFactory.decodeResource(TourPalApplication.getAppContext().getResources(), R.mipmap.icon_share_wechat), tagetUrl, true);
                        break;
                    case QQ:
                        shareToQQ(activity, (title == null ? "" : title), summary, tagetUrl, iconUrl);
                        break;
                    case QZONE:
                        shareToQQZone(activity, (title == null ? "" : title), summary, tagetUrl,  iconUrl);
                        break;
                    case SINA_WEIBO:
                        String s1 = summary;
                        s1 += "[" + tagetUrl + "]" + "(来自@艺龙驴友）";
                        shareToWeibo(activity, getSinaWeiboResolveInfo(shareApps), s1);
                        break;
                }
            }
        });

        shareDialog.show();
    }

    /**
     * 分享url到微信
     *
     * @param activity   界面context
     * @param title      分享url的标题
     * @param icon       分享url的小图标
     * @param url        分享的url
     * @param isTileline 是否是朋友圈分享
     */
    public static void shareToWeChat(Context activity, String title, String summary, Bitmap icon, String url, boolean isTileline) {
        if (!isWeChatInstalled()) {
            Toast.makeText(activity, R.string.share_wechat_no_installed, Toast.LENGTH_LONG).show();
            return;
        } else if (isTileline && !isWeChatTimelineSupport()) {
            Toast.makeText(activity, R.string.share_wechat_tileline_no_support, Toast.LENGTH_LONG).show();
            return;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        if (url != null) {
            webpage.webpageUrl = url;
        }
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        Bitmap thumb = icon;
        msg.thumbData = Utils.bmpToByteArray(thumb, true);
        msg.description = summary;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = isTileline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        if (req.scene == SendMessageToWX.Req.WXSceneTimeline) {
            msg.title = summary;
        }

        TourPalApplication.getInstance().getWXapi().sendReq(req);
    }

    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    /**
     * 判断是否安装微信
     *
     * @return
     */
    public static boolean isWeChatTimelineSupport() {
        int wxSdkVersion = TourPalApplication.getInstance().getWXapi().getWXAppSupportAPI();
        if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否支持朋友圈分享
     *
     * @return
     */
    public static boolean isWeChatInstalled() {
        return TourPalApplication.getInstance().getWXapi().isWXAppInstalled();
    }

    /**
     * QQ分享相关 *
     */
    public static void shareToQQ(Context activity, String title, String summary, String tagetUrl, String iconUrl) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        if (tagetUrl != null) {
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, tagetUrl);
        }
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, iconUrl);
        TourPalApplication.getInstance().getQQapi().shareToQQ((Activity) activity, params, new BaseUiListener());
    }

    /**
     * QQ空间分享相关 *
     */
    public static void shareToQQZone(Context activity, String title, String summary, String tagetUrl, String iconUrl) {
        if (DEBUG) {
            Log.d(TAG, "isinstall:" + TourPalApplication.getInstance().getQQapi().getAppId());
        }
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);
        if (tagetUrl != null) {
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, tagetUrl);
        }
        // 必填，官方文档坑爹呀！说这个是选填，但是不填一点反应都没有，连QQ都起不来也没有错误信息
        if (DEBUG) {
            Log.d(TAG, "qzone img=" + iconUrl);
        }
        ArrayList<String> ims = new ArrayList<String>();
        ims.add(iconUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, ims);
        params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, iconUrl);
        TourPalApplication.getInstance().getQQapi().shareToQzone((Activity) activity, params, new BaseUiListener());
    }

    public static void shareToWeibo(Context activity, ResolveInfo resolveInfo, String content) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
        shareIntent.setType("text/plain");
//                  shareIntent.setType("*/*");
        //这里就是组织内容了，
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(shareIntent);
    }

    private static class BaseUiListener implements IUiListener {
        @Override
        public void onComplete(Object response) {
            if (DEBUG) {
                Log.d(TAG, "onComplete, response=" + response.toString());
            }
            doComplete(response);
        }

        @Override
        public void onError(UiError e) {
            if (DEBUG) {
                Log.d(TAG, "onError,code:" + e.errorCode + ", msg:"
                        + e.errorMessage + ", detail:" + e.errorDetail);
            }
        }

        @Override
        public void onCancel() {
            if (DEBUG) {
                Log.d(TAG, "onCancel");
            }
        }

        protected void doComplete(Object values) {
            JSONObject o = (JSONObject) values;
        }
    }

    private static boolean isSinaWeiboInstalled(List<ResolveInfo> sharedApps) {
        for (ResolveInfo r : sharedApps) {
            if (SINA_WEIBO_PACKAGENAME.equals(r.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private static ResolveInfo getSinaWeiboResolveInfo(List<ResolveInfo> sharedApps) {
        for (ResolveInfo r : sharedApps) {
            if (SINA_WEIBO_PACKAGENAME.equals(r.activityInfo.packageName)) {
                return r;
            }
        }
        return null;
    }

    private static boolean isQQInstalled(List<ResolveInfo> sharedApps) {
        for (ResolveInfo r : sharedApps) {
            if (QQ_PACKAGENAME.equals(r.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    public static class ShareAdapterData {
        public ShareAdapterData(String name, Drawable drawable, ShareType type) {
            mName = name;
            mDrawble = drawable;
            mType = type;
        }

        public String mName;
        public Drawable mDrawble;
        public ShareType mType;
    }

    public enum ShareType {
        WECHAT, WECHAT_TIMELINE, QQ, QZONE, SINA_WEIBO
    }

    private static String getShareImagePath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tourpal/share_icon.png";
        File f = new File(path);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                Bitmap b = ((BitmapDrawable) TourPalApplication.getAppContext().getResources().getDrawable(R.mipmap.ic_tourpal)).getBitmap();
                b.compress(Bitmap.CompressFormat.PNG, 100, fos);

                if (!f.exists()) {
                    path = "";
                }
            } catch (FileNotFoundException e) {
                if (DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "e:", e);
                    }
                }
            }

        }
        return path;
    }
}
