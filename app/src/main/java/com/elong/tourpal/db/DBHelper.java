package com.elong.tourpal.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhitao.xu on 2015/2/10.
 */
class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "tourpal.db";
    private static final int DB_VERSION = 1;
    static final String COL_SELF_ID = "_id";
    static final String TABLE_NAME_DESTINATION = "destinations";
    static final String TABLE_NAME_TOURPOST = "tourpost";

    private static final String CREATE_DESTINATION_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_DESTINATION
            + " (" + COL_SELF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DestinationTableColumn.COL_DES_ID + " INTEGER NOT NULL, " + DestinationTableColumn.COL_DES + " TEXT, "
            + DestinationTableColumn.COL_DES_PY + " TEXT, " + DestinationTableColumn.COL_DES_JP + " TEXT, "
            + DestinationTableColumn.COL_DES_PATH + " TEXT, " + DestinationTableColumn.COL_LEVEL + " INTEGER);";

    private static final String CREATE_TOURPOST_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TOURPOST
            + " (" + COL_SELF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TourPostTableColum.COL_TOURPOST_ID + " INTEGER NOT NULL, " + TourPostTableColum.COL_TOURPOST_STARTTIME + " TEXT, "
            + TourPostTableColum.COL_TOURPOST_ENDTIME + " TEXT, " + TourPostTableColum.COL_TOURPOST_TAG + " TEXT, "
            + TourPostTableColum.COL_TOURPOST_WX + " TEXT, " + TourPostTableColum.COL_TOURPOST_QQ + " TEXT,"
            + TourPostTableColum.COL_TOURPOST_PHONE + " TEXT, " + TourPostTableColum.COL_TOURPOST_DETAIL + " TEXT);";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DESTINATION_TABLE);
        db.execSQL(CREATE_TOURPOST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
}
