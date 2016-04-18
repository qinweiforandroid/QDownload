package com.qw.download;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 获取下载文件基本信息线程
 * Created by qinwei on 2016/4/14 16:05
 * email:qinwei_it@163.com
 */
public class DownloadConnectThread implements Runnable {
    private OnConnectThreadListener listener;
    private DownloadEntity entity;
    private boolean running;
    private DownloadEntity.State state;


    public interface OnConnectThreadListener {
        void onConnectCompleted(long contentLength, boolean isSupportRange);

        void onConnectError(DownloadEntity.State state, String msg);
    }

    public DownloadConnectThread(DownloadEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        running = true;
        HttpURLConnection connection = null;
        state = DownloadEntity.State.connect;
        try {
            connection = (HttpURLConnection) new URL(entity.url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(DownloadConfig.CONNECT_TIME);
            connection.setReadTimeout(DownloadConfig.READ_TIME);
            connection.setRequestProperty("Range", "bytes=0-" + Integer.MAX_VALUE);
            int contentLength = connection.getContentLength();
            boolean isSupportRange = false;
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_PARTIAL) {
                isSupportRange = true;
            } else {
                listener.onConnectError(DownloadEntity.State.error, "server error " + code);
            }
            listener.onConnectCompleted(contentLength, isSupportRange);
        } catch (MalformedURLException e) {
            listener.onConnectError(DownloadEntity.State.error, e.getMessage());
        } catch (IOException e) {
            switch (state) {
                case paused:
                case cancelled:
                    listener.onConnectError(state, e.getMessage());
                    break;
                default:
                    listener.onConnectError(DownloadEntity.State.error, e.getMessage());
                    break;
            }
        } finally {
            running = false;
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void setOnConnectThreadListener(OnConnectThreadListener listener) {
        this.listener = listener;
    }

    public boolean isRunning() {
        return running;
    }

    public void cancel(DownloadEntity.State state) {
        running = false;
        this.state = state;
    }
}
