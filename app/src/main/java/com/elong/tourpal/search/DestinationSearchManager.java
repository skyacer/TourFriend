package com.elong.tourpal.search;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.db.DBManagerClient;
import com.elong.tourpal.utils.SharedPref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhitao.xu on 2015/04/16.
 */
public class DestinationSearchManager {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = DestinationSearchManager.class.getSimpleName();
    private static DestinationSearchManager mInstance;
    private List<DestinationDataManager.DestinationOrigData> mDesList = new ArrayList<DestinationDataManager.DestinationOrigData>();
    private List<DestinationDataManager.DestinationOrigData> mDesParentList = new ArrayList<DestinationDataManager.DestinationOrigData>();
    private List<DestinationDataManager.DestinationOrigData> mDesGrandpaList = new ArrayList<DestinationDataManager.DestinationOrigData>();
    private List<DestinationDataManager.DestinationOrigData> mDesSearchList;

    public static DestinationSearchManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DestinationSearchManager(context);
        }
        return mInstance;
    }

    public static void forceUpdateDesList() {
        Context context = TourPalApplication.getAppContext();
        mInstance = new DestinationSearchManager(context);
    }

    private DestinationSearchManager(final Context context) {
        if (DEBUG) {
            Log.d(TAG, "DestinationSearchManager()");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DestinationDataManager.DestinationOrigData> desList;
                long time1 = System.currentTimeMillis();
                if (DEBUG) {
                    Log.d(TAG, "" + time1);
                }
                if (SharedPref.getInstance().getBoolean(SharedPref.KEY_SC_LOAD_DESTINATION_FLAG, false)) {
                    // 已经初始化目的地数据，检查是否有新版本目的地数据文件已下载
                    checkHasNewDestinationFileUpdate();
                    desList = DBManagerClient.queryAllDesData();
                } else {
                    // 未初始化就初始化
                    desList = DestinationDataManager.loadDesDataFile(context);
                    int insertCount = DBManagerClient.insertAllDesData(desList);

                    if (insertCount == desList.size()) {
                        SharedPref.getInstance().setBoolean(SharedPref.KEY_SC_LOAD_DESTINATION_FLAG, true);
                    }
                    if (DEBUG) {
                        Log.d(TAG, "" + (System.currentTimeMillis() - time1));
                        Log.d(TAG, "data size=" + desList.size() + "  insertSize =" + insertCount);
                    }
                }
                mDesSearchList = desList;
            }
        }).start();
    }

    private void checkHasNewDestinationFileUpdate() {
        if (DEBUG) {
            Log.d(TAG, "checkHasNewDestinationFileUpdate()");
        }
        // 无下载更新
        String lastDownFile = SharedPref.getInstance().getString(SharedPref.KEY_LAST_DOWNLOAD_DESTINATION_FILE, DestinationDataManager.DESTINATION_DATA_FILE_NAME);
        String lastUpdateFile = SharedPref.getInstance().getString(SharedPref.KEY_LAST_UPDATE_DESTINATION_FILE, DestinationDataManager.DESTINATION_DATA_FILE_NAME);
        // 检查是否有已下载未更新的文件
        if (lastDownFile.compareTo(lastUpdateFile) > 0) {
            //有新文件没更新
            File[] files = TourPalApplication.getAppContext().getFilesDir().listFiles();
            for (File f : files) {
                if (f.getName().equals(lastDownFile)) {
                    updateDestinationDataFromFile(f);
                    break;
                }
            }
        }
    }


    public static void updateDestinationDataFromFile(File newFile) {
        if (DEBUG) {
            Log.d(TAG, "updateDestinationDataFromFile(), update new file,filename:" + newFile.getName());
        }
        // 把新下载的文件更新到数据库
        boolean updateSuccess = DestinationDataManager.updateDestinationData(newFile);
        if (updateSuccess) {
            SharedPref.getInstance().setString(SharedPref.KEY_LAST_UPDATE_DESTINATION_FILE, newFile.getName());
            // 目的地数据更新后强制更新内存数据
            DestinationSearchManager.forceUpdateDesList();
            if (DEBUG) {
                Log.d(TAG, "updateDestinationDataFromFile(), update new file success");
            }
            // 更新成功删除文件
            newFile.delete();
        }
    }

    public List<DestinationDataManager.DestinationOrigData> search(String input) {
        List<DestinationDataManager.DestinationOrigData> desHits = new ArrayList<DestinationDataManager.DestinationOrigData>();
        List<DestinationDataManager.DestinationOrigData> desHitsPY = new ArrayList<DestinationDataManager.DestinationOrigData>();
        List<DestinationDataManager.DestinationOrigData> desHitsJP = new ArrayList<DestinationDataManager.DestinationOrigData>();

        // 避免输入大写字符
        input = input.toLowerCase();
        if (TextUtils.isEmpty(input) || mDesSearchList == null) {
            return desHits;
        }

        int size = mDesSearchList.size();
        if (DEBUG) {
            Log.e(TAG, "des size=" + size);
        }
        for (int i = 0; i < size; i++) {
            DestinationDataManager.DestinationOrigData c = mDesSearchList.get(i);
            char[] a = input.toCharArray();
            if (a.length > 0 && isChinese(a[0])) {
                if (searchMatch(c.mDesName, input)) {
                    desHits.add(c);
                }
            } else if (searchMatch(c.mDesPY, input)) {
                desHitsPY.add(c);
            } else if (searchMatch(c.mDesJP, input)) {
                desHitsJP.add(c);
            }
        }

        if (desHitsPY.size() > 0) {
            desHits.addAll(desHitsPY);
        }
        if (desHitsJP.size() > 0) {
            desHits.addAll(desHitsJP);
        }

        desHits = findDesParent(desHits);
        return desHits;
    }

    private boolean searchMatch(String matcher, String input) {
        if (matcher != null && input != null) {
            if (matcher.startsWith(input)) {
                return true;
            }
        }
        return false;
    }

    private List<DestinationDataManager.DestinationOrigData> findDesParent(List<DestinationDataManager.DestinationOrigData> sourceDes) {
        for (DestinationDataManager.DestinationOrigData d : sourceDes) {
            String[] ids = d.mDesPath.split("_");
            long pId = Long.MIN_VALUE;
            if (ids.length >= 2) {
                pId = Long.valueOf(ids[ids.length - 2]);
                if (DEBUG) {
                    Log.e(TAG, "gId=" + pId);
                }
            }
            for (DestinationDataManager.DestinationOrigData d1 : mDesSearchList) {
//                if (d1.mDesPath.matches("" + pId + "_\\d+$")) {
                if (d1.mDesId == pId) {
                    // 查找的隶属父亲
                    if (d.mGrandparents == null) {
                        d.mGrandparents = new ArrayList<DestinationDataManager.DestinationOrigData>();
                    }
                    if (DEBUG) {
                        Log.d(TAG, "parent=" + d1.toString());
                    }
                    d.mGrandparents.add(d1);
                    break;
                }
            }
        }
        return sourceDes;
    }

    private boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    public DestinationDataManager.DestinationOrigData findDestinationDataByName(String desName) {
        if (mDesSearchList != null) {
            for (DestinationDataManager.DestinationOrigData d : mDesSearchList) {
                if (desName.equals(d.mDesName)) {
                    return d;
                }
            }
        }
        return null;
    }
}
