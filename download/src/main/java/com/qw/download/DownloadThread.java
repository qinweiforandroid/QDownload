package com.qw.download;

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
    public static final String TAG = "DownloadThread";
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
        DLog.d(TAG, "isSingleThread " + isSingleThread + " threadIndex " + threadIndex + " start-end:" + start + "_" + end);
    }

    public DownloadThread(DownloadEntity entity) {
        this.entity = entity;
        threadIndex = 0;
        isSingleThread = true;
        DLog.d(TAG, "isSingleThread " + isSingleThread + " threadIndex " + threadIndex);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void pause() {
        listener = null;
        state = DownloadEntity.State.paused;
        isRunning = false;
        DLog.d(TAG, "pause interrupt threadIndex " + threadIndex);
    }

    public void cancel() {
        listener = null;
        state = DownloadEntity.State.cancelled;
        isRunning = false;
        DLog.d(TAG, "cancel interrupt threadIndex " + threadIndex);
    }

    public void error() {
        listener = null;
        state = DownloadEntity.State.error;
        isRunning = false;
        DLog.d(TAG, entity.id + " error interrupt threadIndex " + threadIndex);
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

        /**
         * @param index
         * @deprecated
         */
        void onDownloadPaused(int index);

        /**
         * @param index
         * @deprecated
         */
        void onDownloadCancelled(int index);
    }

    public void setOnDownloadListener(OnDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection connection = null;
        try {
            state = DownloadEntity.State.ing;
            connection = (HttpURLConnection) new URL(entity.url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(DownloadConfig.CONNECT_TIME);
            connection.setReadTimeout(DownloadConfig.READ_TIME);
            if (!isSingleThread) {
                connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
            }
            File file = new File(DownloadFileUtil.getDownloadPath(entity.id));
            InputStream is = null;
            int code = connection.getResponseCode();

            if (code == HttpURLConnection.HTTP_PARTIAL) {
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.seek(start);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPause() || isCancelled() || isError()) {
                        break;
                    }
                    raf.write(buffer, 0, len);
                    if (listener != null)
                        listener.onDownloadProgressUpdate(threadIndex, len);
                }
                raf.close();
            } else {
                FileOutputStream fos = new FileOutputStream(file);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPause() || isCancelled() || isError()) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    if (listener != null)
                        listener.onDownloadProgressUpdate(threadIndex, len);
                }
                fos.close();
            }
            if (state == DownloadEntity.State.paused) {
                if (listener != null)
                    listener.onDownloadPaused(threadIndex);
            } else if (state == DownloadEntity.State.cancelled) {
                if (listener != null)
                    listener.onDownloadCancelled(threadIndex);
            } else if (state == DownloadEntity.State.error) {
                if (listener != null)
                    listener.onDownloadError(threadIndex, "error by frame");
            } else {
                if (listener != null)
                    listener.onDownloadCompleted(threadIndex);
            }
        } catch (MalformedURLException e) {
            if (listener != null)
                listener.onDownloadError(threadIndex, e.getMessage());
        } catch (IOException e) {
            if (state == DownloadEntity.State.paused) {
                if (listener != null)
                    listener.onDownloadPaused(threadIndex);
            } else if (state == DownloadEntity.State.cancelled) {
                if (listener != null)
                    listener.onDownloadCancelled(threadIndex);
            } else {
                synchronized (DownloadThread.class) {
                    if (listener != null) {
                        listener.onDownloadError(threadIndex, e.getMessage());
                    }
                }
            }
        } finally {
            isRunning = false;
        }
    }
}
