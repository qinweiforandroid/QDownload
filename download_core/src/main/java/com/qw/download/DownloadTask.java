package com.qw.download;


import android.os.SystemClock;

import androidx.annotation.RestrictTo;

import com.qw.download.core.AbsDownloadThread;
import com.qw.download.core.ConnectThread;
import com.qw.download.core.HttpURLConnectionListener;
import com.qw.download.core.MultiDownloadThread;
import com.qw.download.core.SingleDownloadThread;
import com.qw.download.db.dto.DownloadEntry;
import com.qw.download.db.dto.DownloadState;
import com.qw.download.db.IDownloadDao;
import com.qw.download.utilities.DConstants;
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
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DownloadTask implements ConnectThread.OnConnectThreadListener, AbsDownloadThread.OnDownloadListener {
    private final static String TAG = "Task";
    private ExecutorService mExecutors;
    private int maxThreads = 1;
    private int maxRetryCount = 1;
    private int connectTimeout;
    private int readTimeout;
    private volatile DownloadEntry entry;
    private volatile AbsDownloadThread[] threads;
    private volatile DownloadState[] states;
    private volatile ConnectThread connectThread;
    private final File destFile;
    private final TickTack mSpeedTickTack;
    private final TickTack mProgressTickTack;
    private volatile int currentRetryIndex;
    private HttpURLConnectionListener httpURLConnectionListener;
    private OnDownloadTaskListener listener;
    private IDownloadDao dao;

    public DownloadTask(DownloadEntry entry, ExecutorService mExecutors, TickTack progressTickTack) {
        this.entry = entry;
        this.mExecutors = mExecutors;
        this.mProgressTickTack = progressTickTack;
        this.destFile = new File(entry.getDir(), entry.getName());
        d("file:" + destFile.getAbsolutePath());
        //计算500毫秒内的下载速度
        this.mSpeedTickTack = new TickTack(500);
    }

    public void setDao(IDownloadDao dao) {
        this.dao = dao;
    }


    public void setHttpURLConnectionListener(HttpURLConnectionListener httpURLConnectionListener) {
        this.httpURLConnectionListener = httpURLConnectionListener;
    }

    public void setOnDownloadTaskListener(OnDownloadTaskListener listener) {
        this.listener = listener;
    }

    public void setMaxThread(int size) {
        this.maxThreads = size;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setConnectTimeout(int time) {
        this.connectTimeout = time;
    }

    public void setReadTimeout(int time) {
        this.readTimeout = time;
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
        connectThread.setConnectTimeout(connectTimeout);
        connectThread.setReadTimeout(readTimeout);
        connectThread.setHttpURLConnectionListener(httpURLConnectionListener);
        entry.state = DownloadState.CONNECT;
        mExecutors.execute(connectThread);
        notifyUpdate(DConstants.NOTIFY_CONNECTING);
    }

    private void d(String msg) {
        DLog.d(TAG + "--> " + entry.id + " " + msg);
    }

    public void pause() {
        d("pause");
        if (connectThread != null && connectThread.isRunning()) {
            //连接中被取消
            connectThread.cancel();
            entry.state = DownloadState.PAUSED;
            notifyUpdate(DConstants.NOTIFY_PAUSED);
            return;
        }
        if (entry.isRange()) {
            //暂停多线程下载
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null && threads[i].isRunning()) {
                    threads[i].pause();
                }
            }
            entry.state = DownloadState.PAUSED;
            notifyUpdate(DConstants.NOTIFY_PAUSED);
        } else {
            //单线程下载不支持暂停操作，所以取消
            cancel();
        }
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
        entry.state = DownloadState.CANCELLED;
        entry.reset();
        notifyUpdate(DConstants.NOTIFY_CANCELLED);
    }

    private void download() {
        d("download");
        tryDataFix();
        //记录触发下载的时间戳
        initDownloadTempData();
        entry.state = DownloadState.ING;
        notifyUpdate(DConstants.NOTIFY_ING);
        if (entry.isRange()) {
            multithreadingDownload();
        } else {
            singleThreadDownload();
        }
    }

    /**
     * 修复数据
     * 1.本地文件已删除，下载信息重置
     */
    private void tryDataFix() {
        if (entry.currentLength > 0 && !new File(entry.getDir(), entry.getName()).exists()) {
            entry.reset();
            dao.newOrUpdate(entry);
        }
    }

    private void multithreadingDownload() {
        d("startMultithreadingDownload");
        threads = new MultiDownloadThread[maxThreads];
        states = new DownloadState[maxThreads];
        long start;
        long end;
        if (entry.ranges == null) {
            entry.ranges = new HashMap<>();
            for (int i = 0; i < maxThreads; i++) {
                entry.ranges.put(i, 0L);
            }
        }
        long block = (int) (entry.contentLength / threads.length);
        for (int i = 0; i < threads.length; i++) {
            start = i * block + entry.ranges.get(i);
            if (i != threads.length - 1) {
                end = (i + 1) * block - 1;
            } else {
                end = (int) entry.contentLength;
            }
            if (start < end) {
                threads[i] = new MultiDownloadThread(entry.id, entry.url, destFile, i, start, end, this);
                threads[i].setConnectTimeout(connectTimeout);
                threads[i].setHttpURLConnectionListener(httpURLConnectionListener);
                threads[i].setReadTimeout(readTimeout);
                states[i] = DownloadState.ING;
                mExecutors.execute(threads[i]);
            } else {
                states[i] = DownloadState.DONE;
            }
        }
    }

    private void singleThreadDownload() {
        d("startSingleThreadDownload");
        threads = new SingleDownloadThread[1];
        states = new DownloadState[1];
        threads[0] = new SingleDownloadThread(entry.id, entry.url, destFile, this);
        threads[0].setHttpURLConnectionListener(httpURLConnectionListener);
        threads[0].setConnectTimeout(connectTimeout);
        threads[0].setReadTimeout(readTimeout);
        states[0] = DownloadState.ING;
        entry.state = DownloadState.ING;
        mExecutors.execute(threads[0]);
        notifyUpdate(DConstants.NOTIFY_ING);
    }

    public synchronized void notifyUpdate(int what) {
        if (dao != null && entry.isRange()) {
            dao.newOrUpdate(entry);
        }
        if (listener != null) {
            listener.onTaskUpdate(what, entry.file);
        }
    }

    public interface OnDownloadTaskListener {
        void onTaskUpdate(int what, DownloadInfo file);
    }

    @Override
    public synchronized void onConnectCompleted(long contentLength, boolean isSupportRange) {
        d("onConnectCompleted contentLength:" + contentLength + ",isSupportRange:" + isSupportRange);
        entry.setRange(isSupportRange && entry.isRange());
        entry.contentLength = contentLength;
        download();
    }

    @Override
    public synchronized void onConnectError(String msg) {
        d("onConnectError " + msg);
        entry.state = DownloadState.ERROR;
        if (currentRetryIndex < maxRetryCount) {
            d("onConnectError retry connect " + currentRetryIndex);
            currentRetryIndex++;
            connect();
            return;
        }
        currentRetryIndex = 0;
        notifyUpdate(DConstants.NOTIFY_ERROR);
    }

    /**
     * 缓存当前进度
     */
    private volatile long tempCurrentLength;
    /**
     * 缓存进度的时间戳
     */
    private volatile long timestamp;

    private void initDownloadTempData() {
        timestamp = SystemClock.elapsedRealtime();
        tempCurrentLength = entry.currentLength;
    }

    @Override
    public synchronized void onDownloadProgressUpdate(int index, long progress) {
        entry.currentLength += progress;
        if (entry.isRange() && entry.ranges != null) {
            entry.ranges.put(index, entry.ranges.get(index) + progress);
        }
        if (mSpeedTickTack.needToNotify()) {
            long now = SystemClock.elapsedRealtime();
            //计算下载速度
            float time = (now - timestamp) / 1000f;
            entry.speed = (int) ((entry.currentLength - tempCurrentLength) / time);
            tempCurrentLength = entry.currentLength;
            timestamp = now;
        }
        if (mProgressTickTack.needToNotify()) {
            d("thread[" + index + "] progress " + entry.currentLength + "/" + entry.contentLength + " speed:" + entry.speed + "/s");
            notifyUpdate(DConstants.NOTIFY_PROGRESS_UPDATE);
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        d("onDownloadCompleted thread[" + index + "]");
        states[index] = DownloadState.DONE;
        for (DownloadState state : states) {
            if (state != DownloadState.DONE) {
                return;
            }
        }
        entry.state = DownloadState.DONE;
        notifyUpdate(DConstants.NOTIFY_COMPLETED);
    }

    @Override
    public synchronized void onDownloadError(int index, String msg) {
        d(" onDownloadError " + msg + " thread[" + index + "]");
        states[index] = DownloadState.ERROR;
        for (int i = 0; i < states.length; i++) {
            if (states[i] == DownloadState.ING) {
                threads[i].cancelByError();
            }
        }
        //只有支持断点续传 才能进行重试恢复下载操作
        if (entry.isRange() && currentRetryIndex < maxRetryCount) {
            d(" thread[" + index + "] error retry " + currentRetryIndex);
            currentRetryIndex++;
            download();
        } else {
            if (!entry.isRange()) {
                //不支持断点下载 要把缓存文件删掉
                destFile.deleteOnExit();
                entry.reset();
            }
            entry.state = DownloadState.ERROR;
            notifyUpdate(DConstants.NOTIFY_ERROR);
        }
    }
}