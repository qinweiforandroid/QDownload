package com.qw.download.db.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qw.download.db.dto.DownloadEntry;
import com.qw.download.db.dto.DownloadState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qinwei on 2016/4/14 16:58
 * email:qinwei_it@163.com
 */
public class DownloadDB {
    private Gson gson;
    private DownloadDBHelper dbHelper;

    private static final int DB_VERSION = 2;

    public DownloadDB(Context context) {
        dbHelper = new DownloadDBHelper(context, "download.db", null, DB_VERSION);
        gson = new Gson();
        getDB();
    }

    private SQLiteDatabase getDB() {
        return dbHelper.getReadableDatabase();
    }

    public synchronized boolean add(DownloadEntry d) {
        long number = getDB().insert(DownloadDBHelper.DB_TABLE, null,
                toContentValues(d));
        return number > 0;
    }

    private ContentValues toContentValues(DownloadEntry d) {
        ContentValues value = new ContentValues();
        value.put("id", d.id);
        value.put("url", d.url);
        value.put("dir", d.getDir());
        value.put("name", d.getName());
        value.put("contentLength", d.contentLength);
        value.put("currentLength", d.currentLength);
        value.put("state", d.state.name());
        value.put("ranges", gson.toJson(d.ranges));
        value.put("isSupportRange", d.isRange() ? 0 : 1);
        return value;
    }

    public synchronized boolean update(DownloadEntry d) {
        long number = getDB().update(DownloadDBHelper.DB_TABLE, toContentValues(d),
                " id=?", new String[]{d.id});
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
    @Nullable
    public DownloadEntry queryById(String id) {
        DownloadEntry entry = null;
        Cursor cursor = getDB().rawQuery("SELECT * from " + DownloadDBHelper.DB_TABLE +
                " WHERE id=?", new String[]{id});
        while (cursor.moveToNext()) {
            entry = dto(cursor);
        }
        cursor.close();
        return entry;
    }

    @SuppressLint("Range")
    private DownloadEntry dto(Cursor cursor) {
        DownloadEntry entry = new DownloadEntry();
        entry.id = cursor.getString(cursor.getColumnIndex("id"));
        entry.url = cursor.getString(cursor.getColumnIndex("url"));

        entry.setDir(cursor.getString(cursor.getColumnIndex("dir")));
        entry.setName(cursor.getString(cursor.getColumnIndex("name")));

        entry.contentLength = cursor.getInt(cursor.getColumnIndex("contentLength"));
        entry.currentLength = cursor.getInt(cursor.getColumnIndex("currentLength"));
        entry.ranges = gson.fromJson(cursor.getString(cursor.getColumnIndex("ranges")),
                new TypeToken<HashMap<Integer, Long>>() {
                }.getType());
        entry.setRange(cursor.getInt(cursor.getColumnIndex("isSupportRange")) == 0);
        entry.state = Enum.valueOf(DownloadState.class,
                cursor.getString(cursor.getColumnIndex("state")));

        return entry;
    }

    public boolean exists(String id) {
        Cursor cursor = getDB().rawQuery("SELECT * from " + DownloadDBHelper.DB_TABLE +
                " WHERE id=?", new String[]{id});
        if (cursor.moveToNext()) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    @SuppressLint("Range")
    @NotNull
    public ArrayList<DownloadEntry> queryAll() {
        ArrayList<DownloadEntry> es = new ArrayList<>();
        Cursor cursor = getDB().rawQuery("SELECT * from " + DownloadDBHelper.DB_TABLE,
                null);
        while (cursor.moveToNext()) {
            es.add(dto(cursor));
        }
        cursor.close();
        return es;
    }

    public synchronized boolean delete(String id) {
        long number = getDB().delete(DownloadDBHelper.DB_TABLE, "id=?",
                new String[]{id});
        return number > 0;
    }

    public synchronized boolean delete(DownloadEntry e) {
        return delete(e.id);
    }

    public synchronized boolean delete(ArrayList<DownloadEntry> es) {
        String[] ids = new String[es.size()];
        for (int i = 0; i < es.size(); i++) {
            ids[i] = es.get(i).id;
        }
        long number = getDB().delete(DownloadDBHelper.DB_TABLE, "id=?", ids);
        return number > 0;
    }

    public synchronized boolean deleteAll() {
        long number = getDB().delete(DownloadDBHelper.DB_TABLE, null, null);
        return number > 0;
    }

    public static class DownloadDBHelper extends SQLiteOpenHelper {
        public final static String DB_TABLE = "tb_download";

        public DownloadDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                                int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE " + DB_TABLE + "  (" +
                        "id  TEXT NOT NULL," +
                        "url  TEXT NOT NULL DEFAULT ''," +
                        "contentLength  INTEGER DEFAULT 0," +
                        "currentLength  INTEGER DEFAULT 0," +
                        "state  TEXT NOT NULL DEFAULT ''," +
                        "dir  TEXT NOT NULL DEFAULT ''," +
                        "name  TEXT NOT NULL DEFAULT ''," +
                        "ranges  TEXT," +
                        "isSupportRange  INTEGER," +
                        "PRIMARY KEY (id)" + ");");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE tb_download ADD COLUMN \"dir\" TEXT NOT NULL DEFAULT ''");
                db.execSQL("ALTER TABLE tb_download ADD COLUMN \"name\" TEXT NOT NULL DEFAULT ''");
            }
        }
    }
}