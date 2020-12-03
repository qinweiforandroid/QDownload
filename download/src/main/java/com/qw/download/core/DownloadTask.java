package com.qw.download.core;

import android.os.Handler;
import android.os.Message;

import com.qw.download.DownloadConfig;
import com.qw.download.db.DownloadDBController;
import com.qw.download.entities.DownloadEntry;
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
public class DownloadTask implements ConnectThread.OnConnectThreadListener, DownloadThread.OnDownloadListener {
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
        d("start " + entry.id);
        currentRetryIndex = 0;
        if (entry.contentLength == 0) {
            connect();
        } else {
            download();
        }
    }

    private void connect() {
        d("connect " + entry.id);
        connectThread = new ConnectThread(entry.url, this);
        entry.state = DownloadEntry.State.connect;
        mExecutors.execute(connectThread);
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_CONNECTING);
    }

    private void d(String msg) {
        DLog.d(TAG + " " + msg);
    }

    public void pause() {
        d("pause " + entry.id);
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
        entry.state = DownloadEntry.State.paused;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_PAUSED);
    }

    public void cancel() {
        d("cancel " + entry.id);
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
                d("cancel delete temp " + destFile);
            }
        }
        entry.state = DownloadEntry.State.cancelled;
        entry.reset();
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_CANCELLED);
    }

    private void download() {
        d("download " + entry.id);
        entry.state = DownloadEntry.State.ing;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ING);
        if (entry.isSupportRange) {
            multithreadingDownload();
        } else {
            singleThreadDownload();
        }
    }

    private void multithreadingDownload() {
        d("startMultithreadingDownload  " + entry.id);
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
                states[i] = DownloadEntry.State.ing;
                mExecutors.execute(threads[i]);
            } else {
                states[i] = DownloadEntry.State.done;
            }
        }
    }

    private void singleThreadDownload() {
        d("startSingleThreadDownload " + entry.id);
        threads = new DownloadThread[1];
        states = new DownloadEntry.State[1];
        threads[0] = new DownloadThread(entry.url, destFile, 0, 0, 0, this);
        states[0] = DownloadEntry.State.ing;
        entry.state = DownloadEntry.State.ing;
        mExecutors.execute(threads[0]);
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ING);
    }


    @Override
    public synchronized void onConnectCompleted(long contentLength, boolean isSupportRange) {
        entry.isSupportRange = isSupportRange;
        entry.contentLength = contentLength;
        d("onConnectCompleted " + entry.id + " length:" + contentLength + ",isSupportRange:" + isSupportRange);
        download();
    }

    @Override
    public synchronized void onConnectError(String msg) {
        entry.state = DownloadEntry.State.error;
        d("onConnectError " + entry.id + " " + msg);
        if (currentRetryIndex < DownloadConfig.getInstance().getMaxRetryCount()) {
            d("onConnectError retry " + entry.id + " " + currentRetryIndex);
            currentRetryIndex++;
            connect();
            return;
        }
        currentRetryIndex = 0;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_COMPLETED);
    }

    public synchronized void notifyUpdate(int what) {
        DownloadDBController.getInstance().addOrUpdate(entry);
//        切换线程
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = entry;
        mHandler.sendMessage(msg);
    }

    @Override
    public synchronized void onDownloadProgressUpdate(int index, long progress) {
        entry.currentLength += progress;
        if (entry.isSupportRange && entry.ranges != null) {
            entry.ranges.put(index, entry.ranges.get(index) + progress);
        }
        if (TickTack.getInstance().needToNotify()) {
            d("thread" + index + " progress " + entry.currentLength + "/" + entry.contentLength + " " + entry.id);
            notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_PROGRESS_UPDATE);
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        d("thread" + index + " onDownloadCompleted " + entry.id);
        states[index] = DownloadEntry.State.done;
        for (DownloadEntry.State state : states) {
            if (state != DownloadEntry.State.done) {
                return;
            }
        }
        entry.state = DownloadEntry.State.done;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_COMPLETED);
    }

    @Override
    public synchronized void onDownloadError(int index, String msg) {
        d("thread" + index + " onDownloadError " + msg + " " + entry.id);
        states[index] = DownloadEntry.State.error;
        for (int i = 0; i < states.length; i++) {
            if (states[i] == DownloadEntry.State.ing) {
                threads[i].cancelByError();
            }
        }
        //只有支持断点续传 才能进行重试恢复下载操作
        if (entry.isSupportRange && currentRetryIndex < DownloadConfig.getInstance().getMaxTask()) {
            d("thread download error retry " + currentRetryIndex + " " + entry.id);
            currentRetryIndex++;
            download();
        } else {
            if (!entry.isSupportRange) {
                //不支持断点下载 要把缓存文件删掉
                if (destFile.exists()) {
                    destFile.delete();
                }
                entry.reset();
            }
            entry.state = DownloadEntry.State.error;
            notifyUpdate(DownloadService.NOTIFY_DOWNLOAD_ERROR);
        }
    }
}
