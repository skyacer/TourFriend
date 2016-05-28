package com.elong.tourpal.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.activities.DestinationSerarchActivity;
import com.elong.tourpal.ui.activities.MainTabsActivity;
import com.elong.tourpal.ui.activities.PostListActivity;
import com.elong.tourpal.ui.listeners.TourpalMainInteractionListener;
import com.elong.tourpal.ui.supports.HotDestinationsAdapter;
import com.elong.tourpal.utils.SharedPref;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 主tabs页面中的首页fragment
 */
public class TourpalMainFragment extends FragmentBase4Pager implements View.OnClickListener {

    private static final String ARG_PAGER_INDEX = "PAGER_INDEX";
    private static final long HOT_DEST_UPDATE_INTERVAL = 24 * 3600 * 1000;
    private static final int WHAT_GET_HOT_DESTINATIONS = 101;
    private static final int WHAT_REFRESH_HOT_DESTINATIONS = 201;

    private TourpalMainInteractionListener mListener;
    private TextView mDestinationSearchTV;
    private ListView mLvHotDestinations = null;

    private HotDestinationsAdapter mHotDestinationAdapter = null;
    private HandlerThread mHandlerThread = null;
    private ExecHandler mExecHandler = null;
    private UIHandler mUIHandler = null;

    public static TourpalMainFragment newInstance(int pagerIndex, HandlerThread thread) {
        TourpalMainFragment fragment = new TourpalMainFragment();
        fragment.setHandlerThread(thread);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGER_INDEX, pagerIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public TourpalMainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            setPageIndex(getArguments().getInt(ARG_PAGER_INDEX, 0));
        }
        initHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tourpal_main, container, false);
        initViews(rootView);
        checkAndUpdateHotDest();
        loadLastHotDests();
        return rootView;
    }

    private void initHandler() {
        if (mHandlerThread != null) {
            mExecHandler = new ExecHandler(this, mHandlerThread.getLooper());
        }
        mUIHandler = new UIHandler();
    }

    private void initViews(View root) {
        mDestinationSearchTV = (TextView) root.findViewById(R.id.tp_m_tv_search);
        root.findViewById(R.id.tp_m_rl_search).setOnClickListener(this);
        mLvHotDestinations = (ListView) root.findViewById(R.id.tp_m_lv_hot_dest);
        mHotDestinationAdapter = new HotDestinationsAdapter(getActivity());
        mLvHotDestinations.setAdapter(mHotDestinationAdapter);
        mLvHotDestinations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //打点数据
                Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.MAIN_HOT_DESTINATION);

                MessageProtos.HotCity hd = mHotDestinationAdapter.getItem(position);
                String dest = hd.getName();
                if (dest != null) {
                    PostListActivity.startActivityByDest(getActivity(), dest);
                }
            }
        });
    }

    public void setHandlerThread(HandlerThread thread) {
        mHandlerThread = thread;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TourpalMainInteractionListener) ((MainTabsActivity) activity).getFragmentInteractionListener(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * 加载上次记录的热门目的地
     */
    private void loadLastHotDests() {
        List<MessageProtos.HotCity> lastHotDests = SharedPref.getInstance().getLastHotDests();
        if (lastHotDests == null || lastHotDests.size() == 0) {
            //无热门目的地的历史记录，先使用默认的来填充
            lastHotDests = getDefaultHotDests();
        }
        mHotDestinationAdapter.setDatas(lastHotDests);
        mHotDestinationAdapter.notifyDataSetChanged();
    }

    private List<MessageProtos.HotCity> getDefaultHotDests() {
        List<MessageProtos.HotCity> hotDests = new ArrayList<>();
        MessageProtos.HotCity hotDest = new MessageProtos.HotCity();
        hotDest.setName("北京");
        hotDest.setDesc("东方古郡，长城故乡");
        hotDest.setUrl("http://pavo.elongstatic.com/i/tourpal_1020x390/0000yPGD.jpg");
        hotDests.add(hotDest);
        hotDest = new MessageProtos.HotCity();
        hotDest.setName("成都");
        hotDest.setDesc("来了就不想走的城市");
        hotDest.setUrl("http://pavo.elongstatic.com/i/tourpal_1020x390/0000yPGG.jpg");
        hotDests.add(hotDest);
        hotDest = new MessageProtos.HotCity();
        hotDest.setName("杭州");
        hotDest.setDesc("最忆是杭州");
        hotDest.setUrl("http://pavo.elongstatic.com/i/tourpal_1020x390/0000yPGI.jpg");
        hotDests.add(hotDest);
        return hotDests;
    }

    /**
     * 检查是否超过设定的更新周期，若超过，则重新获取一下热门目的地
     */
    private void checkAndUpdateHotDest() {
        long curTime = System.currentTimeMillis();
        if (curTime - SharedPref.getInstance().getLastHotDestUpdateTime() > HOT_DEST_UPDATE_INTERVAL) {
            //更新
            getNewHotDest();
        }
    }

    private void getNewHotDest() {
        if (mExecHandler != null) {
            mExecHandler.sendEmptyMessage(WHAT_GET_HOT_DESTINATIONS);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tp_m_tv_search:
            case R.id.tp_m_rl_search:
                //打点数据
                Statistics.log(getActivity().getApplicationContext(), StatisticsEnv.MAIN_SEARCH);
                Intent intent = new Intent(this.getActivity(), DestinationSerarchActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

    }

    public static class ExecHandler extends Handler {

        private WeakReference<TourpalMainFragment> mFragmentRef;

        public ExecHandler(TourpalMainFragment fragment, Looper looper) {
            super(looper);
            mFragmentRef = new WeakReference<TourpalMainFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            TourpalMainFragment fragment = mFragmentRef.get();
            Context context = fragment.getActivity();
            if (context != null && fragment != null) {
                switch (msg.what) {
                    case WHAT_GET_HOT_DESTINATIONS:
                        Request hotDestReq = RequestBuilder.buildGetHotDestRequest();
                        if (hotDestReq != null) {
                            MessageProtos.ResponseInfo respInfo = hotDestReq.get();
                            if (respInfo != null) {
                                if (respInfo.getErrCode() == MessageProtos.SUCCESS) {
                                    if (respInfo.getHotCitysCount() > 0) {
                                        List<MessageProtos.HotCity> hotDests = respInfo.getHotCitysList();
                                        fragment.mHotDestinationAdapter.setDatas(hotDests);
                                        fragment.mUIHandler.sendEmptyMessage(WHAT_REFRESH_HOT_DESTINATIONS);
                                        //获取完毕，更新
                                        SharedPref.getInstance().setLastHotDestUpdateTime(System.currentTimeMillis());
                                        SharedPref.getInstance().setLastHotDests(hotDests);
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

    public class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_REFRESH_HOT_DESTINATIONS:
                    mHotDestinationAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }

}
