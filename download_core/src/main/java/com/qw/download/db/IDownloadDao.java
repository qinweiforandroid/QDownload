package com.qw.download.db;

import android.content.Context;

import com.qw.download.db.dto.DownloadEntry;

import java.util.ArrayList;

public interface IDownloadDao {
    boolean newOrUpdate(DownloadEntry entry);

    boolean delete(String id);

    ArrayList<DownloadEntry> queryAll();

    void init(Context context);
}