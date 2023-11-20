package com.qw.download.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载线程
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class SingleDownloadThread extends AbsDownloadThread {


    public SingleDownloadThread(String id, String url, File destFile, OnDownloadListener listener) {
        super(id, url, destFile, listener);
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection connection = null;
        //记录错误信息
        String msg = "";
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            config(connection);
            InputStream is;
            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                FileOutputStream fos = new FileOutputStream(destFile);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused() || isCancelled() || isError()) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    if (listener != null) {
                        listener.onDownloadProgressUpdate(index, len);
                    }
                }
                is.close();
                fos.close();
            } else {
                isError = true;
                msg = "server error code " + code;
            }
        } catch (Exception e) {
            isError = true;
            msg = e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (listener != null) {
                if (isError) {
                    listener.onDownloadError(index, msg);
                } else if (isRunning) {
                    isRunning = false;
                    listener.onDownloadCompleted(index);
                }
            }
        }
    }
}