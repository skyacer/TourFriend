package com.elong.tourpal.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.ui.supports.WannaJoinListAdapter;
import com.elong.tourpal.ui.views.EmptyView;

import java.util.List;

public class WannaJoinListActivity extends ActivityBase {

    private static final int MAX_ITEM_SIZE = 1000;

    private TextView mTvDest;
    private ListView mLvWannaJoinList;
    private WannaJoinListAdapter mWannaJoinListAdapter;
    private EmptyView mEmptyView;

    private String mPostId;
    private String mPostDest;
    private boolean mIsMy = false;
    private String mPostOwnerName;

    public static void startActivityByPostId(Context context, String postId, String postDest, boolean isMy, String ownerName) {
        if (!TextUtils.isEmpty(postId) && !TextUtils.isEmpty(postDest)) {
            Intent intent = new Intent(context, WannaJoinListActivity.class);
            intent.putExtra(Extras.EXTRA_POST_ID, postId);
            intent.putExtra(Extras.EXTRA_POST_DEST, postDest);
            intent.putExtra(Extras.EXTRA_IS_MY_POST, isMy);
            intent.putExtra(Extras.EXTRA_OWNER_NAME, ownerName);
            context.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wanna_join_list);
        resolveIntent();
        initViews();

        GetWannaJoinListTask getDataTask = new GetWannaJoinListTask();
        getDataTask.execute();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        mPostId = intent.getStringExtra(Extras.EXTRA_POST_ID);
        mPostDest = intent.getStringExtra(Extras.EXTRA_POST_DEST);
        mIsMy = intent.getBooleanExtra(Extras.EXTRA_IS_MY_POST, false);
        mPostOwnerName = intent.getStringExtra(Extras.EXTRA_OWNER_NAME);
        if (!TextUtils.isEmpty(mPostDest)) {
            mPostDest = mPostDest.replace(",", "、");
        }
    }

    private void initViews() {
        //title
        String titleText = getString(R.string.wanna_join_list_title);
        if (mIsMy) {
            titleText = getString(R.string.common_my) + titleText;
        } else if (mPostOwnerName != null) {
            titleText = getString(R.string.somebodys_format, mPostOwnerName, titleText);
        }
        setTitleText(titleText);
        setTitleLeftBtn(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitleRightBtnEnable(false);
        //views
        mEmptyView = (EmptyView) findViewById(R.id.wjl_empty_view);
        mEmptyView.setText(R.string.wanna_join_list_loading);
        mLvWannaJoinList = (ListView) findViewById(R.id.wjl_lv_list);
        mWannaJoinListAdapter = new WannaJoinListAdapter(this);
        mWannaJoinListAdapter.setIsPostOwner(mIsMy);
        mLvWannaJoinList.setAdapter(mWannaJoinListAdapter);
        mLvWannaJoinList.setEmptyView(mEmptyView);

        mTvDest = (TextView) findViewById(R.id.wjl_dest);
        mTvDest.setText(getString(R.string.wanna_join_dest_format, mPostDest == null ? "" : mPostDest));
        mTvDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostDetailActivity.startActivity(WannaJoinListActivity.this, mPostId);
            }
        });
    }

    class GetWannaJoinListTask extends AsyncTask<Integer, Integer, MessageProtos.ResponseInfo> {

        @Override
        protected MessageProtos.ResponseInfo doInBackground(Integer... params) {
            Request request = RequestBuilder.buildGetWannaJoinListByPostIdRequest(0, MAX_ITEM_SIZE, mPostId);
            if (request != null) {
                MessageProtos.ResponseInfo respInfo = request.get();
                return respInfo;
            }
            return null;
        }

        @Override
        protected void onPostExecute(MessageProtos.ResponseInfo responseInfo) {
            if (responseInfo != null && responseInfo.getErrCode() == MessageProtos.SUCCESS) {
                MessageProtos.LikedMessageList likedMessageList = responseInfo.getLikedMessageList();
                if (likedMessageList != null) {
                    List<MessageProtos.LikedMessage> data = likedMessageList.getLikedMessageList();
                    if (data.size() > 0) {
                        mWannaJoinListAdapter.setData(data);
                        mWannaJoinListAdapter.notifyDataSetChanged();
                    } else {
                        mEmptyView.setText(R.string.wanna_join_list_empty_text);
                    }
                }
            }
        }
    }

}
