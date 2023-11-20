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

    public void setConnectTimeout(int time) {
        this.connectTimeout = time;
    }

    public void setReadTimeout(int time) {
        this.readTimeout = time;
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
//            header配置
//            connection.addRequestProperty("key","value");
//            if(connection instanceof HttpsURLConnection){
            //证书配置
//                HttpsURLConnection https= (HttpsURLConnection) connection;
//                https.setHostnameVerifier(HostnameVerifier);
//                https.setSSLSocketFactory(SSLSocketFactory);
//            }
            InputStream is;
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
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