package com.qw.download;

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
//        do request server
        running = true;
        try {
            state = DownloadEntity.State.connect;
            Thread.sleep(5000);
            listener.onConnectCompleted(50001, true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            listener.onConnectError(state, e.getMessage());
        } finally {
            running = false;
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
//        Thread.currentThread().interrupt();
    }
}
