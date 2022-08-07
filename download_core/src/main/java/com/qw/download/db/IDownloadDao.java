package com.qw.download.db;

import android.content.Context;

import java.util.ArrayList;

public interface IDownloadDao {
    boolean newOrUpdate(DownloadEntry entry);

    boolean delete(String id);

    ArrayList<DownloadEntry> queryAll();

    void init(Context context);
}