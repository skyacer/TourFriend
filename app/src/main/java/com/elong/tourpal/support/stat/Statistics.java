package com.elong.tourpal.support.stat;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.net.Request;
import com.elong.tourpal.net.RequestBuilder;
import com.elong.tourpal.protocal.MessageProtos;
import com.elong.tourpal.utils.SharedPref;
import com.elong.tourpal.utils.StatSharedPref;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by zhitao.xu on 2015/4/17.
 */
public class Statistics {
    /**
     * 功能打点工具类。
     * 1. 分配各模块的打点ID：
     * ID为字符串类型，固定为5位，采用模块ID(2位)+功能ID(3位)的方式。模块ID统一分配，功能ID由各模块自行分配。
     * 2. 定义打点数据的本地保存格式和上传格式：
     * 各模块打点数据统一保存在用户数据目录的的stat_pref.xml中，采用(id, value)的方式保存。
     * 打点数据上传的格式如下：
     * id1value1.id2value2.id3value3....
     * 3. 定义功能打点在正常功能联网的URL中增加的参数：
     * Smid：IMEI的md5值
     * Svr：VersionCode
     * Sdt：打点数据
     * 例如：http://host/?Smid=45546c15c42d32584f0edbe808d32a75&Svr=3.2.5&Sdt=000011.000021
     */
    // 基础数据
    public static final String MODULE_ID_BASE = "00";
    // 更新
    public static final String MODULE_ID_UPGRADE = "01";
    // 首页
    public static final String MODULE_ID_MAIN_SCREEN = "02";
    // 帖子
    public static final String MODULE_ID_POST = "03";
    // 用户中心
    public static final String MODULE_ID_USER_CENTER = "04";
    // 搜索
    public static final String MODULE_ID_SEARCH = "05";
    // 目的地
    public static final String MODULE_ID_DESTINATION = "06";
    // 用户交流
    public static final String MODULE_ID_COMMUNICATION = "07";

    //00 BASE
    // 用户进入APP次数
    public static final String CODE_BASE_USER_ENTER_C = "001";
    // 首次进入app
    public static final String CODE_BASE_APP_FIRST_LAUNCH_C = "002";

    //02 MAIN
    // 用户进入首页的次数
    public static final String CODE_MAIN_ENTER_C = "001";
    // 首页点击搜索的次数
    public static final String CODE_MAIN_SEARCH_C = "002";
    // 首页点击热门目的地的次数
    public static final String CODE_MAIN_HOT_DESTINATION_C = "003";
    // 首页点击发帖的次数
    public static final String CODE_MAIN_POST_C = "004";

    //03 POSTING
    // 用户发帖量,指用户点击发帖完成的次数
    public static final String CODE_POST_POSTING_C = "001";
    // 用户进入发帖页的次数
    public static final String CODE_POST_ENTER_C = "002";
    // 用户进入发帖页后点击返回退出的次数
    public static final String CODE_POST_CANCEL_C = "003";
    // 用户在发帖页中点击选择相册的次数
    public static final String CODE_POST_ALBUM_C = "004";

    //04 USER CENTER
    // 进入用户中心页面的次数
    public static final String CODE_USER_CENTER_ENTER_C = "001";
    // 用户中心点击用户资料的次数
    public static final String CODE_USER_CENTER_USER_INFO_C = "002";
    // 用户中心点击用户发帖的次数
    public static final String CODE_USER_CENTER_USER_POST_C = "003";
    // 用户中心点击设置的次数
    public static final String CODE_USER_CENTER_SETTINGS_C = "004";
    // 用户中心进入用户登录页面的次数
    public static final String CODE_USER_CENTER_LOGIN_ENTER_C = "005";
    // 他人主页中点击用户资料的次数
    public static final String CODE_USER_CENTER_OTHRE_USER_INFO_C = "006";
    // 用户点击注册登录按钮的次数
    public static final String CODE_USER_CENTER_LOGIN_PRESS_C = "007";

    //05 SEARCH
    // 进入搜索未搜索就退出的次数
    public static final String CODE_SEARCH_CANCEL_WITHOUT_SEARCH_C = "001";
    // 进入搜索搜索完成的次数
    public static final String CODE_SEARCH_FINISH_C = "002";

    //06 DESTINATION
    // 进入目的地页面的次数
    public static final String CODE_DESTINATION_ENTER_C = "001";
    // 目的地页面点击发帖的次数
    public static final String CODE_DESTINATION_POSTING_C = "002";
    // 目的的页面点击头像的次数
    public static final String CODE_DESTINATION_HEADICON_C = "003";
    // 目的的页面点击下一页的次数
    public static final String CODE_DESTINATION_NEXT_PAGE_C = "004";
    // 目的的页面点击预览帖子图片的次数
    public static final String CODE_DESTINATION_PHOTO_C = "005";

    //07 COMMUNICATION
    // 用户约行的次数
    public static final String CODE_COMMUNICATION_INVITATION_C = "001";
    // 用户进入消息页的次数
    public static final String CODE_COMMUNICATION_MESSAGE_ENTER_C = "002";
    // 用户在消息页点击头像的次数
    public static final String CODE_COMMUNICATION_MESSAGE_HEADICON_C = "003";

    private static final String STAT_ENABLE = "stat_enable"; //是否允许长期性打点
    private static final String STAT_RATE = "stat_rate";  //长期性数据打点的抽样比例
    private static final String STAGE_STAT_ENABLE = "stage_stat_enable"; //是否允许阶段性性打点

    /**
     * 累加指定功能模块使用的次数。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如流量监控 =10001
     */
    public static void logFull(Context c, String functionCode) {
        log(c, functionCode, null, null, true);
    }

    /**
     * 累加指定功能模块使用的次数。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如流量监控 =10001
     */
    public static void log(Context c, String functionCode) {
        log(c, functionCode, null);
    }

    /**
     * 累加指定功能模块使用的次数。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如流量监控 =10001
     * @param expireDate   过期时间，过期的打点不再记录。
     */
    public static void log(Context c, String functionCode, Date expireDate) {
        log(c, functionCode, null, expireDate);
    }

    /**
     * 累加指定功能模块使用的次数。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如流量监控 =10001
     * @param beginDate    开始时间，在开始时间之前的的打点不再记录。
     * @param endDate      结束时间，在结束时间之后的的打点不再记录。
     */
    public static void log(Context c, String functionCode, Date beginDate, Date endDate) {
        log(c, functionCode, beginDate, endDate, false);
    }

    /**
     * 累加指定功能模块使用的次数。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如流量监控 =10001
     * @param beginDate    开始时间，在开始时间之前的的打点不再记录。
     * @param endDate      结束时间，在结束时间之后的的打点不再记录。
     * @param isFull       是否全量打点。
     */
    public static void log(Context c, String functionCode, Date beginDate, Date endDate, boolean isFull) {
        boolean isStage = false;
        if (endDate != null || beginDate != null) {
            isStage = true;
        }

        boolean isExpired = false;
        if (isStage) {
            if (!isStageStatEnabled(c)) {
                return;
            }

            Date current = new Date();
            boolean bf = false;
            if (beginDate != null) {
                bf = current.before(beginDate);
            }
            boolean af = false;
            if (endDate != null) {
                af = current.after(endDate);
            }
            isExpired = bf || af;
        } else {
            if (!isStatEnabled(c)) {
                return;
            }
        }

        if (!isExpired) {
            String key = getSharedPrefKey(functionCode);
            int value = StatSharedPref.getInstance().getInt(key, 0) + 1;
            StatSharedPref.getInstance().setInt(key, value);
        }
    }

    /**
     * 记录指定功能模块的设置或数据。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如骚扰拦截黑名单 =03002
     */
    public static void logFull(Context c, String functionCode, int value) {
        log(c, functionCode, value, null, null, true);
    }

    /**
     * 记录指定功能模块的设置或数据。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如骚扰拦截黑名单 =03002
     */
    public static void log(Context c, String functionCode, int value) {
        log(c, functionCode, value, null);
    }

    /**
     * 记录指定功能模块的设置或数据。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如骚扰拦截黑名单 =03002
     * @param expireDate   过期时间，过期的打点不再记录。
     */
    public static void log(Context c, String functionCode, int value, Date expireDate) {
        log(c, functionCode, value, null, expireDate);
    }

    /**
     * 记录指定功能模块的设置或数据。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如骚扰拦截黑名单 =03002
     * @param beginDate    开始时间，在开始时间之前的的打点不再记录。
     * @param endDate      结束时间，在结束时间之后的的打点不再记录。
     */
    public static void log(Context c, String functionCode, int value, Date beginDate, Date endDate) {
        log(c, functionCode, value, beginDate, endDate, false);
    }

    /**
     * 记录指定功能模块的设置或数据。保存的Key=功能模块代码
     *
     * @param c            Context
     * @param functionCode 功能代码，例如骚扰拦截黑名单 =03002
     * @param beginDate    开始时间，在开始时间之前的的打点不再记录。
     * @param endDate      结束时间，在结束时间之后的的打点不再记录。
     * @param isFull       是否全量打点。
     */
    public static void log(Context c, String functionCode, int value, Date beginDate, Date endDate, boolean isFull) {
        boolean isStage = false; //是否是阶段性打点
        if (endDate != null || beginDate != null) {
            //有开始时间或结束时间，则是阶段性打点
            isStage = true;
        }

        boolean isExpired = false; //是否过期
        if (isStage) {
            if (!isStageStatEnabled(c)) {
                return;
            }

            Date current = new Date();
            boolean bf = false;
            if (beginDate != null) {
                bf = current.before(beginDate);
            }
            boolean af = false;
            if (endDate != null) {
                af = current.after(endDate);
            }
            isExpired = bf || af;
        } else {
            if (!isStatEnabled(c)) {
                return;
            }
        }

        if (!isExpired) {
            String key = getSharedPrefKey(functionCode);
            StatSharedPref.getInstance().setInt(key, value);
        }
    }

    /**
     * 获取打点数据，并返回拼装好的URL参数
     *
     * @param c        Context
     * @param moduleId 指定上传该模块的功能打点数据。当为null时，上传所有模块的打点数据。
     * @return 拼装好的URL数据，如果没有打点数据则返回空字符串。
     */
    public static String getReportStr(Context c, String moduleId) {
        if (TextUtils.isEmpty(moduleId)) {
            ArrayList<String> d = null;
            return getReportStr(c, d);
        } else {
            ArrayList<String> d = new ArrayList<String>();
            d.add(moduleId);
            return getReportStr(c, d);
        }
    }

    /**
     * 获取打点数据，并返回拼装好的URL参数
     *
     * @param c         Context
     * @param moduleIds 指定上传该模块的功能打点数据。当为null时，上传所有模块的打点数据。
     * @return 拼装好的URL数据，如果没有打点数据则返回空字符串。
     */
    public static String getReportStr(Context c, ArrayList<String> moduleIds) {
        if (!isStatEnabled(c))
            return "";

        String data = getData(c, moduleIds);
        if ("".equals(data))
            return "";
        return data;
    }

    /**
     * 重置所有的统计数据
     *
     * @param c        Context
     * @param moduleId 指定清除该模块的功能打点数据。当为null时，清除所有模块的打点数据
     */
    public static void resetStatistics(Context c, String moduleId) {
        if (TextUtils.isEmpty(moduleId))
            resetStatistics(c, (String[]) null);
        else
            resetStatistics(c, new String[]{moduleId});
    }

    /**
     * 重置所有的统计数据
     *
     * @param c         Context
     * @param moduleIds 指定清除该模块的功能打点数据。当为null时，清除所有模块的打点数据
     */
    public static void resetStatistics(Context c, String[] moduleIds) {
        String keys[] = StatSharedPref.getInstance().getKeys();
        if (keys != null) {
            for (String key : keys) {
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                if (moduleIds != null) {
                    boolean bFind = false;
                    for (String moduleId : moduleIds) {
                        if (key.startsWith(moduleId)) {
                            bFind = true;
                            break;
                        }
                    }
                    if (!bFind) {
                        continue;
                    }
                }
                StatSharedPref.getInstance().removeKey(getSharedPrefKey(key));
            }
        }
    }

    private static String getData(Context context, ArrayList<String> moduleIds) {
        StringBuilder sb = new StringBuilder();
        String[] keys = StatSharedPref.getInstance().getKeys();
        if (keys != null) {
            for (String key : keys) {
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                if (moduleIds != null) {
                    boolean bFind = false;
                    for (String moduleId : moduleIds) {
                        if (key.startsWith(moduleId)) {
                            bFind = true;
                            break;
                        }
                    }
                    if (!bFind) {
                        continue;
                    }
                }
                int n = StatSharedPref.getInstance().getInt(getSharedPrefKey(key), 0);
                if (n > 0) {
                    if (sb.length() > 0) {
                        sb.append(".");
                    }
                    // 根据服务端的提议，只有有值的，才上传，以节省带宽和服务器存储空间
                    sb.append(String.format("%s%d", key, n));
                }
            }
        }
        return sb.toString();
    }

    private static String getSharedPrefKey(String functionCode) {
        return String.format("%s", functionCode);
    }

    /***************************** 长期性打点 **********************************/
    /**
     * 是否允许打点操作。
     * 目前的策略是，如果用户没有加入“用户体验计划”，则任何打点操作都不能做。
     * 打点还有抽样比例，如果未命中抽样比例，则不进行打点。
     *
     * @param context
     * @return 如果允许返回true
     */
    public static boolean isStatEnabled(Context context) {
        if (Env.DEBUG) {
            return true;
        }
        boolean bStatEnabled = StatSharedPref.getInstance().getBoolean(STAT_ENABLE, true);
        return bStatEnabled;
    }

    private static void setStatEnabled(Context context, boolean value) {
        StatSharedPref.getInstance().setBoolean(STAT_ENABLE, value);
    }

    private static void setStatRate(Context context, int value) {
        StatSharedPref.getInstance().setInt(STAT_RATE, value);
    }

    private static int getStatRate(Context context) {
        return StatSharedPref.getInstance().getInt(STAT_RATE, 0);
    }

    private static void clearAll() {
        StatSharedPref.getInstance().clearAll();
    }

    /**
     * 检查上传打点数据的条件
     *
     * @return 是否该上传 true：该上传  false:不上传
     */
    private static boolean checkUploadStatisticsData() {
        if (SharedPref.getInstance().contains(SharedPref.KEY_UPLOAD_STATISTICS_DATA_TIME)) {
            long lastTime = SharedPref.getInstance().getLong(SharedPref.KEY_UPLOAD_STATISTICS_DATA_TIME, 0);
            Calendar cLast = Calendar.getInstance();
            cLast.setTimeInMillis(lastTime);
            Calendar now = Calendar.getInstance();
            long nowTime = now.getTimeInMillis();
            long hours = (nowTime - lastTime) / 3600000;
            if (hours >= 24) {
                return true;
            } else if (hours < -24) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                SharedPref.getInstance().setLong(SharedPref.KEY_UPLOAD_STATISTICS_DATA_TIME, c.getTimeInMillis());
            }
        } else {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, -1);
            SharedPref.getInstance().setLong(SharedPref.KEY_UPLOAD_STATISTICS_DATA_TIME, c.getTimeInMillis());
        }
        return false;
    }

    public static void uploadStatisticsData() {
        // 检查上传打点数据的条件
        if (checkUploadStatisticsData()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Request r = RequestBuilder.buildUploadStatisticsRequest(Statistics.getReportStr(TourPalApplication.getAppContext(), (String) null));
                    MessageProtos.ResponseInfo resInfo = r.post();
                    if (resInfo != null) {
                        if (resInfo.getErrCode() == MessageProtos.SUCCESS) {
                            clearAll();
                        }
                    }
                }
            }).start();
        }
    }

    /***************************** 阶段性打点 **********************************/
    /**
     * 是否允许阶段性打点操作。
     * 目前的策略是，如果用户没有加入“用户体验计划”，则任何打点操作都不能做。
     * 打点还有抽样比例，如果未命中抽样比例，则不进行打点。
     *
     * @param context
     * @return 如果允许返回true
     */
    private static boolean isStageStatEnabled(Context context) {
        if (Env.DEBUG) {
            return true;
        }

        boolean bStatEnabled = StatSharedPref.getInstance().getBoolean(STAGE_STAT_ENABLE, true);
        return bStatEnabled;
    }

    private static void setStageStatEnabled(Context context, boolean value) {
        StatSharedPref.getInstance().setBoolean(STAGE_STAT_ENABLE, value);
    }
}
