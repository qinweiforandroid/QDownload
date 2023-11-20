package com.qw.download.core;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载线程
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class MultiDownloadThread extends AbsDownloadThread {
    private final long start;
    private final long end;

    public MultiDownloadThread(String id, String url, File destFile, int threadIndex, long start, long end, OnDownloadListener listener) {
        super(id, url, destFile, listener);
        this.index = threadIndex;
        this.start = start;
        this.end = end;
        d(id + " thread[" + threadIndex + "] start-end:" + start + "/" + end);
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
//            header配置
//            connection.addRequestProperty("key","value");
//            if(connection instanceof HttpsURLConnection){
            //证书配置
//                HttpsURLConnection https= (HttpsURLConnection) connection;
//                https.setHostnameVerifier(HostnameVerifier);
//                https.setSSLSocketFactory(SSLSocketFactory);
//            }
            connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
            InputStream is;
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_PARTIAL) {
                RandomAccessFile raf = new RandomAccessFile(destFile, "rw");
                raf.seek(start);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused() || isCancelled() || isError()) {
                        break;
                    }
                    raf.write(buffer, 0, len);
                    if (listener != null) {
                        listener.onDownloadProgressUpdate(index, len);
                    }
                }
                is.close();
                raf.close();
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