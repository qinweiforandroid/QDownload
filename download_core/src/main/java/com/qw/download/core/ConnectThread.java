package com.qw.download.core;


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

    private int connectTimeout;
    private int readTimeout;


    public ConnectThread(String url, OnConnectThreadListener listener) {
        this.url = url;
        this.listener = listener;
    }

    private HttpURLConnectionListener httpURLConnectionListener;

    public void setHttpURLConnectionListener(HttpURLConnectionListener httpURLConnectionListener) {
        this.httpURLConnectionListener = httpURLConnectionListener;
    }

    public boolean isRunning() {
        return running;
    }

    public void setConnectTimeout(int time) {
        this.connectTimeout = time;
    }

    public void setReadTimeout(int time) {
        this.readTimeout = time;
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
            connection.setRequestMethod("HEAD");
            connection.addRequestProperty("Accept-Ranges", "bytes");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            //config
            if (httpURLConnectionListener != null) {
                httpURLConnectionListener.config(connection);
            }
            boolean isSupportRange;
            int contentLength = connection.getContentLength();
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
            connection.setRequestMethod("HEAD");
            connection.addRequestProperty("Accept-Ranges", "bytes");
            connection.setRequestProperty("Range", "bytes=0-1");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
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