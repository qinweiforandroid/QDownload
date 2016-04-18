package com.qw.download;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by qinwei on 2016/4/14 16:58
 * email:qinwei_it@163.com
 */
public class DownloadDBController {
    private static final String TAG = "DownloadDBController";
    private static DownloadDBController mInstance;
    private DownloadDBHelper dbHelper;

    public DownloadDBController(Context context) {
        dbHelper = new DownloadDBHelper(context, "download.db", null, 1);
    }

    public static DownloadDBController getInstance(Context context) {
        synchronized (DownloadDBController.class) {
            if (mInstance == null) {
                mInstance = new DownloadDBController(context);
            }
            return mInstance;
        }
    }

    public SQLiteDatabase getDB() {
        return dbHelper.getReadableDatabase();
    }

    public boolean add(DownloadEntity e) {
        DLog.d(TAG, "add " + e.id);
        SQLiteDatabase db = getDB();
        ContentValues value = new ContentValues();
        value.put("id", e.id);
        value.put("url", e.url);
        value.put("contentLength", e.contentLength);
        value.put("currentLength", e.currentLength);
        value.put("state", e.state.name());
        value.put("ranges", new Gson().toJson(e.ranges));
        value.put("isSupportRange", e.isSupportRange);
        db.beginTransaction();
        long number = getDB().insert("DB_DOWNLOAD", null, value);
        db.setTransactionSuccessful();
        db.endTransaction();
        DLog.d(TAG, "add " + e.id + " " + number);
        return number > 0;
    }

    public boolean update(DownloadEntity e) {
        DLog.d(TAG, "update " + e.id);
        SQLiteDatabase db = getDB();
        ContentValues value = new ContentValues();
        value.put("id", e.id);
        value.put("url", e.url);
        value.put("contentLength", e.contentLength);
        value.put("currentLength", e.currentLength);
        value.put("state", e.state.name());
        value.put("ranges", new Gson().toJson(e.ranges));
        value.put("isSupportRange", e.isSupportRange);
        db.beginTransaction();
        long number = db.update("DB_DOWNLOAD", value, " id=?", new String[]{e.id});
        db.setTransactionSuccessful();
        db.endTransaction();
        DLog.d(TAG, "update " + e.id+" "+number);
        return number > 0;
    }

    public synchronized boolean addOrUpdate(DownloadEntity e) {
        if (exists(e.id)) {
            return update(e);
        } else {
            return add(e);
        }
    }

    public DownloadEntity findById(String id) {
        SQLiteDatabase db = getDB();
        DownloadEntity e = null;
        Cursor cursor = db.rawQuery("SELECT * from DB_DOWNLOAD WHERE id=?", new String[]{id});
        Gson gson = new Gson();
//        Cursor cursor = getDB().query("DB_DOWNLOAD", null, "id=?", new String[]{id}, null, null, null);
        while (cursor.moveToNext()) {
            if (e == null) {
                e = new DownloadEntity();
            }
            e.id = cursor.getString(cursor.getColumnIndex("id"));
            e.url = cursor.getString(cursor.getColumnIndex("url"));
            e.contentLength = cursor.getInt(cursor.getColumnIndex("contentLength"));
            e.currentLength = cursor.getInt(cursor.getColumnIndex("currentLength"));
            e.ranges = gson.fromJson(cursor.getString(cursor.getColumnIndex("ranges")), HashMap.class);
            e.isSupportRange = cursor.getInt(cursor.getColumnIndex("isSupportRange")) == 0 ? true : false;
            e.state = Enum.valueOf(DownloadEntity.State.class, cursor.getString(cursor.getColumnIndex("state")));
        }
        cursor.close();
        return e;
    }

    public boolean exists(String id) {
        SQLiteDatabase db = getDB();
        DownloadEntity e = null;
        Cursor cursor = db.rawQuery("SELECT * from DB_DOWNLOAD WHERE id=?", new String[]{id});
        while (cursor.moveToNext()) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
}
