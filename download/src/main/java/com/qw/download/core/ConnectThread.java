package com.qw.download.core;

import com.qw.download.DownloadConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 获取下载文件基本信息线程
 * Created by qinwei on 2016/4/14 16:05
 * email:qinwei_it@163.com
 */
public class ConnectThread implements Runnable {
    private final String url;
    private boolean running;
    private OnConnectThreadListener listener;

    public ConnectThread(String url, OnConnectThreadListener listener) {
        this.url = url;
        this.listener = listener;
    }

    public boolean isRunning() {
        return running;
    }

    public void cancel() {
        listener = null;
        running = false;
    }

    @Override
    public void run() {
        running = true;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Accept-Ranges", "bytes");
            connection.setConnectTimeout(DownloadConfig.getInstance().getConnectTimeout());
            connection.setReadTimeout(DownloadConfig.getInstance().getReadTimeout());
            boolean isSupportRange;
            int contentLength = connection.getContentLength();
//            String contentType = connection.getHeaderField("Content-Type");
            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                } else {
                    isSupportRange = retrySupportRange();
                }
                if (listener != null) {
                    listener.onConnectCompleted(contentLength, isSupportRange);
                }
            } else {
                if (listener != null) {
                    listener.onConnectError("server error " + code);
                }
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onConnectError(e.getMessage());
            }
        } finally {
            running = false;
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean retrySupportRange() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Accept-Ranges", "bytes");
            connection.setRequestProperty("Range", "bytes=0-1");
            connection.setConnectTimeout(DownloadConfig.getInstance().getConnectTimeout());
            connection.setReadTimeout(DownloadConfig.getInstance().getReadTimeout());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }

    public interface OnConnectThreadListener {
        void onConnectCompleted(long contentLength, boolean isSupportRange);

        void onConnectError(String msg);
    }
}