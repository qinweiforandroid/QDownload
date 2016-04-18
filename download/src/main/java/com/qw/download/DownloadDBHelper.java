package com.qw.download;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 下载数据进度持久化
 * Created by qinwei on 2016/4/14 16:05
 * email:qinwei_it@163.com
 */
public class DownloadDBHelper extends SQLiteOpenHelper {

    public DownloadDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DownloadDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE DB_DOWNLOAD  (" +
                    "id  TEXT NOT NULL," +
                    "url  TEXT," +
                    "contentLength  INTEGER DEFAULT 0," +
                    "currentLength  INTEGER DEFAULT 0," +
                    "state  TEXT," +
                    "ranges  TEXT," +
                    "isSupportRange  INTEGER," +
                    "PRIMARY KEY (id)" +");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
