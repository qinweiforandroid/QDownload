package com.qw.download.core;

import com.qw.download.utilities.DLog;
import com.qw.download.DownloadConfig;
import com.qw.download.entities.DownloadEntity;
import com.qw.download.utilities.DownloadFileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 下载线程
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadThread implements Runnable {
    private boolean isSingleThread;
    private DownloadEntity entity;
    private int threadIndex;
    private int start;
    private int end;
    private OnDownloadListener listener;
    private boolean isRunning;
    private DownloadEntity.State state;

    public DownloadThread(DownloadEntity entity, int threadIndex, int start, int end) {
        this.entity = entity;
        this.threadIndex = threadIndex;
        this.start = start;
        this.end = end;
        DLog.d("DownloadThread " + " threadIndex " + threadIndex + " start-end:" + start + "_" + end);
    }

    public DownloadThread(DownloadEntity entity) {
        this.entity = entity;
        threadIndex = 0;
        isSingleThread = true;
        DLog.d("DownloadThread");
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void pause() {
        listener = null;
        state = DownloadEntity.State.paused;
        isRunning = false;
        DLog.d("pause interrupt threadIndex " + threadIndex + " id " + entity.id);
    }

    public void cancel() {
        listener = null;
        state = DownloadEntity.State.cancelled;
        isRunning = false;
        DLog.d("cancel interrupt threadIndex " + threadIndex + " id " + entity.id);
    }

    public void error() {
        state = DownloadEntity.State.error;
        isRunning = false;
        DLog.d("error interrupt threadIndex " + threadIndex + " id " + entity.id);
    }

    private boolean isPause() {
        return state == DownloadEntity.State.paused;
    }

    private boolean isCancelled() {
        return state == DownloadEntity.State.cancelled;
    }

    public boolean isError() {
        return state == DownloadEntity.State.error;
    }

    public interface OnDownloadListener {
        void onDownloadProgressUpdate(int index, long progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String msg);
    }

    public void setOnDownloadListener(OnDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection connection;
        try {
            state = DownloadEntity.State.ing;
            connection = (HttpURLConnection) new URL(entity.url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(DownloadConfig.getInstance().getConnectTimeOut());
            connection.setReadTimeout(DownloadConfig.getInstance().getReadTimeOut());
            if (!isSingleThread) {
                connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
            }
            InputStream is;
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_PARTIAL) {
                RandomAccessFile raf = new RandomAccessFile(DownloadFileUtil.getDownloadPath(entity.id), "rw");
                raf.seek(start);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    if (isPause() || isCancelled() || isError() || !isRunning) {
                        break;
                    }
                    raf.write(buffer, 0, len);
                    if (listener != null)
                        listener.onDownloadProgressUpdate(threadIndex, len);
                }
                is.close();
                raf.close();
            } else if (code == HttpURLConnection.HTTP_OK) {
                FileOutputStream fos = new FileOutputStream(DownloadFileUtil.getDownloadPath(entity.id));
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    if (isPause() || isCancelled() || isError() || !isRunning) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    if (listener != null)
                        listener.onDownloadProgressUpdate(threadIndex, len);
                }
                is.close();
                fos.close();
            } else {
                state = DownloadEntity.State.error;
                if (listener != null) {
                    listener.onDownloadError(threadIndex, "server error code " + code);
                }
                return;
            }
            switch (state) {
                case paused:
                case cancelled:
                    break;
                case error:
                    if (listener != null) {
                        listener.onDownloadError(threadIndex, "inner interrupt error ");
                    }
                    break;
                default:
                    state = DownloadEntity.State.done;
                    if (listener != null) {
                        listener.onDownloadCompleted(threadIndex);
                    }
                    break;
            }
        } catch (MalformedURLException e) {
            if (listener != null)
                listener.onDownloadError(threadIndex, e.getMessage());
        } catch (IOException e) {
            if (listener != null) {
                listener.onDownloadError(threadIndex, e.getMessage());
            }
        } finally {
            isRunning = false;
        }
    }
}
