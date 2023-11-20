package com.qw.example;

import android.app.Application;

import com.qw.download.manager.DownloadConfig;


/**
 * Created by qinwei on 2021/6/7 18:41
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DownloadConfig.init(new DownloadConfig.Builder(this)
                .setConnectTimeout(10_000)//连接超时时间
                .setReadTimeout(10_000)//读取超时时间
                .setMaxTask(3)//最多3个任务同时下载
                .setMaxThread(3)//1个任务分3个线程分段下载
                .setAutoResume(true)//启动自动恢复下载
                .setRetryCount(3)//单个任务异常T下载失败重试次数
                .setDownloadDir(getCacheDir().getAbsolutePath())//设置文件存储目录
                .setHttpURLConnectionListener(connection -> {
                    //you can config connection
                })
                .setLogEnable(BuildConfig.DEBUG)
                .builder());
    }
}