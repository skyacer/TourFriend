package com.elong.tourpal.search;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.db.DBManagerClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhitao.xu on 2015/4/16.
 */
public class DestinationDataManager {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = DestinationDataManager.class.getSimpleName();
    public static final String DESTINATION_DATA_FILE_NAME = "dest_0.db";

    /**
     * 初次安装应用时读取目的地文件
     *
     * @param context
     * @return
     */
    public static List<DestinationOrigData> loadDesDataFile(Context context) {
        // TODO
        // 何时初始化
        AssetManager am = context.getAssets();
        InputStream is = null;

        try {
            is = am.open(DESTINATION_DATA_FILE_NAME);
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "e:", e);
            }
            return null;
        }
        List<DestinationOrigData> desList = DestinationDataManager.getSortedDesData(is);
        return desList;
    }

    /**
     * 更新目的地文件数据到数据库
     *
     * @param file
     * @return
     */
    public static boolean updateDestinationData(File file) {
        FileInputStream fis = null;
        boolean success = false;

        try {
            fis = new FileInputStream(file);
            List<DestinationOrigData> desList = DestinationDataManager.getSortedDesData(fis);
            boolean isSuccess = DBManagerClient.updateAllDesData(desList);
            success = true;
        } catch (FileNotFoundException e) {
            if (DEBUG) {
                Log.e(TAG, "e:", e);
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "e:", e);
                    }
                }
            }
        }
        return success;
    }

    private static List<DestinationOrigData> getSortedDesData(InputStream is) {
        List<DestinationOrigData> desDatas = getDesData(is);
        return desDatas;
    }

    private static List<DestinationOrigData> getDesData(InputStream is) {
        List<DestinationOrigData> cityDatas = new ArrayList<DestinationOrigData>();
        InputStreamReader ir = null;
        BufferedReader br = null;
        try {
            ir = new InputStreamReader(is);
            br = new BufferedReader(ir);

            String lineStr = null;
            int i = 1;
            boolean add = false;
            while ((lineStr = br.readLine()) != null) {
                if (!TextUtils.isEmpty(lineStr.trim())) {
                    if (lineStr.endsWith(":")) {
                        continue;
                    } else {
                        String[] data = lineStr.split("#");
                        if (data.length == 7) {
                            DestinationOrigData d = new DestinationOrigData();
                            d.mDesId = Long.valueOf(data[0]);
                            d.mParentId = Long.valueOf(data[1]);
                            d.mDesName = data[2];
                            d.mDesPY = data[3];
                            d.mDesJP = data[4];
                            d.mDesPath = data[5];
                            d.mLevel = Integer.valueOf(data[6]);
                            cityDatas.add(d);
                            add = true;
                        }

                    }
                }
                if (add == false) {
                    if (DEBUG) {
                        Log.e(TAG, "error line " + i);
                    }
                }
                i++;
                add = false;
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "e:", e);
            }
        } finally {
            try {
                br.close();
                ir.close();
            } catch (IOException e) {
                if (DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
        }

        return cityDatas;
    }

    public static void writeToFileWithSortLevel(List<DestinationOrigData> desDatas) {
        if (DEBUG) {
            Log.e(TAG, "size:" + desDatas.size());
        }
        List<DestinationOrigData> level1 = new ArrayList<DestinationOrigData>();
        List<DestinationOrigData> level2 = new ArrayList<DestinationOrigData>();
        List<DestinationOrigData> level3 = new ArrayList<DestinationOrigData>();
        List<DestinationOrigData> level4 = new ArrayList<DestinationOrigData>();

        for (DestinationOrigData c : desDatas) {
            switch (c.mLevel) {
                case 1:
                    level1.add(c);
                    break;
                case 2:
                    level2.add(c);
                    break;
                case 3:
                    level3.add(c);
                    break;
                default:
                    level4.add(c);
                    break;
            }
        }

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + DESTINATION_DATA_FILE_NAME;
        File file = new File(path);
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos);

            for (DestinationOrigData c : level1) {
                String desPath = (c.mDesPath == null) ? "-" : c.mDesPath;
                osw.write(c.mDesId + " " + c.mDesName + " " + c.mDesPY + " " + c.mDesJP + " " + desPath + " " + c.mLevel);
                osw.write("\n");
            }

            for (DestinationOrigData c : level2) {
                String desPath = (c.mDesPath == null) ? "-" : c.mDesPath;
                osw.write(c.mDesId + " " + c.mDesName + " " + c.mDesPY + " " + c.mDesJP + " " + desPath + " " + c.mLevel);
                osw.write("\n");
            }

            for (DestinationOrigData c : level3) {
                String desPath = (c.mDesPath == null) ? "-" : c.mDesPath;
                osw.write(c.mDesId + " " + c.mDesName + " " + c.mDesPY + " " + c.mDesJP + " " + desPath + " " + c.mLevel);
                osw.write("\n");
            }

            for (DestinationOrigData c : level4) {
                String desPath = (c.mDesPath == null) ? "-" : c.mDesPath;
                osw.write(c.mDesId + " " + c.mDesName + " " + c.mDesPY + " " + c.mDesJP + " " + desPath + " " + c.mLevel);
                osw.write("\n");
            }
        } catch (FileNotFoundException e) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        } catch (IOException e) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    if (Env.DEBUG) {
                        Log.e(TAG, "e:", e);
                    }
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    if (Env.DEBUG) {
                        Log.e(TAG, "e:", e);
                    }
                }
            }
        }

    }

    public static class DestinationOrigData implements Cloneable, Serializable {
        public long mDesId;
        public long mParentId;
        public String mDesName;
        public String mDesPY;
        public String mDesJP;
        public String mDesPath;
        public ArrayList<DestinationOrigData> mGrandparents;
        public int mLevel;

        @Override
        protected DestinationOrigData clone() {
            DestinationOrigData o = null;
            try {
                o = (DestinationOrigData) super.clone();
            } catch (CloneNotSupportedException e) {
                if (Env.DEBUG) {
                    Log.e(TAG, "e:", e);
                }
            }
            return o;
        }

        @Override
        public String toString() {
            StringBuilder grandparentsStr = new StringBuilder("grandparentsStr=");

            if (mGrandparents != null) {
                for (DestinationOrigData d : mGrandparents) {
                    grandparentsStr.append(d.toString());
                    grandparentsStr.append(", ");
                }
            } else {
                grandparentsStr.append("null");
            }

            return "desId=" + mDesId + ", desName=" + mDesName + ", desPY=" + mDesPY
                    + ", desJP=" + mDesJP + grandparentsStr;
        }
    }
}
