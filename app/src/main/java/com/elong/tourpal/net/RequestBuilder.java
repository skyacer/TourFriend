package com.elong.tourpal.net;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.SystemUtils;
import com.google.protobuf.micro.ByteStringMicro;

import java.util.ArrayList;

/**
 * RequestBuilder
 *
 * @author tao.chen1
 */
public class RequestBuilder {
    private static final String TAG = RequestBuilder.class.getSimpleName();
    private static final String DEBUG_HOST = "http://192.168.199.163/tourpal";
    private static final String RELEASE_HOST = "http://192.168.199.163/tourpal";

    public static boolean isDebugEnv = Env.DEBUG;

    private static String getHostName() {
        return isDebugEnv ? DEBUG_HOST : RELEASE_HOST;
    }

    /**
     * 创建一个基础的requestInfo
     *
     * @return RequestInfo
     */
    private static MessageProtos.RequestInfo buildBaseRequestInfo() {
        MessageProtos.RequestInfo reqInfo = new MessageProtos.RequestInfo();

        MessageProtos.ClientInfo clientInfo = new MessageProtos.ClientInfo();
        clientInfo.setClientVersion(Env.getVersionName(TourPalApplication.getAppContext()));
        clientInfo.setChannelId(Env.getChannelId(TourPalApplication.getAppContext()));
        clientInfo.setBuildBrand(Build.BRAND);
        clientInfo.setBuildModel(Build.MODEL);
        clientInfo.setVersionCode(Env.getVersionCode(TourPalApplication.getAppContext()));
        clientInfo.setMid(SystemUtils.getMid(TourPalApplication.getAppContext()));
        reqInfo.setClientInfo(clientInfo);

        SharedPref pref = SharedPref.getInstance();
        String sessionId = pref.getSessionId();
        String sessionToken = pref.getSessionToken();
        MessageProtos.LoginInfo loginInfo = new MessageProtos.LoginInfo();
        loginInfo.setSessionId(sessionId == null ? "" : sessionId);
        loginInfo.setSessionToken(sessionToken == null ? "" : sessionToken);
        reqInfo.setLoginInfo(loginInfo);

        return reqInfo;
    }

    /**
     * 获取热门目的地的请求
     *
     * @return Request
     */
    public static Request buildGetHotDestRequest() {
        Request request = new Request();
        request.setRequestInfo(buildBaseRequestInfo());
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Dest/getHotCity");
        return request;
    }

    /**
     * 根据目的地获取帖子的请求
     *
     * @param numPerPage  每页记录条数
     * @param destination 目的地
     * @return Request
     */
    public static Request buildGetPostByDestRequest(int startOffset, int numPerPage, String destination) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();

        MessageProtos.PostByDestRequestInfo pbdr = new MessageProtos.PostByDestRequestInfo();
        MessageProtos.PageRequestInfo pri = new MessageProtos.PageRequestInfo();
        pri.setStartOffset(startOffset);
        pri.setNumPerPage(numPerPage);
        pbdr.setPageRequestInfo(pri);
        pbdr.setDest(destination);
        requestInfo.setPostByDestInfo(pbdr);

        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Dest/getPostsByPlace");

        return request;
    }

    /**
     * 根据用户id获取已发帖子的请求，若用户id为空则获取本账号的
     *
     * @param numPerPage 每页记录条数
     * @param uid        用户id
     * @return Request
     */
    public static Request buildGetPostByUserRequest(int startOffset, int numPerPage, String uid) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();

        MessageProtos.PostByUserRequestInfo pbur = new MessageProtos.PostByUserRequestInfo();
        MessageProtos.PageRequestInfo pri = new MessageProtos.PageRequestInfo();
        pri.setStartOffset(startOffset);
        pri.setNumPerPage(numPerPage);
        pbur.setPageRequestInfo(pri);
        if (uid != null) {
            pbur.setUid(uid);
        }
        requestInfo.setPostByUserInfo(pbur);

        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Posts/myPosts");

        return request;
    }

    /**
     * 根据用户id获取已求同行的帖子的请求，若用户id为空则获取本账号的
     * @param startOffset 其实位置
     * @param numPerPage 每页条数
     * @param uid 用户id
     * @return Request
     */
    public static Request buildGetJoinedPostByUserRequest(int startOffset, int numPerPage, String uid) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();

        MessageProtos.PostByUserRequestInfo pbur = new MessageProtos.PostByUserRequestInfo();
        MessageProtos.PageRequestInfo pri = new MessageProtos.PageRequestInfo();
        pri.setStartOffset(startOffset);
        pri.setNumPerPage(numPerPage);
        pbur.setPageRequestInfo(pri);
        if (uid != null) {
            pbur.setUid(uid);
        }
        requestInfo.setPostByUserInfo(pbur);

        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Liked/myLiked");

        return request;
    }

    /**
     * 获取发贴请求
     *
     * @param pri 发帖请求的内容
     * @return Request
     */
    public static Request buildSendPostRequest(MessageProtos.PostRequestInfo pri) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        requestInfo.setPostReqInfo(pri);
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Posts/post");
        return request;
    }

    /**
     * 获取发送求同行的请求
     *
     * @param postId 帖子的id
     * @return Request
     */
    public static Request buildSendWannaJoinRequest(String postId, int state) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();

        MessageProtos.WannaJoinRequest wjr = new MessageProtos.WannaJoinRequest();
        wjr.setPostId(postId);
        wjr.setState(state);
        requestInfo.setWannaJoinRequest(wjr);

        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Liked/like");
        return request;
    }

    /**
     * a:获取某个帖子求同行的人的请求/b:获取对我点求同行的用户的信息
     *
     * @param startOffset 起始位置
     * @param numPerPage  每页记录条数
     * @param postId      帖子id：不为空则是a，否则为b
     * @return Request
     */
    private static Request buildGetWannaJoinUserInfoRequest(int startOffset, int numPerPage, String postId) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();

        MessageProtos.UsersWannaJoinRequestInfo uwjr = new MessageProtos.UsersWannaJoinRequestInfo();
        MessageProtos.PageRequestInfo pri = new MessageProtos.PageRequestInfo();
        pri.setNumPerPage(numPerPage);
        pri.setStartOffset(startOffset);
        uwjr.setPageRequestInfo(pri);
        if (postId != null) {
            uwjr.setPostId(postId);
        }
        requestInfo.setUsersWannaJoinRequestInfo(uwjr);

        request.setRequestInfo(requestInfo);
        if (postId == null) {
            request.setRequestUrl(getHostName() + "/index.php/Sapi/Liked/getNewsList");
        } else {
            request.setRequestUrl(getHostName() + "/index.php/Sapi/Liked/getLikedInfoByPostIdV2");
        }

        return request;
    }

    /**
     * 获取对我点求同行的用户的信息
     *
     * @param startOffset 起始位置
     * @param numPerPage  每页记录条数
     * @return Request
     */
    public static Request buildGetWannaJoinMineRequest(int startOffset, int numPerPage) {
        return buildGetWannaJoinUserInfoRequest(startOffset, numPerPage, null);
    }

    /**
     * 获取某个帖子求同行的人的请求
     *
     * @param startOffset 起始位置
     * @param numPerPage  每页记录条数
     * @param postId      帖子id（不能为null）
     * @return Request
     */
    public static Request buildGetWannaJoinListByPostIdRequest(int startOffset, int numPerPage, String postId) {
        return buildGetWannaJoinUserInfoRequest(startOffset, numPerPage, postId);
    }

    /**
     * 获取我的资料的请求
     *
     * @return Request
     */
    public static Request buildGetMyUserInfoRequest() {
        return buildGetUserInfoRequest(null);
    }

    /**
     * 获取别的用户的资料的请求
     *
     * @param uid 用户id
     * @return Request
     */
    public static Request buildGetOtherUserInfoRequest(String uid) {
        return buildGetUserInfoRequest(uid);
    }

    /**
     * 获取用户资料的请求
     *
     * @param uid 用户id
     * @return Request
     */
    private static Request buildGetUserInfoRequest(String uid) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        if (uid != null) {
            requestInfo.setUserId(uid);
        }
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/User/getUserInfoById");
        return request;
    }

    /**
     * 获取修改我的资料的请求
     *
     * @param userInfo 我的资料
     * @return Request
     */
    public static Request buildModifyMyUserInfoRequest(MessageProtos.UserInfo userInfo) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        requestInfo.setUserInfo(userInfo);
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/User/userSave");
        return request;
    }

    /**
     * 获取更新信息的请求
     *
     * @return Request
     */
    public static Request buildGetUpdateInfoRequest() {
        Request request = new Request();
        request.setRequestInfo(buildBaseRequestInfo());
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Upgrade/check");
        return request;
    }

    /**
     * 获取设置已读状态的请求
     *
     * @param ids 消息ids
     * @return
     */
    public static Request buildSetMessageReadStateRequest(ArrayList<String> ids) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        try {
            for (String id : ids) {
                requestInfo.addSetReadId(Integer.parseInt(id));
            }
        } catch (Exception e) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Liked/setRead");
        return request;
    }

    /**
     * 获取设置已读状态的请求
     *
     * @param id 消息id
     * @return
     */
    public static Request buildSetMessageReadStateRequest(String id) {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        return buildSetMessageReadStateRequest(ids);
    }

    /**
     * 获取删除已发帖子的请求
     *
     * @param postId 帖子id
     * @return Request
     */
    public static Request buildDeletePostRequest(String postId) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        requestInfo.setPostId(postId);
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Posts/delPostById");
        return request;
    }

    /**
     * 获取绑定clientId的请求
     *
     * @return Request
     */
    public static Request buildBindPushClientIdRequest() {
        String pushClientId = SharedPref.getInstance().getPushClientId();
        if (!TextUtils.isEmpty(pushClientId)) {
            Request request = new Request();
            MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
            requestInfo.getLoginInfo().setPushClientId(pushClientId);
            request.setRequestInfo(requestInfo);
            request.setRequestUrl(getHostName() + "/index.php/Sapi/PushMap/bind");
            return request;
        }
        return null;
    }

    /**
     * 获取解绑clientId的请求
     *
     * @return Request
     */
    public static Request buildUnbindPushClientIdRequest(String sessionId, String sessionToken) {
        String pushClientId = SharedPref.getInstance().getPushClientId();
        if (!TextUtils.isEmpty(pushClientId)) {
            Request request = new Request();
            MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
            MessageProtos.LoginInfo loginInfo = requestInfo.getLoginInfo();
            loginInfo.setPushClientId(pushClientId);
            if (!TextUtils.isEmpty(sessionId)) {
                loginInfo.setSessionId(sessionId);
            }
            if (!TextUtils.isEmpty(sessionToken)) {
                loginInfo.setSessionToken(sessionToken);
            }
            request.setRequestInfo(requestInfo);
            request.setRequestUrl(getHostName() + "/index.php/Sapi/PushMap/unbind");
            return request;
        }
        return null;
    }

    /**
     * 上传打点数据
     *
     * @param statisticsData 打点数据
     * @return request
     */
    public static Request buildUploadStatisticsRequest(String statisticsData) {
        if (Env.DEBUG) {
            Log.d(TAG, "statisticsData=" + statisticsData);
        }
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        requestInfo.setStatisticsData(statisticsData);
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Stat/dot");
        return request;
    }

    /**
     * 获取上传反馈的请求
     *
     * @param content 反馈信息
     * @param qq      qq
     * @param email   邮箱
     * @param phone   电话
     * @return Request
     */
    public static Request buildFeedBackRequest(String content, String qq, String email, String phone) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        MessageProtos.Feedback feedback = new MessageProtos.Feedback();
        feedback.setBugs(content);
        if (qq != null) {
            feedback.setQq(qq);
        }
        if (email != null) {
            feedback.setEmail(email);
        }
        if (phone != null) {
            feedback.setPhone(phone);
        }
        requestInfo.setFeedback(feedback);
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Feedback/feedBack");
        return request;
    }

    /**
     * 请求数据更新
     *
     * @param fileNames 待更新的文件名
     * @return request
     */
    public static Request buildDataUpdateRequest(String[] fileNames) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        MessageProtos.DataUpdateRequestInfo duri = new MessageProtos.DataUpdateRequestInfo();

        for (String fn : fileNames) {
            duri.addDataFileName(fn);
        }
        requestInfo.setDataUpdateRequestInfo(duri);
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Upgrade/updateDb");
        return request;
    }

    /**
     * 获取帖子详情请求
     * @param postId 帖子id
     * @return Request
     */
    public static Request buildGetPostDetailRequest(String postId) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        requestInfo.setPostId(postId);
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Posts/postDetail");
        return request;
    }

    /**
     * 上传头像请求
     * @param avatar
     * @return Request
     */
    public static Request buildUploadAvatarRequest(byte[] avatar) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        requestInfo.setImg(ByteStringMicro.copyFrom(avatar));
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/User/uploadHeadImg");
        return request;
    }

    /**
     * 举报帖子请求
     * @param postId 帖子id
     * @return Request
     */
    public static Request buildReportPostRequest(String postId) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        requestInfo.setPostId(postId);
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Report/addReport");
        return request;
    }

    /**
     * 关闭帖子请求
     * @param postId 帖子id
     * @return Request
     */
    public static Request buildClosePostRequest(String postId) {
        Request request = new Request();
        MessageProtos.RequestInfo requestInfo = buildBaseRequestInfo();
        requestInfo.setPostId(postId);
        request.setRequestInfo(requestInfo);
        request.setRequestUrl(getHostName() + "/index.php/Sapi/Posts/closePostById");
        return request;
    }

}
