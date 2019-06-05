package com.qw.download;

import android.os.Environment;

import java.io.File;

/**
 * Created by qinwei on 2016/4/21 10:09
 * email:qinwei_it@163.com
 */
public class DownloadConfig {
    private static DownloadConfig mInstance;
    private int max_download_tasks = 3;
    private int max_download_threads = 2;
    private int max_retry_count = 3;
    private int connect_time_out = 10 * 1000;
    private int read_time_out = 10 * 1000;
    private File downloadDir;

    public synchronized static DownloadConfig getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadConfig();
        }
        return mInstance;
    }

    private DownloadConfig() {
        downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public int getMaxDownloadTasks() {
        return max_download_tasks;
    }

    public int getMaxDownloadThreads() {
        return max_download_threads;
    }

    public int getMaxRetryCount() {
        return max_retry_count;
    }

    public int getConnectTimeOut() {
        return connect_time_out;
    }

    public int getReadTimeOut() {
        return read_time_out;
    }

    public File getDownloadDir() {
        return downloadDir;
    }
}
