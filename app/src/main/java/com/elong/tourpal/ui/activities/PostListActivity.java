package com.elong.tourpal.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.supports.PostListAdapter;
import com.elong.tourpal.ui.views.CommonToastDialog;
import com.elong.tourpal.ui.views.EmptyView;
import com.elong.tourpal.utils.Utils;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;

import java.lang.ref.WeakReference;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class PostListActivity extends ActivityBase {
    private static final String TAG = PostListActivity.class.getSimpleName();
    public static final int TYPE_POSTS_SELECT_BY_DEST = 1;
    public static final int TYPE_POSTS_SELECT_BY_USER = 2;
    public static final int TYPE_POSTS_SELECT_BY_USER_JOINED = 3;
//    public static final int TYPE_POSTS_SELECT_BY_SEARCH = 3;

    protected int mType = TYPE_POSTS_SELECT_BY_DEST;

    private static final int WHAT_GET_POSTS_LIST = 101;
    private static final int WHAT_SEND_WANNA_JOIN = 102;
    private static final int WHAT_DELETE_POST = 103;
    private static final int WHAT_REFRESH_POSTS_LIST = 201;
    private static final int WHAT_SHOW_DELETE_POST_RESULT = 202;

    private static final int REQ_CODE_START_POST_DETAIL = 999;

    /**
     * 每次请求的帖子个数
     */
    private static final int POST_PER_REQUEST = 200;

    public String mDestination = null;
    public MessageProtos.UserInfo mOtherUserInfo = null;//他人的用户信息

    private ListView mLvPostList = null;
    public PostListAdapter mPostListAdapter = null;
    public int mCurrentOffset = 0;
    private PtrClassicFrameLayout mPtrFrame;
    private Button mBtnFooterLoadNext;
    private LinearLayout mLlFooterContent;
    private EmptyView mEmptyView;
    private CommonToastDialog mProgressDialog = null;

    public boolean mCanFetchNextPage = true;

    private HandlerThread mHandlerThread;
    private ExecHandler mExecHandler;
    private UIHandler mUIHandler;

    private PostListReceiver mPostListReceiver = null;

    /**
     * onItemClick的时候记录点击的item，便于从详情页操作返回后刷新该item状态
     */
    private int mLastClickItemPosition = 0;

    /**
     * 根据地点来选择帖子
     *
     * @param c    context
     * @param dest 地点
     */
    public static void startActivityByDest(Context c, String dest) {
        Intent intent = new Intent(c, PostListActivity.class);
        intent.putExtra(Extras.EXTRA_POST_LIST_TYPE, TYPE_POSTS_SELECT_BY_DEST);
        if (!TextUtils.isEmpty(dest)) {
            intent.putExtra(Extras.EXTRA_LOCATION_FOR_POSTS, dest);
        }
        c.startActivity(intent);
    }

    /**
     * 根据用户id来选择用户发的帖子，若为空，则选择本设备已登录的用户的帖子
     *
     * @param c           context
     * @param userInfoStr 用户id
     */
    public static void startActivityByUser(Context c, String userInfoStr) {
        Intent intent = new Intent(c, PostListActivity.class);
        intent.putExtra(Extras.EXTRA_POST_LIST_TYPE, TYPE_POSTS_SELECT_BY_USER);
        if (!TextUtils.isEmpty(userInfoStr)) {
            intent.putExtra(Extras.EXTRA_USER_INFO, userInfoStr);
        }
        c.startActivity(intent);
    }

    /**
     * 根据用户id来选择用户点赞的帖子，若为空，则选择本设备已登录的用户点赞的帖子
     * @param c context
     * @param userInfoStr 用户id
     */
    public static void startActivityForUserJoinedPosts(Context c, String userInfoStr) {
        Intent intent = new Intent(c, PostListActivity.class);
        intent.putExtra(Extras.EXTRA_POST_LIST_TYPE, TYPE_POSTS_SELECT_BY_USER_JOINED);
        if (!TextUtils.isEmpty(userInfoStr)) {
            intent.putExtra(Extras.EXTRA_USER_INFO, userInfoStr);
        }
        c.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //打点数据
        Statistics.log(getApplicationContext(), StatisticsEnv.DESTINATION_ENTER);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_list);
        initHandlerAndThread();
        resolveIntent();
        initViews();
        initReceivers();
//        getPostListAsync(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent();
        initTitle();
        manualRequestData();
    }

    @Override
    protected void onDestroy() {
        if (mPostListReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mPostListReceiver);
        }
        super.onDestroy();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        mType = intent.getIntExtra(Extras.EXTRA_POST_LIST_TYPE, TYPE_POSTS_SELECT_BY_DEST);
        switch (mType) {
            case TYPE_POSTS_SELECT_BY_DEST:
                mDestination = intent.getStringExtra(Extras.EXTRA_LOCATION_FOR_POSTS);
                break;
            case TYPE_POSTS_SELECT_BY_USER:
            case TYPE_POSTS_SELECT_BY_USER_JOINED:
                String uInfoStr = intent.getStringExtra(Extras.EXTRA_USER_INFO);
                if (uInfoStr != null) {
                    try {
                        mOtherUserInfo = MessageProtos.UserInfo.parseFrom(uInfoStr.getBytes());
                    } catch (InvalidProtocolBufferMicroException e) {
                        if (Env.DEBUG) {
                            Log.e(TAG, "e:", e);
                        }
                    }
                }
                break;
        }
    }

    private void initViews() {
        mPostListAdapter = new PostListAdapter(this);
        initTitle();

        mProgressDialog = new CommonToastDialog(this);
        //初始化list
        mLvPostList = (ListView) findViewById(R.id.pl_lv_pages);
        mLvPostList.addFooterView(getFooterView());
        mPostListAdapter.setOnWannaJoinListener(new PostListAdapter.OnWannaJoinListener() {
            @Override
            public void onWannaJoin(String postId, int state) {
                //求同行的点击事件
                sendWannaJoin(postId, state);
            }

            @Override
            public void onPostDelete(String postId) {
                deletePostByPostId(postId);
            }
        });
        mLvPostList.setAdapter(mPostListAdapter);
        mLvPostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLastClickItemPosition = position;
                PostDetailActivity.startActivityForResult(PostListActivity.this, mPostListAdapter.getItem(position), REQ_CODE_START_POST_DETAIL);
            }
        });
        mEmptyView = (EmptyView) findViewById(R.id.pl_empty_view);
        mEmptyView.setText(R.string.loading);
        mLvPostList.setEmptyView(mEmptyView);

        //初始化下拉刷新frame
        initPtrFrame();
    }

    private void initTitle() {
        //初始化title
        if (mType == TYPE_POSTS_SELECT_BY_DEST) {
            setTitleText(mDestination != null ? mDestination : "");
            setTitleRightBtn(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //打点数据
                    Statistics.log(getApplicationContext(), StatisticsEnv.DESTINATION_POST);

                    Intent intent = new Intent(PostListActivity.this, PostingTourActivity.class);
                    startActivity(intent);
                }
            });
        } else if (mType == TYPE_POSTS_SELECT_BY_USER) {
            mPostListAdapter.setAvatarClickEnable(false);
            setTitleTextGravity(Gravity.CENTER);
            if (mOtherUserInfo == null) {
                setTitleText(R.string.pl_title_my_posts);
            } else {
                setTitleText(getString(R.string.pl_title_other_posts_format, mOtherUserInfo.getNickName()));
            }
            setTitleRightBtnEnable(false);
        } else if (mType == TYPE_POSTS_SELECT_BY_USER_JOINED) {
            mPostListAdapter.setAvatarClickEnable(false);
            setTitleTextGravity(Gravity.CENTER);
            if (mOtherUserInfo == null) {
                setTitleText(R.string.pl_title_my_joined_posts);
            } else {
                setTitleText(getString(R.string.pl_title_others_joined_posts_format, mOtherUserInfo.getNickName()));
            }
            setTitleRightBtnEnable(false);
        }
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initReceivers() {
        mPostListReceiver = new PostListReceiver();
        IntentFilter intentFilter = new IntentFilter(Actions.ACTION_LOGIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPostListReceiver, intentFilter);
    }

    private void initPtrFrame() {
        initToRequestData();
    }

    protected void configPtrFrame() {
        mPtrFrame = (PtrClassicFrameLayout) findViewById(R.id.pl_ptr_frame);
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                getPostListAsync(true);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return Utils.canListPullDown(mLvPostList);
            }
        });
        // the following are default settings
        mPtrFrame.setResistance(1.7f);
        mPtrFrame.setRatioOfHeaderHeightToRefresh(1.2f);
        mPtrFrame.setDurationToClose(200);
        mPtrFrame.setDurationToCloseHeader(1000);
        // default is false
        mPtrFrame.setPullToRefresh(false);
        // default is true
        mPtrFrame.setKeepHeaderWhenRefresh(true);
    }

    protected void initToRequestData() {
        configPtrFrame();
        manualRequestData();
    }

    private View getFooterView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View footerView = inflater.inflate(R.layout.refresh_footer_view, null, false);
        mLlFooterContent = (LinearLayout) footerView.findViewById(R.id.rfv_ll_content);
        mBtnFooterLoadNext = (Button) footerView.findViewById(R.id.rfv_btn_load);
        mBtnFooterLoadNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打点数据
                Statistics.log(getApplicationContext(), StatisticsEnv.DESTINATION_NEXT_PAGE);

                //加载下一页
                getPostListAsync(false);
            }
        });
        mLlFooterContent.setVisibility(View.GONE);
        return footerView;
    }

    private void initHandlerAndThread() {
        mHandlerThread = new HandlerThread("plaThread");
        mHandlerThread.start();
        mExecHandler = new ExecHandler(this, mHandlerThread.getLooper());
        mUIHandler = new UIHandler();
    }

    public static class ExecHandler extends Handler {

        WeakReference<PostListActivity> mContext;

        public ExecHandler(PostListActivity context, Looper looper) {
            super(looper);
            mContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            final PostListActivity context = mContext.get();
            if (context != null) {
                Bundle data = msg.getData();
                switch (msg.what) {
                    case WHAT_GET_POSTS_LIST:
                        if (!Utils.isNetworkConnected(context)) {
                            Toast.makeText(context, R.string.network_disable, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        //联网请求帖子数据
                        int numPerPage = data.getInt("numPerPage");
                        final int startOffset = data.getInt("startOffset");
                        Request getPostReq = null;
                        if (context.mType == TYPE_POSTS_SELECT_BY_DEST) {
                            getPostReq = RequestBuilder.buildGetPostByDestRequest(startOffset, numPerPage, context.mDestination);
                        } else if (context.mType == TYPE_POSTS_SELECT_BY_USER) {
                            String uid = context.mOtherUserInfo == null ? null : context.mOtherUserInfo.getId();
                            getPostReq = RequestBuilder.buildGetPostByUserRequest(startOffset, numPerPage, uid);
                        } else if (context.mType == TYPE_POSTS_SELECT_BY_USER_JOINED) {
                            String uid = context.mOtherUserInfo == null ? null : context.mOtherUserInfo.getId();
                            getPostReq = RequestBuilder.buildGetJoinedPostByUserRequest(startOffset, numPerPage, uid);
                        }
                        if (getPostReq != null) {
                            MessageProtos.ResponseInfo respInfo = getPostReq.get();
                            if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                final MessageProtos.PostResponseInfoList pril = respInfo.getPostInfoList();
                                if (pril != null) {
                                    context.mUIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            MessageProtos.PageResponseInfo pri = pril.getPageResponseInfo();
                                            context.mCanFetchNextPage = pri.getHasRest();
                                            if (startOffset <= 0) {
                                                context.mPostListAdapter.setDatas(pril.getPostInfosList());
                                            } else {
                                                context.mPostListAdapter.addDatas(pril.getPostInfosList());
                                            }
                                            //记录上次结束的偏移
                                            context.mCurrentOffset = pri.getEndOffset();
                                            if (!context.mUIHandler.hasMessages(WHAT_REFRESH_POSTS_LIST)) {
                                                context.mUIHandler.sendEmptyMessage(WHAT_REFRESH_POSTS_LIST);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        context.mUIHandler.sendEmptyMessage(WHAT_REFRESH_POSTS_LIST);
                        break;
                    case WHAT_SEND_WANNA_JOIN:
                        int reqState = data.getInt("state");
                        final String postId = data.getString("postId");
                        if (!Utils.isNetworkConnected(context)) {
                            Toast.makeText(context, R.string.network_disable, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        if (!TextUtils.isEmpty(postId)) {
                            Request sendJoinReq = RequestBuilder.buildSendWannaJoinRequest(postId, reqState);
                            if (sendJoinReq != null) {
                                MessageProtos.ResponseInfo respInfo = sendJoinReq.get();
                                if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                    final MessageProtos.WannaJoinResponse wjr = respInfo.getWannaJoinResponse();
                                    if (wjr != null) {
                                        if (!hasMessages(WHAT_SEND_WANNA_JOIN)) {
                                            context.mUIHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //当点击了多次求同行和取消求同行，这个时候只在最后一个求同行请求返回后才置界面的状态，否则按钮状态会乱跳
                                                    int num = wjr.getWannaJoinNum();
                                                    int state = wjr.getWannaJoinState();
                                                    MessageProtos.PostResponseInfo pri = context.mPostListAdapter.getItemByPostId(postId);
                                                    if (pri != null) {
                                                        pri.setLikeNum(num);
                                                        pri.setIsLiked(state == 1);//1为已点赞
                                                        context.mUIHandler.sendEmptyMessage(WHAT_REFRESH_POSTS_LIST);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case WHAT_DELETE_POST:
                        String delPostId = data.getString("postId");
                        Message showResultMsg = context.mUIHandler.obtainMessage(WHAT_SHOW_DELETE_POST_RESULT);
                        showResultMsg.arg1 = 1;//默认失败
                        if (!Utils.isNetworkConnected(context)) {
                            Toast.makeText(context, R.string.network_disable, Toast.LENGTH_SHORT).show();
                        } else {
                            if (!TextUtils.isEmpty(delPostId)) {
                                Request request = RequestBuilder.buildDeletePostRequest(delPostId);
                                if (request != null) {
                                    MessageProtos.ResponseInfo respInfo = request.get();
                                    if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                        showResultMsg.arg1 = 0;
                                    }
                                    showResultMsg.setData(data);
                                }
                            }
                        }
                        context.mUIHandler.sendMessage(showResultMsg);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what) {
                case WHAT_REFRESH_POSTS_LIST:
                    mPtrFrame.refreshComplete();
                    if (mPostListAdapter.getCount() > 0 && mCanFetchNextPage) {
                        mLlFooterContent.setVisibility(View.VISIBLE);
                    } else {
                        mLlFooterContent.setVisibility(View.GONE);
                        refreshEmptyView();
                    }
                    mPostListAdapter.notifyDataSetChanged();
                    break;
                case WHAT_SHOW_DELETE_POST_RESULT:
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        if (Env.DEBUG) {
                            Log.e(TAG, "e:", e);
                        }
                    }
                    boolean deleteSuccess = msg.arg1 == 0;
                    String deletePostId = data.getString("postId");
                    if (deleteSuccess) {
                        Toast.makeText(PostListActivity.this, R.string.pl_delete_succeed, Toast.LENGTH_SHORT).show();
                        mPostListAdapter.removeData(deletePostId);
                        mPostListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(PostListActivity.this, R.string.pl_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                    mProgressDialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 异步获取
     *
     * @param refreshFromFirst 是否从头刷新
     */
    private void getPostListAsync(boolean refreshFromFirst) {
        Message msg = mExecHandler.obtainMessage(WHAT_GET_POSTS_LIST);
        Bundle data = new Bundle();
        data.putInt("numPerPage", POST_PER_REQUEST);
        if (refreshFromFirst) {
            mCurrentOffset = -1;
        }
        data.putInt("startOffset", mCurrentOffset);
        msg.setData(data);
        mExecHandler.sendMessage(msg);
    }

    private void sendWannaJoin(String postId, int state) {
        Message msg = mExecHandler.obtainMessage(WHAT_SEND_WANNA_JOIN);
        Bundle data = new Bundle();
        data.putString("postId", postId);
        data.putInt("state", state);
        msg.setData(data);
        mExecHandler.sendMessage(msg);
    }

    private void deletePostByPostId(String postId) {
        mProgressDialog.setDialogTitle(getString(R.string.pl_toast_deleting));
        mProgressDialog.show();
        Message msg = mExecHandler.obtainMessage(WHAT_DELETE_POST);
        Bundle data = new Bundle();
        data.putString("postId", postId);
        msg.setData(data);
        mExecHandler.sendMessage(msg);
    }

    protected void manualRequestData() {
        mPtrFrame.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPtrFrame.autoRefresh();
            }
        }, 100);
    }

    private void refreshEmptyView() {
        if (mType == TYPE_POSTS_SELECT_BY_USER && mOtherUserInfo == null) {
            mEmptyView.setText(R.string.pl_empty_view_text_my);
            mEmptyView.setIcon(R.mipmap.ic_empty_post_location);
        } else {
            mEmptyView.setText(R.string.pl_empty_view_text);
            mEmptyView.setIcon(R.mipmap.ic_empty_post_my);
        }
    }

    class PostListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.ACTION_LOGIN)) {
                getPostListAsync(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_START_POST_DETAIL) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    boolean isDelete = data.getBooleanExtra(Extras.EXTRA_IS_POST_DELETE, false);
                    if (isDelete) {
                        if (mPostListAdapter != null && mPostListAdapter.getCount() > mLastClickItemPosition) {
                            List<MessageProtos.PostResponseInfo> listDatas = mPostListAdapter.getDatas();
                            if (listDatas != null) {
                                listDatas.remove(mLastClickItemPosition);
                                mPostListAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        byte[] postInfoBytes = data.getByteArrayExtra(Extras.EXTRA_POST_INFO);
                        MessageProtos.PostResponseInfo newPostInfo = null;
                        try {
                            newPostInfo = MessageProtos.PostResponseInfo.parseFrom(postInfoBytes);
                        } catch (InvalidProtocolBufferMicroException e) {
                            if (Env.DEBUG) {
                                Log.e(TAG, "e:", e);
                            }
                        }
                        if (mPostListAdapter != null && mPostListAdapter.getCount() > mLastClickItemPosition) {
                            MessageProtos.PostResponseInfo oldPostInfo = mPostListAdapter.getItem(mLastClickItemPosition);
                            if (newPostInfo != null && oldPostInfo != null) {
                                String newId = oldPostInfo.getId();
                                if (newId != null && newId.equals(oldPostInfo.getId())) {
                                    //若id一样，则将新的postInfo的某些信息覆盖旧的postInfo
                                    oldPostInfo.setIsLiked(newPostInfo.getIsLiked());
                                    oldPostInfo.setLikeNum(newPostInfo.getLikeNum());
                                    oldPostInfo.setStatus(newPostInfo.getStatus());
                                    oldPostInfo.setPv(newPostInfo.getPv());
                                    mPostListAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
