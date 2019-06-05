package com.qw.download.core;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.qw.download.utilities.DLog;
import com.qw.download.notify.DownloadChanger;
import com.qw.download.DownloadConfig;
import com.qw.download.utilities.DownloadConstants;
import com.qw.download.db.DownloadDBController;
import com.qw.download.entities.DownloadEntity;
import com.qw.download.utilities.DownloadFileUtil;

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
    public Handler handler = new Handler(Looper.getMainLooper()) {
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
        if (task != null) {
            DownloadEntity entity = mDownloadWaitQueues.poll();
            if (entity != null) {
                DLog.d("Waiting Queues  poll execute next download task name is " + e.url);
                add(entity);
            } else {
                DLog.d("All download task execute completed ");
            }
        }
    }

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
        DLog.d("initDownload (Executors、mDownloadWaitQueues、mDownloadingTasks、DB、DownloadChanger)");
        mExecutors = Executors.newCachedThreadPool();
        mDownloadWaitQueues = new LinkedBlockingQueue<>();
        mDownloadingTasks = new HashMap<>();
        DownloadDBController.init(this);
        ArrayList<DownloadEntity> es = DownloadDBController.getInstance().queryAll();
        DownloadChanger.getInstance(getApplicationContext()).init(es);
        for (int i = 0; i < es.size(); i++) {
            DownloadEntity e = es.get(i);
            if (e.state == DownloadEntity.State.ing || e.state == DownloadEntity.State.wait) {
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
                pause(entity);
                break;
            case DownloadConstants.KEY_DOWNLOAD_ACTION_RESUME:
                resume(entity);
                break;
            case DownloadConstants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancel(entity);
                break;
            case DownloadConstants.KEY_DOWNLOAD_ACTION_STOP_ALL:
                stopAll();
                break;
            case DownloadConstants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll();
                break;
            default:
                break;
        }
    }

    private void add(DownloadEntity entity) {
        DownloadChanger.getInstance(getApplicationContext()).addOperationTasks(entity);
        if (mDownloadingTasks.size() >= DownloadConfig.getInstance().getMaxDownloadTasks()) {
            addQueues(entity);
        } else {
            start(entity);
        }
    }

    private void addQueues(DownloadEntity entity) {
        DLog.d("add download queues task id=" + entity.id);
        entity.state = DownloadEntity.State.wait;
        mDownloadWaitQueues.offer(entity);
        DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(entity);
        DLog.d("add download queues then queues size " + mDownloadWaitQueues.size());
    }

    private void start(DownloadEntity entity) {
        DLog.d("add download task id=" + entity.id);
        DownloadTask task = new DownloadTask(entity, mExecutors, handler);
        mDownloadingTasks.put(entity.id, task);
        DLog.d("add download task then downloadingTasks size " + mDownloadingTasks.size());
        task.start();
    }

    private void resume(DownloadEntity entity) {
        DLog.d("resume download task id " + entity.id);
        add(entity);
    }

    private void pause(DownloadEntity entity) {
        DLog.d("pause download task id " + entity.id);
        DownloadTask task = mDownloadingTasks.get(entity.id);
        if (task != null) {
            //正在进行的task 暂停操作
            task.pause();
        } else {
            //下载队列的task 暂停操作
            entity.state = DownloadEntity.State.paused;
            mDownloadWaitQueues.remove(entity);
            DLog.d("queues poll queues size " + mDownloadWaitQueues.size());
            DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(entity);
        }
    }

    private void cancel(DownloadEntity entity) {
        DLog.d("cancel download task id " + entity.id);
        DownloadTask task = mDownloadingTasks.get(entity.id);
        if (task != null) {
            task.cancel();
        } else {
            entity.state = DownloadEntity.State.cancelled;
            mDownloadWaitQueues.remove(entity);
            DLog.d("queues poll queues size " + mDownloadWaitQueues.size());
            DownloadChanger.getInstance(getApplicationContext()).notifyDataChanged(entity);
        }
        DownloadDBController.getInstance().delete(entity);
    }

    private void stopAll() {
        DLog.d("stopAll");
    }

    private void recoverAll() {
        DLog.d("recoverAll");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DLog.d("DownloadService onDestroy");
    }
}
