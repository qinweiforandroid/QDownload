package com.qw.download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qw.download.DownloadEntry;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qinwei on 2016/4/14 16:58
 * email:qinwei_it@163.com
 */
public class DownloadDBController {
    private static DownloadDBController mInstance;
    private Gson gson;
    private DownloadDBHelper dbHelper;

    public DownloadDBController(Context context) {
        dbHelper = new DownloadDBHelper(context, "download.db", null, 1);
        gson = new Gson();
    }

    public static DownloadDBController getInstance() {
        return mInstance;
    }

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadDBController(context);
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
        value.put("isSupportRange", e.isSupportRange ? 0 : 1);
        long number = getDB().insert("DB_DOWNLOAD", null, value);
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
        value.put("isSupportRange", d.isSupportRange ? 0 : 1);
        long number = getDB().update("DB_DOWNLOAD", value, " id=?", new String[]{d.id});
        return number > 0;
    }

    public synchronized boolean newOrUpdate(DownloadEntry d) {
        if (exists(d.id)) {
            return update(d);
        } else {
            return add(d);
        }
    }

    public DownloadEntry findById(String id) {
        DownloadEntry e = null;
        Cursor cursor = getDB().rawQuery("SELECT * from DB_DOWNLOAD WHERE id=?", new String[]{id});
        while (cursor.moveToNext()) {
            if (e == null) {
                e = new DownloadEntry();
            }
            e.id = cursor.getString(cursor.getColumnIndex("id"));
            e.url = cursor.getString(cursor.getColumnIndex("url"));
            e.contentLength = cursor.getInt(cursor.getColumnIndex("contentLength"));
            e.currentLength = cursor.getInt(cursor.getColumnIndex("currentLength"));
            e.ranges = gson.fromJson(cursor.getString(cursor.getColumnIndex("ranges")), new TypeToken<HashMap<Integer, Long>>() {
            }.getType());
            e.isSupportRange = cursor.getInt(cursor.getColumnIndex("isSupportRange")) == 0;
            e.state = Enum.valueOf(DownloadEntry.State.class, cursor.getString(cursor.getColumnIndex("state")));
        }
        cursor.close();
        return e;
    }

    public boolean exists(String id) {
        Cursor cursor = getDB().rawQuery("SELECT * from DB_DOWNLOAD WHERE id=?", new String[]{id});
        if (cursor.moveToNext()) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public ArrayList<DownloadEntry> queryAll() {
        ArrayList<DownloadEntry> es = new ArrayList<>();
        DownloadEntry e = null;
        Cursor cursor = getDB().rawQuery("SELECT * from DB_DOWNLOAD", null);
        while (cursor.moveToNext()) {
            e = new DownloadEntry();
            e.id = cursor.getString(cursor.getColumnIndex("id"));
            e.url = cursor.getString(cursor.getColumnIndex("url"));
            e.contentLength = cursor.getInt(cursor.getColumnIndex("contentLength"));
            e.currentLength = cursor.getInt(cursor.getColumnIndex("currentLength"));
            e.ranges = gson.fromJson(cursor.getString(cursor.getColumnIndex("ranges")), new TypeToken<HashMap<Integer, Long>>() {
            }.getType());
            e.isSupportRange = cursor.getInt(cursor.getColumnIndex("isSupportRange")) == 0;
            e.state = Enum.valueOf(DownloadEntry.State.class, cursor.getString(cursor.getColumnIndex("state")));
            es.add(e);
        }
        cursor.close();
        return es;
    }

    public boolean delete(DownloadEntry e) {
        long number = getDB().delete("DB_DOWNLOAD", "id=?", new String[]{e.id});
        return number > 0;
    }

    public boolean delete(ArrayList<DownloadEntry> es) {
        String[] ids = new String[es.size()];
        for (int i = 0; i < es.size(); i++) {
            ids[i] = es.get(i).id;
        }
        long number = getDB().delete("DB_DOWNLOAD", "id=?", ids);
        return number > 0;
    }

    public boolean deleteAll() {
        long number = getDB().delete("DB_DOWNLOAD", null, null);
        return number > 0;
    }
}
