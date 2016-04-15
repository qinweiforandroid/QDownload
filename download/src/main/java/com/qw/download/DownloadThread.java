package com.qw.download;

/**
 * 下载线程
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadThread implements Runnable {
    public static final String TAG = "DownloadThread";
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
        DLog.d(TAG, "threadIndex " + threadIndex + " start-end:" + start + "_" + end);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void pause() {
        state = DownloadEntity.State.paused;
        isRunning = false;
        DLog.d(TAG, "pause interrupt threadIndex " + threadIndex);
    }

    public void cancel() {
        state = DownloadEntity.State.cancelled;
        isRunning = false;
        DLog.d(TAG, "cancel interrupt threadIndex " + threadIndex);
    }

    public interface OnDownloadListener {
        void onDownloadProgressUpdate(int index, long progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String msg);

        void onDownloadPaused(int index);

        void onDownloadCancelled(int index);
    }

    public void setOnDownloadListener(OnDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            state = DownloadEntity.State.ing;
            int buffer = 1000;
            while (isRunning) {
                Thread.sleep(500);
                start += buffer;
                if (start < end) {
                    listener.onDownloadProgressUpdate(threadIndex, buffer);
                } else {
                    listener.onDownloadProgressUpdate(threadIndex, end - (start - buffer));
                    break;
                }
            }
            if (state == DownloadEntity.State.paused) {
                listener.onDownloadPaused(threadIndex);
            } else if (state == DownloadEntity.State.cancelled) {
                listener.onDownloadCancelled(threadIndex);
            } else {
                listener.onDownloadCompleted(threadIndex);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            if (state == DownloadEntity.State.paused) {
                listener.onDownloadPaused(threadIndex);
            }
        }finally {
            isRunning = false;
        }
    }
}
