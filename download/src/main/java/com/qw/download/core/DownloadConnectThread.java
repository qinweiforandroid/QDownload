package com.qw.download.core;

import android.webkit.URLUtil;

import com.qw.download.DownloadConfig;
import com.qw.download.entities.DownloadEntity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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

    private boolean isSuccessful(int code) {
        return code >= 200 && code < 300;
    }

    @Override
    public void run() {
        running = true;
        HttpURLConnection connection = null;
        state = DownloadEntity.State.connect;
        try {
            URL url = new URL(entity.url);
            if (URLUtil.isHttpsUrl(entity.url)) {
                connection = (HttpsURLConnection) url.openConnection();
            } else if (URLUtil.isHttpUrl(entity.url)) {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Accept-Ranges", "bytes");
            connection.setConnectTimeout(DownloadConfig.getInstance().getConnectTimeOut());
            connection.setReadTimeout(DownloadConfig.getInstance().getReadTimeOut());
            boolean isSupportRange;
            int contentLength = connection.getContentLength();
            int code = connection.getResponseCode();
            if (isSuccessful(code)) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                } else {
                    isSupportRange = isSupportRange();
                }
                listener.onConnectCompleted(contentLength, isSupportRange);
            } else {
                listener.onConnectError(DownloadEntity.State.error, "server error " + code);
            }
        } catch (MalformedURLException e) {
            if (listener != null)
                listener.onConnectError(DownloadEntity.State.error, e.getMessage());
        } catch (IOException e) {
            if (listener != null) {
                switch (state) {
                    case cancelled:
                        listener.onConnectError(state, e.getMessage());
                        break;
                    default:
                        listener.onConnectError(DownloadEntity.State.error, e.getMessage());
                        break;
                }
            }
        } finally {
            running = false;
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isSupportRange() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(entity.url);
            if (URLUtil.isHttpsUrl(entity.url)) {
                connection = (HttpsURLConnection) url.openConnection();
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Accept-Ranges", "bytes");
            connection.setRequestProperty("Range", "bytes=0-1");
            connection.setConnectTimeout(DownloadConfig.getInstance().getConnectTimeOut());
            connection.setReadTimeout(DownloadConfig.getInstance().getReadTimeOut());
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

    public void setOnConnectThreadListener(OnConnectThreadListener listener) {
        this.listener = listener;
    }

    public boolean isRunning() {
        return running;
    }

    public void cancel(DownloadEntity.State state) {
        switch (state) {
            case paused:
            case cancelled:
                this.listener = null;
                break;
            default:
                break;
        }
        running = false;
        this.state = state;
    }
}
