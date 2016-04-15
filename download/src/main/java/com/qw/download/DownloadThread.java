package com.qw.download;

/**
 * 下载线程
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadThread implements Runnable {
    public static final String TAG="DownloadThread";
    private DownloadEntity entity;
    private int threadIndex;
    private int start;
    private int end;
    private OnDownloadListener listener;

    public DownloadThread(DownloadEntity entity, int threadIndex, int start, int end) {
        this.entity = entity;
        this.threadIndex = threadIndex;
        this.start = start;
        this.end = end;
        DLog.d(TAG,"threadIndex "+threadIndex+" start-end:"+start+"_"+end);
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
        try {
            int buffer = 1000;
            while (true) {
                Thread.sleep(500);
                start += buffer;
                if (start < end) {
                    listener.onDownloadProgressUpdate(threadIndex, buffer);
                } else {
                    listener.onDownloadProgressUpdate(threadIndex, end - (start-buffer));
                    break;
                }
            }
            listener.onDownloadCompleted(threadIndex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
