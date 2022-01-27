package com.qw.download;

import android.os.Handler;
import android.os.Message;

import com.qw.download.db.DownloadDBManager;
import com.qw.download.utilities.DLog;
import com.qw.download.utilities.TickTack;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * 下载任务
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
class DownloadTask implements ConnectThread.OnConnectThreadListener, DownloadThread.OnDownloadListener {
    private final static String TAG = "Task";
    private ExecutorService mExecutors;
    private final Handler mHandler;
    private volatile DownloadEntry entry;
    private volatile DownloadThread[] threads;
    private volatile DownloadEntry.State[] states;
    private volatile ConnectThread connectThread;
    private volatile int currentRetryIndex;
    private final File destFile;

    public DownloadTask(DownloadEntry entry, ExecutorService mExecutors, Handler handler) {
        this.entry = entry;
        this.mExecutors = mExecutors;
        this.mHandler = handler;
        this.destFile = DownloadConfig.getInstance().getDownloadFile(entry.url);
    }

    public void start() {
        d("start");
        currentRetryIndex = 0;
        if (entry.contentLength == 0) {
            connect();
        } else {
            download();
        }
    }

    private void connect() {
        d("connect");
        connectThread = new ConnectThread(entry.url, this);
        entry.state = DownloadEntry.State.CONNECT;
        mExecutors.execute(connectThread);
        notifyUpdate(DownloadService.NOTIFY_CONNECTING);
    }

    private void d(String msg) {
        DLog.d(TAG + "--> " + entry.id + " " + msg);
    }

    public void pause() {
        d("pause");
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        } else {
            if (!entry.isSupportRange) {
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
        entry.state = DownloadEntry.State.PAUSED;
        notifyUpdate(DownloadService.NOTIFY_PAUSED);
    }

    public void cancel() {
        d("cancel");
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        } else {
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null && threads[i].isRunning()) {
                    threads[i].cancel();
                }
            }
        }
        if (destFile.exists()) {
            if (destFile.delete()) {
                d("cancel delete temp file " + destFile);
            }
        }
        entry.state = DownloadEntry.State.CANCELLED;
        entry.reset();
        notifyUpdate(DownloadService.NOTIFY_CANCELLED);
    }

    private void download() {
        d("download");
        entry.state = DownloadEntry.State.ING;
        notifyUpdate(DownloadService.NOTIFY_ING);
        if (entry.isSupportRange) {
            multithreadingDownload();
        } else {
            singleThreadDownload();
        }
    }

    private void multithreadingDownload() {
        d("startMultithreadingDownload");
        threads = new DownloadThread[DownloadConfig.getInstance().getMaxThread()];
        states = new DownloadEntry.State[DownloadConfig.getInstance().getMaxThread()];
        int start;
        int end;
        if (entry.ranges == null) {
            entry.ranges = new HashMap<>();
            for (int i = 0; i < DownloadConfig.getInstance().getMaxThread(); i++) {
                entry.ranges.put(i, 0L);
            }
        }
        int block = (int) (entry.contentLength / threads.length);
        for (int i = 0; i < threads.length; i++) {
            start = (int) (i * block + entry.ranges.get(i));
            if (i != threads.length - 1) {
                end = (i + 1) * block - 1;
            } else {
                end = (int) entry.contentLength;
            }
            if (start < end) {
                threads[i] = new DownloadThread(entry.url, destFile, i, start, end, this);
                states[i] = DownloadEntry.State.ING;
                mExecutors.execute(threads[i]);
            } else {
                states[i] = DownloadEntry.State.DONE;
            }
        }
    }

    private void singleThreadDownload() {
        d("startSingleThreadDownload");
        threads = new DownloadThread[1];
        states = new DownloadEntry.State[1];
        threads[0] = new DownloadThread(entry.url, destFile, 0, 0, 0, this);
        states[0] = DownloadEntry.State.ING;
        entry.state = DownloadEntry.State.ING;
        mExecutors.execute(threads[0]);
        notifyUpdate(DownloadService.NOTIFY_ING);
    }

    public synchronized void notifyUpdate(int what) {
        if (entry.isSupportRange) {
            DownloadDBManager.getInstance().newOrUpdate(entry);
        }
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = entry;
        mHandler.sendMessageDelayed(msg, 20);
    }

    @Override
    public synchronized void onConnectCompleted(long contentLength, boolean isSupportRange) {
        d("onConnectCompleted contentLength:" + contentLength + ",isSupportRange:" + isSupportRange);
        entry.isSupportRange = isSupportRange;
        entry.contentLength = contentLength;
        download();
    }

    @Override
    public synchronized void onConnectError(String msg) {
        d("onConnectError " + msg);
        entry.state = DownloadEntry.State.ERROR;
        if (currentRetryIndex < DownloadConfig.getInstance().getMaxRetryCount()) {
            d("onConnectError retry connect " + currentRetryIndex);
            currentRetryIndex++;
            connect();
            return;
        }
        currentRetryIndex = 0;
        notifyUpdate(DownloadService.NOTIFY_ERROR);
    }

    /**
     * 缓存当前进度
     */
    private long tempCurrentLength;
    /**
     * 缓存进度的时间戳
     */
    private long timestamp;

    @Override
    public synchronized void onDownloadProgressUpdate(int index, long progress) {
        entry.currentLength += progress;
        if (entry.isSupportRange && entry.ranges != null) {
            entry.ranges.put(index, entry.ranges.get(index) + progress);
        }
        if (TickTack.getInstance().needToNotify()) {
            if (tempCurrentLength > 0) {
                //计算下载速度
                float time = (System.currentTimeMillis() - timestamp) / 1000f;
                entry.speed = (int) ((entry.currentLength - tempCurrentLength) / time);
            }
            tempCurrentLength = entry.currentLength;
            timestamp = System.currentTimeMillis();
            d("thread[" + index + "] progress " + entry.currentLength + "/" + entry.contentLength);
            notifyUpdate(DownloadService.NOTIFY_PROGRESS_UPDATE);
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        d("onDownloadCompleted thread[" + index + "]");
        states[index] = DownloadEntry.State.DONE;
        for (DownloadEntry.State state : states) {
            if (state != DownloadEntry.State.DONE) {
                return;
            }
        }
        entry.state = DownloadEntry.State.DONE;
        notifyUpdate(DownloadService.NOTIFY_COMPLETED);
    }

    @Override
    public synchronized void onDownloadError(int index, String msg) {
        d(" onDownloadError " + msg + " thread[" + index + "]");
        states[index] = DownloadEntry.State.ERROR;
        for (int i = 0; i < states.length; i++) {
            if (states[i] == DownloadEntry.State.ING) {
                threads[i].cancelByError();
            }
        }
        //只有支持断点续传 才能进行重试恢复下载操作
        if (entry.isSupportRange && currentRetryIndex < DownloadConfig.getInstance().getMaxTask()) {
            d(" thread[" + index + "] error retry " + currentRetryIndex);
            currentRetryIndex++;
            download();
        } else {
            if (!entry.isSupportRange) {
                //不支持断点下载 要把缓存文件删掉
                destFile.deleteOnExit();
                entry.reset();
            }
            entry.state = DownloadEntry.State.ERROR;
            notifyUpdate(DownloadService.NOTIFY_ERROR);
        }
    }
}