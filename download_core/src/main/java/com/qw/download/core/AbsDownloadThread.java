package com.qw.download.core;

import com.qw.download.utilities.DLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载线程
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public abstract class AbsDownloadThread implements Runnable {
    protected boolean isRunning;
    protected boolean isPaused;
    protected boolean isCancelled;
    protected boolean isError;
    /**
     * 存储线程的索引
     */
    protected int index;

    protected final String id;

    protected final String url;
    protected final File destFile;
    protected OnDownloadListener listener;
    protected int connectTimeout;
    protected int readTimeout;


    public AbsDownloadThread(String id, String url, File destFile, OnDownloadListener listener) {
        this.id = id;
        this.url = url;
        this.destFile = destFile;
        this.listener = listener;
    }

    private HttpURLConnectionListener httpURLConnectionListener;

    public void setHttpURLConnectionListener(HttpURLConnectionListener httpURLConnectionListener) {
        this.httpURLConnectionListener = httpURLConnectionListener;
    }

    public void config(HttpURLConnection connection) {
        if (httpURLConnectionListener != null) {
            httpURLConnectionListener.config(connection);
        }
    }

    public void setConnectTimeout(int time) {
        this.connectTimeout = time;
    }

    public void setReadTimeout(int time) {
        this.readTimeout = time;
    }

    protected void d(String msg) {
        DLog.d("DownloadThread--> " + msg + " url " + url + " ");
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void pause() {
        listener = null;
        isRunning = false;
        isPaused = true;
    }

    public void cancel() {
        listener = null;
        isRunning = false;
        isCancelled = true;
    }

    public void cancelByError() {
        listener = null;
        isRunning = false;
        isError = true;
    }

    protected boolean isPaused() {
        return isPaused;
    }

    protected boolean isCancelled() {
        return isCancelled;
    }

    protected boolean isError() {
        return isError;
    }


    public interface OnDownloadListener {
        void onDownloadProgressUpdate(int index, long progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String msg);
    }
}