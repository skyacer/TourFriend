package com.elong.tourpal.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.search.DestinationDataManager;
import com.elong.tourpal.search.DestinationSearchManager;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.supports.SearchDestinationAdapter;
import com.elong.tourpal.ui.views.CommonTitleBar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhitao.xu on 2015/4/10.
 */
public class DestinationSerarchActivity extends Activity {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = DestinationSerarchActivity.class.getSimpleName();
    private CommonTitleBar mTitleBar;
    private EditText mSearchET;
    private View mSearchCancel;
    private TextView mSearchCancelTV;
    private TextView mSearchNoResult;
    /*
     * 目的地搜索提示列表
     */
    private ListView mDestinationSearchListview;
    private SearchDestinationAdapter mDestinationSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.destination_search_activity);
        initView();
    }

    private void initView() {
        mTitleBar = (CommonTitleBar) findViewById(R.id.destination_search_titlebar);
        mTitleBar.setContentView(R.layout.destination_search_titlebar);

        mSearchNoResult = (TextView) findViewById(R.id.destination_search_no_result);
        mSearchET = (EditText) mTitleBar.findViewById(R.id.destination_search_titlebar_et);
        mSearchCancel = mTitleBar.findViewById(R.id.destination_search_titlebar_right_tv_ll);
        mSearchCancelTV = (TextView) mTitleBar.findViewById(R.id.destination_search_titlebar_right_tv);
        mSearchCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHasSearch) {
                    //打点数据
                    Statistics.log(getApplicationContext(), StatisticsEnv.SEARCH_CANCEL);
                }

                finish();
            }
        });
        mSearchET.addTextChangedListener(mTextWatcher);
        mSearchET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });
        mDestinationSearchListview = (ListView) findViewById(R.id.destination_search_lv);
        mDestinationSearchListview.setOnItemClickListener(mDestinationOnItemClickListener);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

                           public void run() {
                               InputMethodManager inputManager =
                                       (InputMethodManager) mSearchET.getContext().getSystemService
                                               (Context.INPUT_METHOD_SERVICE);
                               inputManager.showSoftInput(mSearchET, 0);
                           }

                       },
                500);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (TextUtils.isEmpty(text)) {
                mDestinationSearchListview.setVisibility(View.GONE);
            } else {
                mDestinationSearchListview.setVisibility(View.VISIBLE);
            }

            // 输入框有内容就进行搜索建议
            final ArrayList<DestinationDataManager.DestinationOrigData> citys =
                    (ArrayList<DestinationDataManager.DestinationOrigData>) DestinationSearchManager
                            .getInstance(TourPalApplication.getAppContext()).search(s.toString());
            mDestinationSearchAdapter = new SearchDestinationAdapter(DestinationSerarchActivity.this, citys);
            if (DEBUG) {
                Log.d(TAG, "citys size = " + citys.size());
            }
            mDestinationSearchListview.setAdapter(mDestinationSearchAdapter);
            if (citys.size() == 0 && !TextUtils.isEmpty(text)) {
                mSearchNoResult.setVisibility(View.VISIBLE);
            } else {
                mSearchNoResult.setVisibility(View.GONE);
            }
        }
    };

    private boolean mHasSearch = false;
    private AdapterView.OnItemClickListener mDestinationOnItemClickListener = new AdapterView
            .OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mHasSearch = true;
            //打点数据
            Statistics.log(getApplicationContext(), StatisticsEnv.SEARCH_FINISH);

            final DestinationDataManager.DestinationOrigData selectedDestination = mDestinationSearchAdapter
                    .getSelectData(position);
            String desName = selectedDestination.mDesName;
            if (DEBUG) {
                Log.d(TAG, String.format("level=%d, name=%s, grandparent name=%s", selectedDestination.mLevel,
                        selectedDestination.mDesName,
                        selectedDestination.mGrandparents == null ? "null" : selectedDestination.mGrandparents.get(0).mDesName));
            }
            if (selectedDestination.mLevel > 2) {
                if (selectedDestination.mGrandparents != null && selectedDestination.mGrandparents.get(0) != null) {
                    desName = selectedDestination.mGrandparents.get(0).mDesName;
                }
            }

            PostListActivity.startActivityByDest(DestinationSerarchActivity.this, desName);
        }
    };
}
