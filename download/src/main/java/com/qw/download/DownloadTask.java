package com.qw.download;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 下载任务
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadTask implements DownloadConnectThread.OnConnectThreadListener, DownloadThread.OnDownloadListener {
    public static final String TAG = "DownloadTask";
    private volatile DownloadEntity entity;
    private ExecutorService mExecutors;
    private final Handler mHandler;
    private volatile DownloadThread[] threads;
    private volatile DownloadEntity.State[] states;
    private volatile DownloadConnectThread connectThread;
    private volatile Future<?> connectThreadFuture;
    private volatile Future<?>[] futures;
    private volatile int currentRetryIndex;

    public DownloadTask(DownloadEntity entity, ExecutorService mExecutors, Handler handler) {
        this.entity = entity;
        this.mExecutors = mExecutors;
        this.mHandler = handler;
    }

    public void start() {
        currentRetryIndex = 0;
        if (entity.contentLength == 0) {
            doConnectDownloadFile();
        } else {
            startDownload();
        }
    }

    private void doConnectDownloadFile() {
        DLog.d("http request file info " + entity.id);
        connectThread = new DownloadConnectThread(entity);
        connectThread.setOnConnectThreadListener(this);
        connectThreadFuture = mExecutors.submit(connectThread);
        entity.state = DownloadEntity.State.connect;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_CONNECTING);
    }

    public void pause() {
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel(DownloadEntity.State.paused);
            connectThreadFuture.cancel(true);
        } else {
            if (!entity.isSupportRange) {//单线程下载不支持暂停操作
                cancel();
                return;
            }
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null && threads[i].isRunning()) {
                    threads[i].pause();
                    futures[i].cancel(true);
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
            connectThreadFuture.cancel(true);
        } else {
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null && threads[i].isRunning()) {
                    threads[i].cancel();
                    futures[i].cancel(true);
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

    private void startDownload() {
        DLog.d("start download id=" + entity.id);
        entity.state = DownloadEntity.State.ing;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ING);
        if (entity.isSupportRange) {
            startMultithreadingDownload();
        } else {
            startSingleThreadDownload();
        }
    }

    private void startMultithreadingDownload() {
        DLog.d("start multithread download id=" + entity.id);
        threads = new DownloadThread[DownloadConfig.MAX_DOWNLOAD_THREAD_SIZE];
        futures = new Future<?>[DownloadConfig.MAX_DOWNLOAD_THREAD_SIZE];
        states = new DownloadEntity.State[DownloadConfig.MAX_DOWNLOAD_THREAD_SIZE];
        int start;
        int end;
        if (entity.ranges == null) {
            DLog.d("init ranges id=" + entity.id);
            entity.ranges = new HashMap<>();
            for (int i = 0; i < DownloadConfig.MAX_DOWNLOAD_THREAD_SIZE; i++) {
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
//                mExecutors.execute(threads[i]);
                futures[i] = mExecutors.submit(threads[i]);
            } else {
                states[i] = DownloadEntity.State.done;
            }
        }
    }

    private void startSingleThreadDownload() {
        DLog.d("start single thread download id=" + entity.id);
        threads = new DownloadThread[1];
        futures = new Future<?>[1];
        states = new DownloadEntity.State[1];
        threads[0] = new DownloadThread(entity);
        threads[0].setOnDownloadListener(this);
        states[0] = DownloadEntity.State.ing;
        entity.state = DownloadEntity.State.ing;
        futures[0] = mExecutors.submit(threads[0]);
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ING);
    }


    @Override
    public synchronized void onConnectCompleted(long contentLength, boolean isSupportRange) {
        entity.isSupportRange = isSupportRange;
        entity.contentLength = contentLength;
        DLog.d("http request success contentLength:" + contentLength + ",isSupportRange:" + isSupportRange + " id=" + entity.id);
        startDownload();
    }

    @Override
    public synchronized void onConnectError(DownloadEntity.State state, String msg) {
        entity.state = state;
        DLog.d("http request error " + " msg:" + msg + " id=" + entity.id);
        if (currentRetryIndex < DownloadConfig.MAX_RETRY_COUNT) {
            DLog.d("http request  retry " + currentRetryIndex);
            currentRetryIndex++;
            doConnectDownloadFile();
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
//        将子线程数据 传递给主线程
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = entity;
        mHandler.sendMessage(msg);
    }

    long tmp;

    @Override
    public synchronized void onDownloadProgressUpdate(int index, long progress) {
        entity.currentLength += progress;
        if (entity.isSupportRange) {
            entity.ranges.put(index, entity.ranges.get(index) + progress);
        }
        if (System.currentTimeMillis() - tmp >= 1000) {
            DLog.d("thread index=" + index + " progress update " + entity.currentLength + " / " + entity.contentLength + " id=" + entity.id);
            notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_PROGRESS_UPDATE);
            tmp = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        DLog.d("thread index=" + index + " execute completed" + " id=" + entity.id);
        states[index] = DownloadEntity.State.done;
        for (DownloadEntity.State state : states) {
            if (state != DownloadEntity.State.done) {
                return;
            }
        }
        entity.state = DownloadEntity.State.done;
        DLog.d("the download task is completed notifyUpdate state=" + entity.state.name());
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_COMPLETED);
    }

    @Override
    public synchronized void onDownloadError(int index, String msg) {
//        FIXME 有一条线程出错 停止其它下载线程
        DLog.d("thread index=" + index + " execute error" + " msg:" + msg + " id=" + entity.id);
        states[index] = DownloadEntity.State.error;
        for (int i = 0; i < states.length; i++) {
            if (states[i] == DownloadEntity.State.ing) {
                threads[i].error();
            }
        }
        //只有支持断点续传 才能进行重试恢复下载操作
        if (currentRetryIndex < DownloadConfig.MAX_RETRY_COUNT && entity.isSupportRange) {
            DLog.d("thread download error  retry " + currentRetryIndex + " id=" + entity.id);
            currentRetryIndex++;
            startDownload();
        } else {
            if (!entity.isSupportRange) {//不支持断点下载 要把缓存文件删掉
                File file = new File(DownloadFileUtil.getDownloadPath(entity.id));
                if (file.exists()) {
                    file.delete();
                }
                entity.reset();
            }
            entity.state = DownloadEntity.State.error;
            DLog.d("the download task is error notifyUpdate state=" + entity.state.name());
            notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ERROR);
        }
    }

    @Override
    public synchronized void onDownloadPaused(int index) {
        DLog.d("thread index=" + index + " paused" + " id=" + entity.id);
        states[index] = DownloadEntity.State.paused;
        for (DownloadEntity.State state : states) {
            if (state == DownloadEntity.State.ing) {
                return;
            }
        }
        entity.state = DownloadEntity.State.paused;
        DLog.d("the download task is Paused notifyUpdate state=" + entity.state.name());
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_PAUSED);
    }

    @Override
    public synchronized void onDownloadCancelled(int index) {
        DLog.d("thread index=" + index + " cancelled" + " id=" + entity.id);
        states[index] = DownloadEntity.State.cancelled;
        for (DownloadEntity.State state : states) {
            if (state == DownloadEntity.State.ing) {
                return;
            }
        }
        File file = new File(DownloadFileUtil.getDownloadPath(entity.id));
        if (file.exists()) {
            file.delete();
        }
        entity.state = DownloadEntity.State.cancelled;
        entity.reset();
        DLog.d("the download task is Cancelled notifyUpdate state=" + entity.state.name());
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_CANCELLED);
    }
}
