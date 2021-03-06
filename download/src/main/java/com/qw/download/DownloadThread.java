package com.qw.download;

import com.qw.download.utilities.DLog;
import com.qw.download.DownloadConfig;

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
class DownloadThread implements Runnable {
    private boolean isRunning;
    private boolean isPaused;
    private boolean isCancelled;
    private boolean isError;
    private final boolean isSingleDownload;
    private final String url;
    private final File destFile;
    private final int index;
    private final int start;
    private final int end;
    private OnDownloadListener listener;

    public DownloadThread(String url, File destFile, int threadIndex, int start, int end, OnDownloadListener listener) {
        this.url = url;
        this.destFile = destFile;
        this.index = threadIndex;
        this.start = start;
        this.end = end;
        if (start == 0 && end == 0) {
            isSingleDownload = true;
        } else {
            isSingleDownload = false;
        }
        this.listener = listener;
        d("thread[" + threadIndex + "] start-end:" + start + "/" + end);
    }

    private void d(String msg) {
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

    private boolean isPaused() {
        return isPaused;
    }

    private boolean isCancelled() {
        return isCancelled;
    }

    private boolean isError() {
        return isError;
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
            connection.setConnectTimeout(DownloadConfig.getInstance().getConnectTimeout());
            connection.setReadTimeout(DownloadConfig.getInstance().getReadTimeout());
            if (!isSingleDownload) {
                connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
            }
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
            } else if (code == HttpURLConnection.HTTP_OK) {
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

    public interface OnDownloadListener {
        void onDownloadProgressUpdate(int index, long progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String msg);
    }
}