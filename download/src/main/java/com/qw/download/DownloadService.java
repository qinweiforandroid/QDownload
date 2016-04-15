package com.qw.download;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 下载服务
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadService extends Service {
    public static final String TAG = "DownloadService";
    public static final int NOTIFY_DOWNLOAD_CONNECTING = 0;
    public static final int NOTIFY_DOWNLOAD_ERROR = 1;
    public static final int NOTIFY_DOWNLOAD_ING = 2;
    public static final int NOTIFY_DOWNLOAD_PROGRESS_UPDATE = 3;
    public static final int NOTIFY_DOWNLOAD_COMPLETED = 4;
    public static final int NOTIFY_DOWNLOAD_PAUSED = 5;
    public LinkedBlockingQueue<DownloadEntity> mDownloadWaitQueues;//保存等待下载的任务队列
    public HashMap<String, DownloadTask> mDownloadingTasks;//保存正在下载的任务
    private ExecutorService mExecutors;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadEntity e = (DownloadEntity) msg.obj;
            DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(e);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutors = Executors.newCachedThreadPool();
        mDownloadWaitQueues = new LinkedBlockingQueue<>();
        mDownloadingTasks = new HashMap<>();
        DLog.d(TAG, "下载服务初始化");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int action = intent.getIntExtra(DownloadConstants.KEY_DOWNLOAD_ACTION, -1);
            if (action != -1) {
                doAction(action, (DownloadEntity) intent.getSerializableExtra(DownloadConstants.KEY_DOWNLOAD_ENTITY));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntity entity) {
        switch (action) {
            case DownloadConstants.KEY_DOWNLOAD_ACTION_ADD:
                add(entity);
                break;
            case DownloadConstants.KEY_DOWNLOAD_ACTION_PAUSE:
                stop(entity);
                break;
            case DownloadConstants.KEY_DOWNLOAD_ACTION_RESUME:
                resume(entity);
                break;
            case DownloadConstants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancel(entity);
                break;
            case DownloadConstants.KEY_DOWNLOAD_ACTION_STOP_ALL:
                stopAll(entity);
                break;
            case DownloadConstants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll(entity);
                break;
            default:
                break;
        }
    }

    private void add(DownloadEntity entity) {
        DownloadChanger.getInstance(getApplicationContext()).addOperationTasks(entity);
        DLog.d(TAG, entity.id + " add mDownloadingTasks size:" + mDownloadingTasks.size());
        if (mDownloadingTasks.size() >= DownloadConfig.MAX_DOWNLOAD_TASK_SIZE) {
            entity.state= DownloadEntity.State.wait;
            mDownloadWaitQueues.offer(entity);
            DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(entity);
            DLog.d(TAG, entity.id + " add 达到最大下载数 添加到等待队列mDownloadWaitQueues size:" + mDownloadWaitQueues.size());
        } else {
            start(entity);
        }
    }

    private void start(DownloadEntity entity) {
        DLog.d(TAG, entity.id + " start");
        DownloadTask task = new DownloadTask(entity, mExecutors, handler);
        mDownloadingTasks.put(entity.id, task);
        DLog.d(TAG, entity.id + " start 添加到下载任务中mDownloadingTasks size:" + mDownloadingTasks.size());
        task.start();
    }

    private void resume(DownloadEntity entity) {
        DLog.d(TAG, entity.id + " resume");
        add(entity);
    }

    private void stop(DownloadEntity entity) {
        DLog.d(TAG, entity.id + " pause");
        DownloadTask task=mDownloadingTasks.get(entity.id);
        if(task!=null){
            task.stop();
        }else{
            entity.state= DownloadEntity.State.paused;
            mDownloadWaitQueues.remove(entity);
            DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(entity);
        }
    }

    private void cancel(DownloadEntity entity) {
        DLog.d(TAG, entity.id + " cancel");
    }

    private void stopAll(DownloadEntity entity) {
        DLog.d(TAG, entity.id + " stopAll");
    }

    private void recoverAll(DownloadEntity entity) {
        DLog.d(TAG, entity.id + " recoverAll");
    }
}
