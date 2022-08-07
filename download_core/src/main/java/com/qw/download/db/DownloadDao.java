package com.qw.download.db;

import android.content.Context;

import java.util.ArrayList;

public class DownloadDao implements IDownloadDao {
    private DownloadDB db;

    @Override
    public void init(Context context) {
        db = new DownloadDB(context);
    }

    @Override
    public boolean newOrUpdate(DownloadEntry entry) {
        return db.newOrUpdate(entry);
    }

    @Override
    public boolean delete(String id) {
        return db.delete(id);
    }

    @Override
    public ArrayList<DownloadEntry> queryAll() {
        return db.queryAll();
    }
}