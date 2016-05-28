package com.elong.tourpal.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;

import java.util.List;

/**
 * Created by zhitao.xu on 2015/2/10.
 */
class DBManager {
    private static final String TAG = DBManager.class.getSimpleName();
    private static DBManager mInstance;
    private static DBHelper mDBHelper;

    private DBManager(Context context) {
        mDBHelper = new DBHelper(context);
    }

    public static DBManager getInstance() {
        if (mInstance == null) {
            mInstance = new DBManager(TourPalApplication.getAppContext());
        }
        return mInstance;
    }

    public synchronized int delete(String tableName, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int row = db.delete(tableName, whereClause, whereArgs);
        return row;
    }

    public synchronized Cursor select(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor c = db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy);
        return c;
    }

    public synchronized long insert(String tableName, String nullColumnHack, ContentValues values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        long row = db.insert(tableName, nullColumnHack, values);
        return row;
    }

    public synchronized long batchInsert(String tableName, String nullColumnHack, List<ContentValues> valuesList) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.beginTransaction();
        boolean hasError = false;
        int successRowCount = 0;
        for (ContentValues v : valuesList) {
            long row = db.insert(tableName, nullColumnHack, v);
            if (Env.DEBUG) {
                Log.d(TAG, "insert row=" + row);
            }
            if (row < 0) {
                if (Env.DEBUG) {
                    Log.e(TAG, v.toString());
                }
                hasError = true;
                successRowCount = 0;
                break;
            } else {
                successRowCount++;
            }
        }

        if (!hasError) {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
        return successRowCount;
    }

    public synchronized long updateAllTable(String tableName, String nullColumnHack, List<ContentValues> valuesList) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.beginTransaction();
        // 删除整个表数据
        db.delete(tableName, null, null);
        // 添加新数据
        boolean hasError = false;
        int successRowCount = 0;
        for (ContentValues v : valuesList) {
            long row = db.insert(tableName, nullColumnHack, v);
            if (Env.DEBUG) {
                Log.d(TAG, "insert row=" + row);
            }
            if (row < 0) {
                if (Env.DEBUG) {
                    Log.e(TAG, v.toString());
                }
                hasError = true;
                successRowCount = 0;
                break;
            } else {
                successRowCount++;
            }
        }

        if (!hasError) {
            db.setTransactionSuccessful();
        } else {
            return -1;
        }
        db.endTransaction();
        return successRowCount;
    }

    public synchronized int update(String tableName, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int row = db.update(tableName, values, where, whereArgs);
        return row;
    }

    public synchronized Cursor execSelectSQL(String selectSQL, String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        return db.rawQuery(selectSQL, selectionArgs);
    }
}
