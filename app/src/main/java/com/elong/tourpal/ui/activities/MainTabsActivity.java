package com.elong.tourpal.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.push.PushHelper;
import com.elong.tourpal.search.DestinationSearchManager;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.fragments.FragmentBase4Pager;
import com.elong.tourpal.ui.fragments.MessageFragment;
import com.elong.tourpal.ui.fragments.PersonalCenterFragment;
import com.elong.tourpal.ui.fragments.TourpalMainFragment;
import com.elong.tourpal.ui.listeners.BaseFragmentInteractionListener;
import com.elong.tourpal.update.DataUpdateManager;
import com.elong.tourpal.update.UpdateManager;
import com.elong.tourpal.utils.SharedPref;
import com.igexin.sdk.PushManager;

import java.util.HashMap;
import java.util.Locale;


public class MainTabsActivity extends ActivityBase {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = MainTabsActivity.class.getSimpleName();
    private static boolean sIsFirst = true;//用于控制首次onCreate检测更新

    public static final int IDX_MAIN = 0;
    public static final int IDX_MSG = 1;
    public static final int IDX_PERSONAL_CENTER = 2;

    private int mCurrentTabIdx = 0;

    private LinearLayout mLlTabMain = null;
    private TextView mTvTabMain = null;
    private ImageView mIvTabMain = null;

    private LinearLayout mLlTabMsg = null;
    private TextView mTvTabMsg = null;
    private ImageView mIvTabMsg = null;
    private ImageView mIvTabMsgNew = null;
    boolean mHasNewMsg = false;

    private LinearLayout mLlTabPersonalCenter = null;
    private TextView mTvTabPersonalCenter = null;
    private ImageView mIvTabPersonalCenter = null;

    private OnTabClickListener mOnTabClickListener;

    private NewWannaJoinMessageReceiver mNewWannaJoinMessageReceiver;

    private MessageFragment mMessageFragment = null;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private HashMap<Integer, BaseFragmentInteractionListener> mFragmentListenersMap;

    private HandlerThread mHandlerThread;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(TAG, "onCreate()");
        }
        super.onCreate(savedInstanceState);
        //打点数据
        Statistics.log(getApplicationContext(), StatisticsEnv.MAIN_ENTER);

        setContentView(R.layout.activity_main_tabs);

        //初始化push服务
        PushManager.getInstance().initialize(this.getApplicationContext());

        initThread();
        initViews();
        handleIntent();
        // 初始化目的地搜索
        DestinationSearchManager.getInstance(this.getApplicationContext());
        // 首次安装打点
        checkFirstLaunch();
        // 检查并上传打点数据
        Statistics.uploadStatisticsData();
        // 延时检查目的地数据更新
        mViewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                DataUpdateManager.destinationFileUpdate();
            }
        }, 30 * 1000);

        if (sIsFirst){
            mViewPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UpdateManager updateManager = new UpdateManager(MainTabsActivity.this);
                    updateManager.checkUpdate(true);
                }
            }, 2 * 1000);
            sIsFirst = false;
        }

        checkHasNewMessage();

        //初始化广播接收器
        mNewWannaJoinMessageReceiver = new NewWannaJoinMessageReceiver();
        IntentFilter newWannaJoinMessageFilter = new IntentFilter(Actions.ACTION_RECEIVE_NEW_MESSAGE);
        newWannaJoinMessageFilter.addAction(Actions.ACTION_LOGOUT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mNewWannaJoinMessageReceiver, newWannaJoinMessageFilter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
        checkHasNewMessage();
    }

    @Override
    protected void onDestroy() {
        if (mNewWannaJoinMessageReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mNewWannaJoinMessageReceiver);
        }
//        if (mMessageFragment != null) {
//            mMessageFragment.sendAllMessageReadState();
//        }
        super.onDestroy();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        int startTabIdx = intent.getIntExtra(Extras.EXTRA_MAIN_TAB_START_TAB_IDX, -1);
        switch (startTabIdx) {
            case IDX_MAIN:
                changeTitle(IDX_MAIN);
                changeCurrentItem(IDX_MAIN, false);
                break;
            case IDX_MSG:
                changeTitle(IDX_MSG);
                changeCurrentItem(IDX_MSG, false);
                break;
            case IDX_PERSONAL_CENTER:
                changeTitle(IDX_PERSONAL_CENTER);
                changeCurrentItem(IDX_PERSONAL_CENTER, false);
                break;
            default:
                break;
        }
    }

    private void initThread() {
        mHandlerThread = new HandlerThread("mtaThread");
        mHandlerThread.start();
    }

    private void initViews() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.mt_vp_contents);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeTitle(position);
                changeTabUi(position);
                switch (position) {
                    case IDX_MAIN:
                        break;
                    case IDX_MSG:
                        if (!TourPalApplication.getInstance().hasLogin()) {
                            //若没登录，则直接跳转到登录页面
                            Intent intent = new Intent(MainTabsActivity.this, LoginWebviewActivity.class);
                            startActivity(intent);
                        }
                        break;
                    case IDX_PERSONAL_CENTER:
                        break;
                }
//                if (mCurrentTabIdx == IDX_MSG && position != IDX_MSG) {
//                    //从消息tab切到非消息tab，则消掉所有的未读
//                    if (mMessageFragment != null) {
//                        mMessageFragment.sendAllMessageReadState();
//                    }
//                }
                mCurrentTabIdx = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mOnTabClickListener = new OnTabClickListener();
        mTvTabMain = (TextView) findViewById(R.id.mt_tv_tab_main);
        mIvTabMain = (ImageView) findViewById(R.id.mt_iv_tab_main);
        mLlTabMain = (LinearLayout) findViewById(R.id.mt_ll_tab_main);
        mLlTabMain.setOnClickListener(mOnTabClickListener);

        mTvTabMsg = (TextView) findViewById(R.id.mt_tv_tab_msg);
        mIvTabMsg = (ImageView) findViewById(R.id.mt_iv_tab_msg);
        mLlTabMsg = (LinearLayout) findViewById(R.id.mt_ll_tab_msg);
        mLlTabMsg.setOnClickListener(mOnTabClickListener);
        mIvTabMsgNew = (ImageView) findViewById(R.id.mt_iv_tab_msg_new);

        mTvTabPersonalCenter = (TextView) findViewById(R.id.mt_tv_tab_personal_center);
        mIvTabPersonalCenter = (ImageView) findViewById(R.id.mt_iv_tab_personal_center);
        mLlTabPersonalCenter = (LinearLayout) findViewById(R.id.mt_ll_tab_personal_center);
        mLlTabPersonalCenter.setOnClickListener(mOnTabClickListener);

        //初始title和tab选择为首页
        changeTitle(IDX_MAIN);
        changeTabUi(IDX_MAIN);
    }

    private void changeTitle(int position) {
        switch (position) {
            case IDX_MAIN:
                //打点数据
                Statistics.log(getApplicationContext(), StatisticsEnv.MAIN_POSTING);

                setTitleLeftBtnEnable(false);
                setTitleRightBtnEnable(true);
                setTitleText(R.string.tp_m_title);
                setTitleRightBtn(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainTabsActivity.this, PostingTourActivity.class);
                        startActivity(i);
                    }
                });
                break;
            case IDX_MSG:
                setTitleText(R.string.tp_msg_title);
                setTitleRightBtnEnable(false);
                break;
            case IDX_PERSONAL_CENTER:
                setTitleText(R.string.tp_my_title);
                setTitleRightBtnEnable(false);
                break;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case IDX_MAIN:
                    return TourpalMainFragment.newInstance(IDX_MAIN, mHandlerThread);
                case IDX_MSG:
                    mMessageFragment = MessageFragment.newInstance(IDX_MSG, mHandlerThread);
                    return mMessageFragment;
                case IDX_PERSONAL_CENTER:
                    return PersonalCenterFragment.newInstance(IDX_PERSONAL_CENTER);
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_tabs, container, false);
            return rootView;
        }
    }

    public BaseFragmentInteractionListener getFragmentInteractionListener(FragmentBase4Pager fragment) {
        if (mFragmentListenersMap != null) {
            return mFragmentListenersMap.get(fragment.getPageIndex());
        }
        return null;
    }

    class OnTabClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mt_ll_tab_main:
                    changeCurrentItem(IDX_MAIN, true);
                    break;
                case R.id.mt_ll_tab_msg:
                    changeCurrentItem(IDX_MSG, true);
                    break;
                case R.id.mt_ll_tab_personal_center:
                    changeCurrentItem(IDX_PERSONAL_CENTER, true);
                    break;
            }
        }
    }

    private void changeCurrentItem(int item, boolean smoothScroll) {
        int currentItem = mViewPager.getCurrentItem();
        if (item != currentItem) {
            mViewPager.setCurrentItem(item, smoothScroll);
        }
    }

    private void changeTabUi(int item) {
        switch (item) {
            case IDX_MAIN:
                mIvTabMain.setImageResource(R.mipmap.ic_home_selected);
                mIvTabMsg.setImageResource(R.mipmap.ic_message);
                mIvTabPersonalCenter.setImageResource(R.mipmap.ic_personal_center);
                mTvTabMain.setTextColor(getResources().getColor(R.color.tab_item_text_selected));
                mTvTabMsg.setTextColor(getResources().getColor(R.color.tab_item_text_normal));
                mTvTabPersonalCenter.setTextColor(getResources().getColor(R.color.tab_item_text_normal));
                break;
            case IDX_MSG:
                mIvTabMain.setImageResource(R.mipmap.ic_home);
                mIvTabMsg.setImageResource(R.mipmap.ic_message_selected);
                mIvTabPersonalCenter.setImageResource(R.mipmap.ic_personal_center);
                mTvTabMain.setTextColor(getResources().getColor(R.color.tab_item_text_normal));
                mTvTabMsg.setTextColor(getResources().getColor(R.color.tab_item_text_selected));
                mTvTabPersonalCenter.setTextColor(getResources().getColor(R.color.tab_item_text_normal));
                if (mHasNewMsg) {
                    cancelNewMsgFlag();
                }
                break;
            case IDX_PERSONAL_CENTER:
                mIvTabMain.setImageResource(R.mipmap.ic_home);
                mIvTabMsg.setImageResource(R.mipmap.ic_message);
                mIvTabPersonalCenter.setImageResource(R.mipmap.ic_personal_center_selected);
                mTvTabMain.setTextColor(getResources().getColor(R.color.tab_item_text_normal));
                mTvTabMsg.setTextColor(getResources().getColor(R.color.tab_item_text_normal));
                mTvTabPersonalCenter.setTextColor(getResources().getColor(R.color.tab_item_text_selected));
                break;
        }
    }

    private void cancelNewMsgFlag() {
        mHasNewMsg = false;
        mIvTabMsgNew.setVisibility(View.GONE);
        SharedPref.getInstance().setHasNewMessage(false);
        //消掉通知栏
        PushHelper.cancelNotification(MainTabsActivity.this, PushHelper.NOTIFICATION_ID_WANNA_JOIN);
    }

    /**
     * tab的红点检查
     */
    public void checkHasNewMessage() {
        mHasNewMsg = SharedPref.getInstance().hasNewMessage();
        if (mViewPager != null) {
            if (mHasNewMsg) {
                if (mViewPager.getCurrentItem() != IDX_MSG) {
                    mIvTabMsgNew.setVisibility(View.VISIBLE);
                } else {
                    mIvTabMsgNew.setVisibility(View.GONE);
                    cancelNewMsgFlag();
                }
            } else {
                mIvTabMsgNew.setVisibility(View.GONE);
            }
        } else {
            mIvTabMsgNew.setVisibility(View.GONE);
        }
    }

    class NewWannaJoinMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Actions.ACTION_RECEIVE_NEW_MESSAGE)) {
                checkHasNewMessage();
                if (mMessageFragment != null) {
                    mMessageFragment.refreshMessageList();
                }
            } else if (action.equals(Actions.ACTION_LOGOUT)) {
                checkHasNewMessage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TourPalApplication.getInstance().getQQapi().onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 首次启动打点
     */
    private void checkFirstLaunch() {
        SharedPref pref = SharedPref.getInstance();
        if (pref.isFirstLaunch()) {
            Statistics.log(this, StatisticsEnv.APP_FIRST_LAUNCH, 1);
            pref.setFirstLaunch(false);
        }
    }
}
