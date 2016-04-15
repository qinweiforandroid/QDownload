package com.qw.download;

import android.os.Handler;
import android.os.Message;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * 下载任务
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadTask implements DownloadConnectThread.OnConnectThreadListener, DownloadThread.OnDownloadListener {
    public static final String TAG = "DownloadTask";
    private final DownloadEntity entity;
    private final ExecutorService mExecutors;
    private final Handler mHandler;
    private DownloadThread[] threads;
    private DownloadEntity.State[] states;

    public DownloadTask(DownloadEntity entity, ExecutorService mExecutors, Handler handler) {
        this.entity = entity;
        this.mExecutors = mExecutors;
        this.mHandler = handler;
    }

    public void start() {
        if (entity.contentLength == 0) {
            doConnectDownloadFile();
        } else {
            startDownload();
        }
    }

    public void stop() {
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] != null && threads[i].isRunning()) {
                threads[i].pause();
            }
        }
    }

    private void startDownload() {
        DLog.d(TAG, entity.id + " startDownload");
        entity.state = DownloadEntity.State.ing;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ING);
        if (entity.isSupportRange) {
            startMultithreadingDownload();
        } else {
            startSingThreadDownload();
        }
    }


    private void startMultithreadingDownload() {
        DLog.d(TAG, entity.id + " startMultithreadingDownload");
        threads = new DownloadThread[DownloadConfig.MAX_DOWNLOAD_THREAD_SIZE];
        states = new DownloadEntity.State[DownloadConfig.MAX_DOWNLOAD_THREAD_SIZE];
        int start = 0;
        int end = 0;
        if (entity.ranges == null) {
            entity.ranges = new HashMap<>();
            for (int i = 0; i < DownloadConfig.MAX_DOWNLOAD_THREAD_SIZE; i++) {
                entity.ranges.put(i, (long) 0);
            }
        }
        int block = (int) (entity.contentLength / threads.length);
        for (int i = 0; i < threads.length; i++) {
            start = (int) (i * block + entity.ranges.get(i));
            if (i != threads.length - 1) {
                end = (i + 1) * block;
            } else {
                end = (int) entity.contentLength;
            }
            if (start < end) {
                threads[i] = new DownloadThread(entity, i, start, end);
                states[i] = DownloadEntity.State.ing;
                threads[i].setOnDownloadListener(this);
                mExecutors.execute(threads[i]);
            } else {
                states[i] = DownloadEntity.State.done;
            }
        }
    }

    private void startSingThreadDownload() {
        DLog.d(TAG, entity.id + " startSingThreadDownload");
    }

    private void doConnectDownloadFile() {
        DLog.d(TAG, entity.id + " doConnectDownloadFile");
        DownloadConnectThread connectThread = new DownloadConnectThread(entity);
        connectThread.setOnConnectThreadListener(this);
        mExecutors.execute(connectThread);
        entity.state = DownloadEntity.State.connect;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_CONNECTING);
    }

    @Override
    public void onConnectCompleted(long contentLength, boolean isSupportRange) {
        entity.isSupportRange = isSupportRange;
        entity.contentLength = contentLength;
        DLog.d(TAG, entity.id + " onConnectCompleted contentLength:" + contentLength + ",isSupportRange:" + isSupportRange);
        startDownload();
    }

    @Override
    public void onConnectError(String msg) {
        entity.state = DownloadEntity.State.error;
        DLog.d(TAG, entity.id + " onConnectError " + msg);
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ERROR);
    }

    public synchronized void notifyUpdate(int what) {
//        将子线程数据 转向主线程
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = entity;
        mHandler.sendMessage(msg);
    }

    long tmp;

    @Override
    public synchronized void onDownloadProgressUpdate(int index, long progress) {
        entity.currentLength += progress;
        entity.ranges.put(index, entity.ranges.get(index) + progress);
        if (System.currentTimeMillis() - tmp >= 1000) {
            DLog.d(TAG, entity.id + " onDownloadProgressUpdate thread index " + index + "," + entity.currentLength + "/" + entity.contentLength);
            notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_PROGRESS_UPDATE);
            tmp = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        DLog.d(TAG, entity.id + " onDownloadCompleted thread index " + index);
        states[index] = DownloadEntity.State.done;
        for (int i = 0; i < states.length; i++) {
            if (states[i] != DownloadEntity.State.done) {
                return;
            }
        }
        entity.state = DownloadEntity.State.done;
        DLog.d(TAG, entity.id + " onDownloadCompleted notifyUpdate download state: " + entity.state.name());
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_COMPLETED);
    }

    @Override
    public synchronized void onDownloadError(int index, String msg) {

    }

    @Override
    public synchronized void onDownloadPaused(int index) {
        DLog.d(TAG, entity.id + " onDownloadPaused thread index " + index);
        states[index] = DownloadEntity.State.paused;
        for (int i = 0; i < states.length; i++) {
            if (states[i] == DownloadEntity.State.ing) {
                return;
            }
        }
        entity.state = DownloadEntity.State.paused;
        DLog.d(TAG, entity.id + " onDownloadPaused notifyUpdate download state: " + entity.state.name());
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_PAUSED);
    }

    @Override
    public synchronized void onDownloadCancelled(int index) {

    }


}
