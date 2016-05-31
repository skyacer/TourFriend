package com.elong.tourpal.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.model.TourPostData;
import com.elong.tourpal.search.DestinationDataManager;
import com.elong.tourpal.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhitao.xu on 2015/2/10.
 */
public class DBManagerClient {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = DBManagerClient.class.getSimpleName();

    public static int insertAllDesData(List<DestinationDataManager.DestinationOrigData> data) {
        if(data == null) {
            return -1;
        }

        int count = 0;
        long time1 = System.currentTimeMillis();
        if (DEBUG) {
            Log.w(TAG, "time1:" + time1);
        }
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        for (DestinationDataManager.DestinationOrigData d : data) {
            ContentValues values = new ContentValues();
            values.put(DestinationTableColumn.COL_DES_ID, d.mDesId);
            values.put(DestinationTableColumn.COL_DES, d.mDesName);
            values.put(DestinationTableColumn.COL_DES_PY, d.mDesPY);
            values.put(DestinationTableColumn.COL_DES_JP, d.mDesJP);
            values.put(DestinationTableColumn.COL_DES_PATH, d.mDesPath);
            values.put(DestinationTableColumn.COL_LEVEL, d.mLevel);
            valuesList.add(values);
            count++;
        }

        DBManager.getInstance().batchInsert(DBHelper.TABLE_NAME_DESTINATION, null, valuesList);
        if (DEBUG) {
            Log.w(TAG, "time1:" + (System.currentTimeMillis() - time1));
        }
        if (DEBUG) {
            Log.d(TAG, "count=" + count);
        }
        return count;
    }

    /**
     * 插入旅程信息
     * @return
     */
    public static int insertTourPost(TourPostData data){
        if (data == null){
            return -1;
        }
        long time1 = System.currentTimeMillis();
        if (DEBUG) {
            Log.w(TAG, "time1:" + time1);
        }
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        ContentValues values = new ContentValues();
        if (data.mDestinationAndIds.get(0)!=null) {
            values.put(TourPostTableColum.COL_TOURPOST_ID, data.mDestinationAndIds.get(0));
        }
        values.put(TourPostTableColum.COL_TOURPOST_STARTTIME,sf.format(data.mStartTime.getTime()));
        values.put(TourPostTableColum.COL_TOURPOST_ENDTIME,sf.format(data.mEndTime.getTime()));
        if (data.mSelectTags.get(0)!=null) {
            values.put(TourPostTableColum.COL_TOURPOST_TAG, data.mSelectTags.get(0));
        }
        values.put(TourPostTableColum.COL_TOURPOST_WX,data.mWeixin);
        values.put(TourPostTableColum.COL_TOURPOST_QQ,data.mQQ);
        values.put(TourPostTableColum.COL_TOURPOST_PHONE,data.mPhone);
        values.put(TourPostTableColum.COL_TOURPOST_DETAIL,data.mDetail);
        valuesList.add(values);
        DBManager.getInstance().batchInsert(DBHelper.TABLE_NAME_TOURPOST,null,valuesList);

        if (DEBUG) {
            Log.w(TAG, "time1:" + (System.currentTimeMillis() - time1));
        }

        return 0;
    }



    public static boolean updateAllDesData(List<DestinationDataManager.DestinationOrigData> data) {
        if (data == null) {
            return false;
        }

        int count = 0;
        long time1 = System.currentTimeMillis();
        if (DEBUG) {
            Log.w(TAG, "time1:" + time1);
        }
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        for (DestinationDataManager.DestinationOrigData d : data) {
            ContentValues values = new ContentValues();
            values.put(DestinationTableColumn.COL_DES_ID, d.mDesId);
            values.put(DestinationTableColumn.COL_DES, d.mDesName);
            values.put(DestinationTableColumn.COL_DES_PY, d.mDesPY);
            values.put(DestinationTableColumn.COL_DES_JP, d.mDesJP);
            values.put(DestinationTableColumn.COL_DES_PATH, d.mDesPath);
            values.put(DestinationTableColumn.COL_LEVEL, d.mLevel);
            valuesList.add(values);
            count++;
        }

        long successCount = DBManager.getInstance().updateAllTable(DBHelper.TABLE_NAME_DESTINATION, null, valuesList);
        if (DEBUG) {
            Log.w(TAG, "time1:" + (System.currentTimeMillis() - time1));
        }
        if (DEBUG) {
            Log.d(TAG, "count=" + count);
        }
        return (count == successCount);
    }

    public static ArrayList<TourPostData> queryAllTourPostData(){
        String[] cloumns = new String[]{TourPostTableColum.COL_TOURPOST_ID,TourPostTableColum.COL_TOURPOST_STARTTIME,
        TourPostTableColum.COL_TOURPOST_ENDTIME,TourPostTableColum.COL_TOURPOST_TAG,
        TourPostTableColum.COL_TOURPOST_WX,TourPostTableColum.COL_TOURPOST_QQ,
        TourPostTableColum.COL_TOURPOST_PHONE,TourPostTableColum.COL_TOURPOST_DETAIL};
        Cursor c = DBManager.getInstance().select(DBHelper.TABLE_NAME_TOURPOST,cloumns,null,null,null,null,
                TourPostTableColum.COL_TOURPOST_ID);
        ArrayList<TourPostData> tourPostDataList = new ArrayList<>();

        while (c.moveToNext()){
            TourPostData data = new TourPostData();
            String id = c.getString(c.getColumnIndex(TourPostTableColum.COL_TOURPOST_ID));
            String startTime = c.getString(c.getColumnIndex(TourPostTableColum.COL_TOURPOST_STARTTIME));
            String endTime = c.getString(c.getColumnIndex(TourPostTableColum.COL_TOURPOST_ENDTIME));
            String tag = c.getString(c.getColumnIndex(TourPostTableColum.COL_TOURPOST_TAG));
            String wx = c.getString(c.getColumnIndex(TourPostTableColum.COL_TOURPOST_WX));
            String qq = c.getString(c.getColumnIndex(TourPostTableColum.COL_TOURPOST_QQ));
            String phone = c.getString(c.getColumnIndex(TourPostTableColum.COL_TOURPOST_PHONE));
            String detail = c.getString(c.getColumnIndex(TourPostTableColum.COL_TOURPOST_DETAIL));

            data.mDestinationAndIds.add(id);
            data.mSelectTags.add(tag);
            data.mStartTime = TimeUtils.stringToCalender(startTime);
            data.mEndTime = TimeUtils.stringToCalender(endTime);
            data.mWeixin = wx;
            data.mQQ = qq;
            data.mPhone = phone;
            data.mDetail = detail;
            tourPostDataList.add(data);
        }
        return tourPostDataList;
    }

    public static ArrayList<DestinationDataManager.DestinationOrigData> queryAllDesData() {
        String[] columns = new String[]{DestinationTableColumn.COL_DES_ID, DestinationTableColumn.COL_DES,
                DestinationTableColumn.COL_DES_PY, DestinationTableColumn.COL_DES_JP,
                DestinationTableColumn.COL_DES_PATH, DestinationTableColumn.COL_LEVEL};
        String sortStr = DestinationTableColumn.COL_LEVEL + " ASC";
        Cursor c = DBManager.getInstance().select(DBHelper.TABLE_NAME_DESTINATION, columns, null, null, null, null, sortStr);
        ArrayList<DestinationDataManager.DestinationOrigData> datas = new ArrayList<DestinationDataManager.DestinationOrigData>();
        while (c.moveToNext()) {
            DestinationDataManager.DestinationOrigData d = new DestinationDataManager.DestinationOrigData();
            int desId = c.getInt(c.getColumnIndex(DestinationTableColumn.COL_DES_ID));
            String des = c.getString(c.getColumnIndex(DestinationTableColumn.COL_DES));
            String desPY = c.getString(c.getColumnIndex(DestinationTableColumn.COL_DES_PY));
            String desJP = c.getString(c.getColumnIndex(DestinationTableColumn.COL_DES_JP));
            String desPath = c.getString(c.getColumnIndex(DestinationTableColumn.COL_DES_PATH));
            int level = c.getInt(c.getColumnIndex(DestinationTableColumn.COL_LEVEL));

            d.mDesId = desId;
            d.mDesName = des;
            d.mDesPY = desPY;
            d.mDesJP = desJP;
            d.mDesPath = desPath;
            d.mLevel = level;

            datas.add(d);
        }
        if (c != null) {
            c.close();
        }

        if (DEBUG) {
            Log.d(TAG, "loadDestinationData=" + datas.size());
        }
        return datas;
    }
}
