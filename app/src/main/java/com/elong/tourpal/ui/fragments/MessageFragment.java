package com.elong.tourpal.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.activities.MainTabsActivity;
import com.elong.tourpal.ui.activities.WannaJoinListActivity;
import com.elong.tourpal.ui.supports.MessageAdapter;
import com.elong.tourpal.ui.views.EmptyView;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * MessageFragment
 * <p/>
 * 消息页面
 */
public class MessageFragment extends FragmentBase4Pager {

    private static final String TAG = "MessageFragment";

    private static final String ARG_PAGER_INDEX = "PAGER_INDEX";
    private static final int MESSAGE_COUNT_PER_REQUEST = 100;
    private static final int WHAT_GET_MESSAGES = 101;
    private static final int WHAT_SET_MESSAGES_READ_STATE = 102;
    private static final int WHAT_SET_ALL_MESSAGES_READ_STATE = 103;
    private static final int WHAT_REFRESH_MESSAGE_LIST = 201;

    private static boolean sHasFirstCheckNewMsg = false;

    private ListView mLvMessages;
    private PtrClassicFrameLayout mPtrFrame;
    private LinearLayout mLlFooterContent;
    private Button mBtnFooterLoadNext;
    private EmptyView mEmptyView;

    private MessageAdapter mMessageAdapter;
    private HandlerThread mHandlerThread;
    private ExecHandler mExecHandler;

    private UIHandler mUIHandler;
    public boolean mCanFetchNextPage = true;
    public int mCurrentOffset = 0;

    private MessageReceiver mMessageReceiver = null;

    public static MessageFragment newInstance(int pagerIndex, HandlerThread thread) {
        MessageFragment fragment = new MessageFragment();
        fragment.setHandlerThread(thread);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGER_INDEX, pagerIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //打点数据
        Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.COMMUNICATION_MESSAGE_ENTER);

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            setPageIndex(getArguments().getInt(ARG_PAGER_INDEX, 0));
        }
        initHandler();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateEmtpyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);
        initViews(rootView);
        initReceivers();
        return rootView;
    }

    @Override
    public void onDestroy() {
        if (mMessageReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        }
        super.onDestroy();
    }

    private void initReceivers() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter messageIntentFilter = new IntentFilter(Actions.ACTION_LOGOUT);
        messageIntentFilter.addAction(Actions.ACTION_LOGIN);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, messageIntentFilter);
    }

    private void initHandler() {
        if (mHandlerThread != null) {
            mExecHandler = new ExecHandler(this, mHandlerThread.getLooper());
        }
        mUIHandler = new UIHandler();
    }

    private void initViews(View rootView) {
        mLvMessages = (ListView) rootView.findViewById(R.id.fm_lv_message);
        mMessageAdapter = new MessageAdapter(getActivity());
        mMessageAdapter.setOnMessageReadListener(new MessageAdapter.OnMessageReadListener() {
            @Override
            public void onMessageRead(String messageId, int position) {
                sendMessageReadState(messageId, position);
            }
        });
        mLvMessages.addFooterView(getFooterView());
        mLvMessages.setAdapter(mMessageAdapter);
        mLvMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessageProtos.LikedMessage likedMessage = mMessageAdapter.getItem(position);
                if (likedMessage != null) {
                    sendMessageReadState(likedMessage.getId(), position);
                    WannaJoinListActivity.startActivityByPostId(getActivity(), likedMessage.getPostId(), likedMessage.getDest(), true, "");
                }
            }
        });
        mEmptyView = (EmptyView) rootView.findViewById(R.id.fm_empty_view);
        mLvMessages.setEmptyView(mEmptyView);
        initPtrFrame(rootView);
    }

    private void initPtrFrame(View rootView) {
        mPtrFrame = (PtrClassicFrameLayout) rootView.findViewById(R.id.fm_ptr_frame);
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                getMessagesAsync(true);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return Utils.canListPullDown(mLvMessages);
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
        if (TourPalApplication.getInstance().hasLogin()) {
            mPtrFrame.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPtrFrame.autoRefresh();
                }
            }, 100);
        }
    }

    private void getMessagesAsync(boolean refreshFromFirst) {
        if (mExecHandler != null) {
            Message msg = mExecHandler.obtainMessage(WHAT_GET_MESSAGES);
            Bundle data = new Bundle();
            if (refreshFromFirst) {
                mCurrentOffset = -1;
            }
            data.putInt("startOffset", mCurrentOffset);
            msg.setData(data);
            mExecHandler.sendMessage(msg);
        }
    }

    private void sendMessageReadState(String messageId, int position) {
        Message msg = mExecHandler.obtainMessage(WHAT_SET_MESSAGES_READ_STATE);
        Bundle data = new Bundle();
        data.putString("messageId", messageId);
        data.putInt("position", position);
        msg.setData(data);
        mExecHandler.sendMessage(msg);
    }

    public void sendAllMessageReadState() {
        if (mExecHandler == null){
            return;
        }

        if (!mExecHandler.hasMessages(WHAT_SET_ALL_MESSAGES_READ_STATE)) {
            mExecHandler.sendEmptyMessage(WHAT_SET_ALL_MESSAGES_READ_STATE);
        }
    }

    private View getFooterView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View footerView = inflater.inflate(R.layout.refresh_footer_view, null, false);
        mLlFooterContent = (LinearLayout) footerView.findViewById(R.id.rfv_ll_content);
        mBtnFooterLoadNext = (Button) footerView.findViewById(R.id.rfv_btn_load);
        mBtnFooterLoadNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //加载下一页
                getMessagesAsync(false);
            }
        });
        mLlFooterContent.setVisibility(View.GONE);
        return footerView;
    }

    private void setHandlerThread(HandlerThread thread) {
        mHandlerThread = thread;
    }

    class ExecHandler extends Handler {

        private WeakReference<MessageFragment> mFragmentRef;

        public ExecHandler(MessageFragment mf, Looper looper) {
            super(looper);
            mFragmentRef = new WeakReference<MessageFragment>(mf);
        }

        @Override
        public void handleMessage(Message msg) {
            MessageFragment fragment = mFragmentRef.get();
            if (fragment != null) {
                final Context context = fragment.getActivity();
                if (context == null){
                    return;
                }
                Bundle data = msg.getData();
                switch (msg.what) {
                    case WHAT_GET_MESSAGES:
                        if (!Utils.isNetworkConnected(context)) {
                            Toast.makeText(context, R.string.network_disable, Toast.LENGTH_SHORT).show();
                            fragment.mUIHandler.sendEmptyMessage(WHAT_REFRESH_MESSAGE_LIST);
                            break;
                        }
                        int startOffset = data.getInt("startOffset");
                        Request getMsgRequest = RequestBuilder.buildGetWannaJoinMineRequest(startOffset, MESSAGE_COUNT_PER_REQUEST);
                        if (getMsgRequest != null) {
                            MessageProtos.ResponseInfo respInfo = getMsgRequest.get();
                            if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                MessageProtos.LikedMessageList lmList = respInfo.getLikedMessageList();
                                if (lmList != null) {
                                    MessageProtos.PageResponseInfo pri = lmList.getPageResponseInfo();
                                    if (pri != null) {
                                        fragment.mCanFetchNextPage = pri.getHasRest();
                                        List<MessageProtos.LikedMessage> messageList = lmList.getLikedMessageList();
                                        if (startOffset <= 0) {
                                            fragment.mMessageAdapter.setDatas(messageList);
                                            checkNewMsgFirstLaunch(fragment, (MainTabsActivity) context, messageList);
                                        } else {
                                            fragment.mMessageAdapter.addDatas(messageList);
                                        }
                                        fragment.mCurrentOffset = pri.getEndOffset();
                                    }
                                }
                            }
                            fragment.mUIHandler.sendEmptyMessage(WHAT_REFRESH_MESSAGE_LIST);
                        }
                        break;
                    case WHAT_SET_MESSAGES_READ_STATE:
                        String messageId = data.getString("messageId");
                        int position = data.getInt("position");
                        if (!TextUtils.isEmpty(messageId)) {
                            Request setReadRequest = RequestBuilder.buildSetMessageReadStateRequest(messageId);
                            if (setReadRequest != null) {
                                MessageProtos.ResponseInfo respInfo = setReadRequest.get();
                                if (respInfo != null && respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                    //暂时不做处理
                                    if (mMessageAdapter != null) {
                                        MessageProtos.LikedMessage likedMessage = mMessageAdapter.getItem(position);
                                        if (likedMessage != null) {
                                            likedMessage.setIsRead(1);
                                            fragment.mUIHandler.sendEmptyMessage(WHAT_REFRESH_MESSAGE_LIST);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case WHAT_SET_ALL_MESSAGES_READ_STATE:
                        List<MessageProtos.LikedMessage> messageList = fragment.mMessageAdapter.getDatas();
                        ArrayList<String> ids = new ArrayList<>();
                        if (messageList != null && messageList.size() > 0) {
                            for (MessageProtos.LikedMessage message : messageList) {
                                if (message != null) {
                                    if (message.getIsRead() == 0) {
                                        ids.add(message.getId());
                                    }
                                }
                            }
                            Request setAllReadRequest = RequestBuilder.buildSetMessageReadStateRequest(ids);
                            if (setAllReadRequest != null) {
                                MessageProtos.ResponseInfo respInfo = setAllReadRequest.get();
                                if (respInfo != null) {
                                    if (Env.DEBUG) {
                                        Log.d(TAG, "setAllReadRequest err_code=" + respInfo.getErrCode());
                                    }
                                    if (respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                        fragment.getMessagesAsync(true);
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 首次启动检测有无新消息，有新消息则在MainTabsActivity显示红点
     *
     * @param fragment    fragment
     * @param context     MainTabsActivity
     * @param messageList 首次请求的结果
     */
    private void checkNewMsgFirstLaunch(MessageFragment fragment, final MainTabsActivity context, List<MessageProtos.LikedMessage> messageList) {
        if (!sHasFirstCheckNewMsg) {
            //首次启动应用时，检测一下有没有新消息，有新消息则标上红点
            if (messageList != null && messageList.size() > 0 && messageList.get(0).getIsRead() == 0) {
                sHasFirstCheckNewMsg = true;
                SharedPref.getInstance().setHasNewMessage(true);
                fragment.mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((MainTabsActivity) context).checkHasNewMessage();
                    }
                });
            }
        }
    }

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_REFRESH_MESSAGE_LIST:
                    mMessageAdapter.notifyDataSetChanged();
                    mPtrFrame.refreshComplete();
                    if (mMessageAdapter.getCount() > 0 && mCanFetchNextPage) {
                        mLlFooterContent.setVisibility(View.VISIBLE);
                    } else {
                        mLlFooterContent.setVisibility(View.GONE);
                    }
                    updateEmtpyView();
                    break;
                default:
                    break;
            }
        }
    }

    private void updateEmtpyView() {
        if (mMessageAdapter.getCount() == 0) {
            if (TourPalApplication.getInstance().hasLogin()) {
                mEmptyView.setText(R.string.fm_empty_view_text);
                mEmptyView.setIcon(R.mipmap.ic_empty_message);
            } else {
                mEmptyView.setIcon(0);
                mEmptyView.setText(R.string.fm_empty_view_text_not_login_yet);
            }
        }
    }

    /**
     * 刷新消息列表
     */
    public void refreshMessageList() {
        getMessagesAsync(true);
    }

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (action.equals(Actions.ACTION_LOGOUT)) {
                mMessageAdapter.setDatas(new ArrayList<MessageProtos.LikedMessage>());
                mMessageAdapter.notifyDataSetChanged();
                updateEmtpyView();
            } else if (action.equals(Actions.ACTION_LOGIN)) {
                refreshMessageList();
            }
        }
    }

}
