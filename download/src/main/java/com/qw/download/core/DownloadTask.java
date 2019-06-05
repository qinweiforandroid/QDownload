package com.qw.download.core;

import android.os.Handler;
import android.os.Message;

import com.qw.download.db.DownloadDBController;
import com.qw.download.utilities.DLog;
import com.qw.download.DownloadConfig;
import com.qw.download.entities.DownloadEntity;
import com.qw.download.utilities.DownloadFileUtil;
import com.qw.download.utilities.TickTack;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * 下载任务
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadTask implements DownloadConnectThread.OnConnectThreadListener, DownloadThread.OnDownloadListener {
    private ExecutorService mExecutors;
    private final Handler mHandler;
    private volatile DownloadEntity entity;
    private volatile DownloadThread[] threads;
    private volatile DownloadEntity.State[] states;
    private volatile DownloadConnectThread connectThread;
    private volatile int currentRetryIndex;

    public DownloadTask(DownloadEntity entity, ExecutorService mExecutors, Handler handler) {
        this.entity = entity;
        this.mExecutors = mExecutors;
        this.mHandler = handler;
    }

    public void start() {
        currentRetryIndex = 0;
        if (entity.contentLength == 0) {
            loadDownloadFileSimpleInfo();
        } else {
            loadStartDownload();
        }
    }

    private void loadDownloadFileSimpleInfo() {
        DLog.d("loadDownloadFileSimpleInfo id " + entity.id);
        connectThread = new DownloadConnectThread(entity);
        connectThread.setOnConnectThreadListener(this);
        entity.state = DownloadEntity.State.connect;
        mExecutors.execute(connectThread);
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_CONNECTING);
    }

    public void pause() {
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel(DownloadEntity.State.cancelled);
        } else {
            if (!entity.isSupportRange) {
                //单线程下载不支持暂停操作
                cancel();
                return;
            }
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null && threads[i].isRunning()) {
                    threads[i].pause();
                }
            }
        }
        entity.state = DownloadEntity.State.paused;
        DLog.d("pause notifyUpdate downloadEntry info: " + entity.id + "/" + entity.state.name());
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_PAUSED);
    }

    public void cancel() {
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel(DownloadEntity.State.cancelled);
        } else {
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null && threads[i].isRunning()) {
                    threads[i].cancel();
                }
            }
        }

        File file = new File(DownloadFileUtil.getDownloadPath(entity.id));
        if (file.exists()) {
            boolean delete = file.delete();
            if (delete) {
                DLog.d("delete download temp file success id=" + entity.id);
            }
        }
        entity.state = DownloadEntity.State.cancelled;
        entity.reset();
        DLog.d("cancel notifyUpdate downloadEntry info: " + entity.id + "/" + entity.state.name());
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_CANCELLED);
    }

    private void loadStartDownload() {
        DLog.d("loadStartDownload id " + entity.id);
        entity.state = DownloadEntity.State.ing;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ING);
        if (entity.isSupportRange) {
            startMultithreadingDownload();
        } else {
            startSingleThreadDownload();
        }
    }

    private void startMultithreadingDownload() {
        DLog.d("startMultithreadingDownload id " + entity.id);
        threads = new DownloadThread[DownloadConfig.getInstance().getMaxDownloadThreads()];
        states = new DownloadEntity.State[DownloadConfig.getInstance().getMaxDownloadThreads()];
        int start;
        int end;
        if (entity.ranges == null) {
            DLog.d("startMultithreadingDownload init ranges id " + entity.id);
            entity.ranges = new HashMap<>();
            for (int i = 0; i < DownloadConfig.getInstance().getMaxDownloadThreads(); i++) {
                entity.ranges.put(i, 0L);
            }
        }
        int block = (int) (entity.contentLength / threads.length);
        for (int i = 0; i < threads.length; i++) {
            start = (int) (i * block + entity.ranges.get(i));
            if (i != threads.length - 1) {
                end = (i + 1) * block - 1;
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

    private void startSingleThreadDownload() {
        DLog.d("startSingleThreadDownload id " + entity.id);
        threads = new DownloadThread[1];
        states = new DownloadEntity.State[1];
        threads[0] = new DownloadThread(entity);
        threads[0].setOnDownloadListener(this);
        states[0] = DownloadEntity.State.ing;
        entity.state = DownloadEntity.State.ing;
        mExecutors.execute(threads[0]);
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ING);
    }


    @Override
    public synchronized void onConnectCompleted(long contentLength, boolean isSupportRange) {
        entity.isSupportRange = isSupportRange;
        entity.contentLength = contentLength;
        DLog.d("loadDownloadFileSimpleInfo success " + "id " + entity.id + " contentLength:" + contentLength + ",isSupportRange:" + isSupportRange);
        loadStartDownload();
    }

    @Override
    public synchronized void onConnectError(DownloadEntity.State state, String msg) {
        entity.state = state;
        DLog.d("http request error " + " msg:" + msg + " id=" + entity.id);
        if (currentRetryIndex < DownloadConfig.getInstance().getMaxRetryCount()) {
            DLog.d("http request  retry " + currentRetryIndex);
            currentRetryIndex++;
            loadDownloadFileSimpleInfo();
        } else {
            currentRetryIndex = 0;
            switch (state) {
                case paused:
                    notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_PAUSED);
                    break;
                case cancelled:
                    notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_CANCELLED);
                    break;
                case error:
                    notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ERROR);
                    break;
                default:
                    break;
            }
        }
    }

    public synchronized void notifyUpdate(int what) {
        DownloadDBController.getInstance().addOrUpdate(entity);
//        将子线程数据 传递给主线程
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = entity;
        mHandler.sendMessage(msg);
    }

    @Override
    public synchronized void onDownloadProgressUpdate(int index, long progress) {
        entity.currentLength += progress;
        if (entity.isSupportRange) {
            entity.ranges.put(index, entity.ranges.get(index) + progress);
        }
        if (TickTack.getInstance().needToNotify()) {
            DLog.d("thread index=" + index + " progress update " + entity.currentLength + " / " + entity.contentLength + " id=" + entity.id);
            notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_PROGRESS_UPDATE);
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        DLog.d("onDownloadCompleted thread index " + index + " execute completed" + " id " + entity.id);
        states[index] = DownloadEntity.State.done;
        for (DownloadEntity.State state : states) {
            if (state != DownloadEntity.State.done) {
                return;
            }
        }
        entity.state = DownloadEntity.State.done;
        DLog.d("the download task is completed notifyUpdate state " + entity.state.name());
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_COMPLETED);
    }

    @Override
    public synchronized void onDownloadError(int index, String msg) {
        DLog.d("onDownloadError thread index=" + index + " execute error" + " msg:" + msg + " id " + entity.id);
        states[index] = DownloadEntity.State.error;
        for (int i = 0; i < states.length; i++) {
            if (states[i] == DownloadEntity.State.ing) {
                threads[i].error();
            }
        }
        //只有支持断点续传 才能进行重试恢复下载操作
        if (entity.isSupportRange && currentRetryIndex < DownloadConfig.getInstance().getMaxDownloadTasks()) {
            DLog.d("thread download error retry " + currentRetryIndex + " id " + entity.id);
            currentRetryIndex++;
            loadStartDownload();
        } else {
            if (!entity.isSupportRange) {
                //不支持断点下载 要把缓存文件删掉
                File file = new File(DownloadFileUtil.getDownloadPath(entity.id));
                if (file.exists()) {
                    file.delete();
                }
                entity.reset();
            }
            entity.state = DownloadEntity.State.error;
            DLog.d("the download task is error notifyUpdate state " + entity.state.name());
            notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ERROR);
        }
    }
}
