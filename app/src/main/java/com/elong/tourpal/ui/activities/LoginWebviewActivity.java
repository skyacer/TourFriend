package com.elong.tourpal.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Actions;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.Extras;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.application.Version;
import com.elong.tourpal.net.GetMyUserInfoTask;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.support.stat.Statistics;
import com.elong.tourpal.support.stat.StatisticsEnv;
import com.elong.tourpal.ui.views.CommonTitleBar;
import com.elong.tourpal.utils.SharedPref;

public class LoginWebviewActivity extends ActionBarActivity implements View.OnClickListener {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = LoginWebviewActivity.class.getSimpleName();
    private static final String LOGIN_URL_FORMAT = "https://msecure.elong.com/login/?RedirectUrl=%s&ref=%s";
    private static final String LOGIN_REDIRECT_URL_FORMAT = "https://msecure.elong.com/login/%s";
    private static final String LY_REDIRECT_URL = "lyloginsuccess";
    private static final String MY_PRODUCT = "tourpal";
    private static final String LY_UA = "ewandroidtourpal";
    private static final String COOKIE_SESSION_ID = "H5SessionId";
    private static final String COOKIE_SESSIION_TOKEN = "SessionToken";

    private EditText mURLET;
    private Button mLoadURLBtn;
    private WebView mWvLogin = null;
    private CommonTitleBar mTitleBar;

    private boolean mGo2PersonalCenterAfterLogin = false;//完成登录后是否跳转到个人中心页面

    private boolean mCanGoBack = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //打点数据
        Statistics.log(getApplicationContext(), StatisticsEnv.USERCENTER_LOGIN_ENTER);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_webview);
        resolveIntent();
        initViews();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extras.EXTRA_GO_2_PERSONAL_CENTER_AFTER_LOGIN)) {
            mGo2PersonalCenterAfterLogin = intent.getBooleanExtra(Extras.EXTRA_GO_2_PERSONAL_CENTER_AFTER_LOGIN, false);
        }
    }

    private void initViews() {
        final String loginUrl = String.format(LOGIN_URL_FORMAT, LY_REDIRECT_URL, MY_PRODUCT);
        final String loginSucceedUrl = String.format(LOGIN_REDIRECT_URL_FORMAT, LY_REDIRECT_URL);

        mTitleBar = (CommonTitleBar) findViewById(R.id.login_titlebar);
        mTitleBar.getTitleView().setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_login_elong, 0, 0, 0);
        mTitleBar.getTitleView().setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.login_elong_padding));
        mTitleBar.setTitle(R.string.elong_login);
        mTitleBar.setOnBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWvLogin.canGoBack()) {
                    mWvLogin.goBack();
                } else {
                    finish();
                }
            }
        });

        mURLET = (EditText) findViewById(R.id.webview_url);
        mLoadURLBtn = (Button) findViewById(R.id.webview_load);
        mLoadURLBtn.setOnClickListener(this);

        mWvLogin = (WebView) findViewById(R.id.lwa_wv_login);
        mWvLogin.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.equals(loginSucceedUrl)) {
                    doLoginSuccess(loginUrl);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        mWvLogin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && mWvLogin.canGoBack()) {  //表示按返回键
                        mWvLogin.goBack();
                        return true;    //已处理
                    }
                }
                return false;
            }
        });
        mWvLogin.getSettings().setJavaScriptEnabled(true);
        mWvLogin.addJavascriptInterface(new JsInterface(), "tourpal");
        mWvLogin.getSettings().setUserAgentString(getUserAgentString());
        mWvLogin.loadUrl(loginUrl);
        if (DEBUG) {
            Log.e(TAG, "ua= " + mWvLogin.getSettings().getUserAgentString());
            Log.e(TAG, "url=" + loginUrl);
        }
    }

    private void doLoginSuccess(String loginUrl) {
        //1.保存session的id和token
        CookieManager cookieManager = CookieManager.getInstance();
        String cookie = cookieManager.getCookie(loginUrl);
        saveCookies(cookie);
        TourPalApplication.getInstance().setHasLogin(true);
        if (Env.DEBUG) {
            Log.e(TAG, "cookie = " + cookie);
        }
        //2.获取个人信息（若已经获取到push的clientId，则绑定此id）
        GetMyUserInfoTaskExt task = new GetMyUserInfoTaskExt();
        task.execute();
        //3.发送登录成功广播
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Actions.ACTION_LOGIN));
    }

    private String getUserAgentString() {
        return mWvLogin.getSettings().getUserAgentString() + " " + LY_UA + "/" + Version.code;
//        return String.format(USER_AGENT_FORMAT, LY_UA, Version.code);
    }

    /**
     * 从cookie中取出session和token加密保存到sharedPreference
     *
     * @param cookies
     */
    private void saveCookies(String cookies) {
        if (!TextUtils.isEmpty(cookies)) {
            String[] cookieArray = cookies.split(";");
            SharedPref pref = SharedPref.getInstance();
            for (String cookie : cookieArray) {
                String[] kv = cookie.split("=");
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String val = kv[1].trim();
                    if (COOKIE_SESSION_ID.equals(key)) {
                        pref.setSessionId(val);
                    } else if (COOKIE_SESSIION_TOKEN.equals(key)) {
                        pref.setSessionToken(val);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        String url = mURLET.getText().toString();
        if (url != null) {
            loadURL(url);
        }
    }

    private void loadURL(String url) {
        mWvLogin.loadUrl(url);
        if (DEBUG) {
            Log.d(TAG, mWvLogin.getSettings().getUserAgentString());
        }
    }

    class GetMyUserInfoTaskExt extends GetMyUserInfoTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCanGoBack = false;
        }

        @Override
        protected MessageProtos.UserInfo doInBackground(Integer... params) {
            //若已经获取到push的clientId，则绑定此id
            String clientId = SharedPref.getInstance().getPushClientId();
            if (!TextUtils.isEmpty(clientId)) {
                Request request = RequestBuilder.buildBindPushClientIdRequest();
                if (request != null) {
                    MessageProtos.ResponseInfo respInfo = request.get();
                    if (respInfo != null) {
                        if (DEBUG) {
                            Log.d(TAG, "bind push client id result: err_code=" + respInfo.getErrCode());
                        }
                    }
                }
            }
            return super.doInBackground(params);
        }

        @Override
        protected void onPostExecute(MessageProtos.UserInfo userInfo) {
            mCanGoBack = true;
            //跳转到相应界面
            if (userInfo != null) {
                Toast.makeText(LoginWebviewActivity.this, R.string.login_succeed, Toast.LENGTH_SHORT).show();
                if (!userInfo.getIsComplete()) {
                    //用户信息不完整，跳转到信息页面填充完整
                    Intent intent = new Intent(LoginWebviewActivity.this, EditUserInfoActivity.class);
                    startActivity(intent);
                } else {
                    if (!mGo2PersonalCenterAfterLogin) {
                        finish();
                    } else {
                        //跳转到个人中心
                        Intent intent = new Intent(LoginWebviewActivity.this, MainTabsActivity.class);
                        intent.putExtra(Extras.EXTRA_MAIN_TAB_START_TAB_IDX, MainTabsActivity.IDX_PERSONAL_CENTER);
                        startActivity(intent);
                    }
                }
            } else {
                Toast.makeText(LoginWebviewActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mCanGoBack) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public final class JsInterface {
        @JavascriptInterface
        public void onTitleChange(String title) {
            final String titleName = title;
            mWvLogin.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String titleName1 = titleName;
                    if (getString(R.string.login).equals(titleName1)) {
                        titleName1 = getString(R.string.elong_login);
                    }

                    mTitleBar.setTitle(titleName1);

                    if (Env.DEBUG){
                        Toast.makeText(LoginWebviewActivity.this, "title已更改为" + titleName1, Toast.LENGTH_SHORT).show();
                    }
                }
            }, 10);
        }
    }
}
