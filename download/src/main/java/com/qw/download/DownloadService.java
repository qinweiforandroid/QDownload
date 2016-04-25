package com.qw.download;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.ArrayList;
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
    public static final int NOTIFY_DOWNLOAD_CANCELLED = 6;
    public LinkedBlockingQueue<DownloadEntity> mDownloadWaitQueues;//保存等待下载的任务队列
    public HashMap<String, DownloadTask> mDownloadingTasks;//保存正在下载的任务
    private ExecutorService mExecutors;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DownloadEntity e = (DownloadEntity) msg.obj;
            DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(e);
            switch (msg.what) {
                case NOTIFY_DOWNLOAD_ERROR:
                case NOTIFY_DOWNLOAD_COMPLETED:
                case NOTIFY_DOWNLOAD_PAUSED:
                case NOTIFY_DOWNLOAD_CANCELLED:
                    executeNext(e);
                    break;
                case NOTIFY_DOWNLOAD_CONNECTING:
                case NOTIFY_DOWNLOAD_ING:
                case NOTIFY_DOWNLOAD_PROGRESS_UPDATE:
                    break;
                default:
                    break;
            }
        }
    };

    public void executeNext(DownloadEntity e) {
        DownloadTask task = mDownloadingTasks.remove(e.id);
        DLog.d(TAG, "executeNext mDownloadingTasks.remove " + (task != null));
        if (task != null) {
            DownloadEntity entity = mDownloadWaitQueues.poll();
            if (entity != null) {
                DLog.d(TAG, entity.id + " executeNext mDownloadWaitQueues.poll() " + (task != null));
                add(entity);
            } else {
                DLog.d(TAG, "executeNext no task can run ");
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initDownload();
    }

    private void initDownload() {
        DLog.d(TAG, "initDownload (Executors、mDownloadWaitQueues、mDownloadingTasks、DB、DownloadChanger)");
        mExecutors = Executors.newCachedThreadPool();
        mDownloadWaitQueues = new LinkedBlockingQueue<>();
        mDownloadingTasks = new HashMap<>();
        DownloadDBController.getInstance(getApplicationContext()).getDB();
        ArrayList<DownloadEntity> es = DownloadDBController.getInstance(getApplicationContext()).queryAll();
        DownloadChanger.getInstance(getApplicationContext()).init(es);
        for (int i = 0; i < es.size(); i++) {
            DownloadEntity e = es.get(i);
            if(e.state== DownloadEntity.State.ing||e.state== DownloadEntity.State.wait){
                add(e);
            }
        }
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
            addQueues(entity);
        } else {
            start(entity);
        }
    }

    private void addQueues(DownloadEntity entity) {
        entity.state = DownloadEntity.State.wait;
        mDownloadWaitQueues.offer(entity);
        DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(entity);
        DLog.d(TAG, entity.id + " addQueues 添加到等待队列mDownloadWaitQueues size:" + mDownloadWaitQueues.size());
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
        DownloadTask task = mDownloadingTasks.get(entity.id);
        if (task != null) {
            task.pause();
        } else {
            entity.state = DownloadEntity.State.paused;
            mDownloadWaitQueues.remove(entity);
            DLog.d(TAG, entity.id + " pause mDownloadWaitQueues remove |size:" + mDownloadWaitQueues.size());
            DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(entity);
        }
    }

    private void cancel(DownloadEntity entity) {
        DLog.d(TAG, entity.id + " cancel");
        DownloadTask task = mDownloadingTasks.get(entity.id);
        if (task != null) {
            task.cancel();
        } else {
            entity.state = DownloadEntity.State.cancelled;
            mDownloadWaitQueues.remove(entity);
            DLog.d(TAG, entity.id + " cancel mDownloadWaitQueues remove |size:" + mDownloadWaitQueues.size());
            DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(entity);
        }
//        delete db data
        DownloadDBController.getInstance(getApplicationContext()).delete(entity);
    }

    private void stopAll(DownloadEntity entity) {
        DLog.d(TAG, entity.id + " stopAll");
    }

    private void recoverAll(DownloadEntity entity) {
        DLog.d(TAG, entity.id + " recoverAll");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DLog.d(TAG,"onDestroy");
    }
}
