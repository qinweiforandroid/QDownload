package com.qw.download.db.sqlite.impl;

import android.content.Context;

import com.qw.download.db.dto.DownloadEntry;
import com.qw.download.db.IDownloadDao;
import com.qw.download.db.sqlite.DownloadDB;

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