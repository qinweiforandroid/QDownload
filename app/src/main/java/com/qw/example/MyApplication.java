package com.qw.example;

import android.app.Application;

import com.qw.download.manager.DownloadManager;


/**
 * Created by qinwei on 2021/6/7 18:41
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.init(this);
    }
}
