package com.qw.download.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qw.download.DownloadEntry;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qinwei on 2016/4/14 16:58
 * email:qinwei_it@163.com
 */
public class DownloadDBManager {
    private static DownloadDBManager mInstance;
    private Gson gson;
    private DownloadDBHelper dbHelper;


    public DownloadDBManager(Context context) {
        dbHelper = new DownloadDBHelper(context, "download.db", null, 1);
        gson = new Gson();
    }

    public static DownloadDBManager getInstance() {
        return mInstance;
    }

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadDBManager(context);
            mInstance.getDB();
        }
    }

    private SQLiteDatabase getDB() {
        return dbHelper.getReadableDatabase();
    }

    public boolean add(DownloadEntry e) {
        ContentValues value = new ContentValues();
        value.put("id", e.id);
        value.put("url", e.url);
        value.put("contentLength", e.contentLength);
        value.put("currentLength", e.currentLength);
        value.put("state", e.state.name());
        value.put("ranges", gson.toJson(e.ranges));
        value.put("isSupportRange", e.isSupportRange() ? 0 : 1);
        long number = getDB().insert(DownloadDBHelper.DB_TABLE, null, value);
        return number > 0;
    }

    public boolean update(DownloadEntry d) {
        ContentValues value = new ContentValues();
        value.put("id", d.id);
        value.put("url", d.url);
        value.put("contentLength", d.contentLength);
        value.put("currentLength", d.currentLength);
        value.put("state", d.state.name());
        value.put("ranges", gson.toJson(d.ranges));
        value.put("isSupportRange", d.isSupportRange() ? 0 : 1);
        long number = getDB().update(DownloadDBHelper.DB_TABLE, value, " id=?", new String[]{d.id});
        return number > 0;
    }

    public synchronized boolean newOrUpdate(DownloadEntry d) {
        if (exists(d.id)) {
            return update(d);
        } else {
            return add(d);
        }
    }

    @SuppressLint("Range")
    public DownloadEntry queryById(String id) {
        DownloadEntry entry = null;
        Cursor cursor = getDB().rawQuery("SELECT * from " + DownloadDBHelper.DB_TABLE + " WHERE id=?", new String[]{id});
        while (cursor.moveToNext()) {
            entry = DownloadEntry.obtain(cursor.getString(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("url")));
            entry.contentLength = cursor.getInt(cursor.getColumnIndex("contentLength"));
            entry.currentLength = cursor.getInt(cursor.getColumnIndex("currentLength"));
            entry.ranges = gson.fromJson(cursor.getString(cursor.getColumnIndex("ranges")), new TypeToken<HashMap<Integer, Long>>() {
            }.getType());
            entry.setSupportRange(cursor.getInt(cursor.getColumnIndex("isSupportRange")) == 0);
            entry.state = Enum.valueOf(DownloadEntry.State.class, cursor.getString(cursor.getColumnIndex("state")));
        }
        cursor.close();
        return entry;
    }

    public boolean exists(String id) {
        Cursor cursor = getDB().rawQuery("SELECT * from " + DownloadDBHelper.DB_TABLE + " WHERE id=?", new String[]{id});
        if (cursor.moveToNext()) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    @SuppressLint("Range")
    public ArrayList<DownloadEntry> queryAll() {
        ArrayList<DownloadEntry> es = new ArrayList<>();
        DownloadEntry entry;
        Cursor cursor = getDB().rawQuery("SELECT * from " + DownloadDBHelper.DB_TABLE, null);
        while (cursor.moveToNext()) {
            entry = DownloadEntry.obtain(cursor.getString(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("url")));
            entry.id = cursor.getString(cursor.getColumnIndex("id"));
            entry.url = cursor.getString(cursor.getColumnIndex("url"));
            entry.contentLength = cursor.getInt(cursor.getColumnIndex("contentLength"));
            entry.currentLength = cursor.getInt(cursor.getColumnIndex("currentLength"));
            entry.ranges = gson.fromJson(cursor.getString(cursor.getColumnIndex("ranges")), new TypeToken<HashMap<Integer, Long>>() {
            }.getType());
            entry.setSupportRange(cursor.getInt(cursor.getColumnIndex("isSupportRange")) == 0);
            entry.state = Enum.valueOf(DownloadEntry.State.class, cursor.getString(cursor.getColumnIndex("state")));
            es.add(entry);
        }
        cursor.close();
        return es;
    }

    public boolean delete(DownloadEntry e) {
        long number = getDB().delete(DownloadDBHelper.DB_TABLE, "id=?", new String[]{e.id});
        return number > 0;
    }

    public boolean delete(ArrayList<DownloadEntry> es) {
        String[] ids = new String[es.size()];
        for (int i = 0; i < es.size(); i++) {
            ids[i] = es.get(i).id;
        }
        long number = getDB().delete(DownloadDBHelper.DB_TABLE, "id=?", ids);
        return number > 0;
    }

    public boolean deleteAll() {
        long number = getDB().delete(DownloadDBHelper.DB_TABLE, null, null);
        return number > 0;
    }

    public static class DownloadDBHelper extends SQLiteOpenHelper {
        public final static String DB_TABLE = "tb_download";

        public DownloadDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public DownloadDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
            super(context, name, factory, version, errorHandler);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE " + DB_TABLE + "  (" +
                        "id  TEXT NOT NULL," +
                        "url  TEXT," +
                        "contentLength  INTEGER DEFAULT 0," +
                        "currentLength  INTEGER DEFAULT 0," +
                        "state  TEXT," +
                        "ranges  TEXT," +
                        "isSupportRange  INTEGER," +
                        "PRIMARY KEY (id)" + ");");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}