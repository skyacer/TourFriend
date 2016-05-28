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

    private static final String CREATE_DESTINATION_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_DESTINATION
            + " (" + COL_SELF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DestinationTableColumn.COL_DES_ID + " INTEGER NOT NULL, " + DestinationTableColumn.COL_DES + " TEXT, "
            + DestinationTableColumn.COL_DES_PY + " TEXT, " + DestinationTableColumn.COL_DES_JP + " TEXT, "
            + DestinationTableColumn.COL_DES_PATH + " TEXT, " + DestinationTableColumn.COL_LEVEL + " INTEGER);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DESTINATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
}
