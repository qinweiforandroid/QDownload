package com.qw.download;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.qw.download.utilities.DLog;
import com.qw.download.utilities.DConstants;
import com.qw.download.db.DownloadDBController;

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
    public static final int NOTIFY_CONNECTING = 0;
    public static final int NOTIFY_ERROR = 1;
    public static final int NOTIFY_ING = 2;
    public static final int NOTIFY_PROGRESS_UPDATE = 3;
    public static final int NOTIFY_COMPLETED = 4;
    public static final int NOTIFY_PAUSED = 5;
    public static final int NOTIFY_CANCELLED = 6;

    private LinkedBlockingQueue<DownloadEntry> mDownloadWaitQueues = new LinkedBlockingQueue<>();//保存等待下载的任务队列
    private HashMap<String, DownloadTask> mDownloadingTasks = new HashMap<>();//保存正在下载的任务
    private final ExecutorService mExecutors = Executors.newCachedThreadPool();

    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            DownloadEntry e = (DownloadEntry) msg.obj;
            DownloadChanger.getInstance().notifyDataChanged(e);
            switch (msg.what) {
                case NOTIFY_ERROR:
                case NOTIFY_COMPLETED:
                case NOTIFY_PAUSED:
                case NOTIFY_CANCELLED:
                    executeNext(e);
                    break;
                case NOTIFY_CONNECTING:
                case NOTIFY_ING:
                case NOTIFY_PROGRESS_UPDATE:
                    break;
                default:
                    break;
            }
        }
    };

    public void executeNext(DownloadEntry e) {
        DownloadTask task = mDownloadingTasks.remove(e.id);
        if (task != null) {
            DownloadEntry entity = mDownloadWaitQueues.poll();
            if (entity != null) {
                DLog.d("Waiting Queues  poll execute next download task name is " + e.url);
                add(entity);
            }
        }
        if (mDownloadingTasks.size() == 0) {
            DLog.d("All download task execute completed ");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DLog.d("init (Executors、mDownloadWaitQueues、mDownloadingTasks、DB、DownloadChanger)");
        DownloadDBController.init(this);
        ArrayList<DownloadEntry> es = DownloadDBController.getInstance().queryAll();
        DownloadChanger.getInstance().init(es);
        for (int i = 0; i < es.size(); i++) {
            DownloadEntry e = es.get(i);
            if (e.state == DownloadEntry.State.ing || e.state == DownloadEntry.State.wait) {
                add(e);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int action = intent.getIntExtra(DConstants.KEY_ACTION, -1);
            if (action != -1) {
                doAction(action, (DownloadEntry) intent.getSerializableExtra(DConstants.KEY_ENTRY));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entity) {
        switch (action) {
            case DConstants.KEY_ACTION_ADD:
                add(entity);
                break;
            case DConstants.KEY_ACTION_PAUSE:
                pause(entity);
                break;
            case DConstants.KEY_ACTION_RESUME:
                resume(entity);
                break;
            case DConstants.KEY_ACTION_CANCEL:
                cancel(entity);
                break;
            case DConstants.KEY_ACTION_STOP_ALL:
                stopAll();
                break;
            case DConstants.KEY_ACTION_RECOVER_ALL:
                recoverAll();
                break;
            default:
                break;
        }
    }

    private void add(DownloadEntry entry) {
        DLog.d("add " + entry.id);
        DownloadChanger.getInstance().addOperationTasks(entry);
        if (mDownloadingTasks.size() >= DownloadConfig.getInstance().getMaxTask()) {
            addQueues(entry);
        } else {
            start(entry);
        }
    }

    private void addQueues(DownloadEntry entry) {
        DLog.d("addQueues " + entry.id);
        entry.state = DownloadEntry.State.wait;
        mDownloadWaitQueues.offer(entry);
        DownloadChanger.getInstance().notifyDataChanged(entry);
    }


    private void start(DownloadEntry entry) {
        DLog.d("start " + entry.id);
        DownloadTask task = new DownloadTask(entry, mExecutors, handler);
        mDownloadingTasks.put(entry.id, task);
        task.start();
    }

    private void resume(DownloadEntry entry) {
        DLog.d("resume " + entry.id);
        add(entry);
    }

    private void pause(DownloadEntry entry) {
        DLog.d("pause " + entry.id);
        DownloadTask task = mDownloadingTasks.get(entry.id);
        if (task != null) {
            //正在进行的task 暂停操作
            task.pause();
        } else {
            //下载队列的task 暂停操作
            entry.state = DownloadEntry.State.paused;
            mDownloadWaitQueues.remove(entry);
            DownloadChanger.getInstance().notifyDataChanged(entry);
        }
    }

    private void cancel(DownloadEntry entry) {
        DLog.d("cancel " + entry.id);
        DownloadTask task = mDownloadingTasks.get(entry.id);
        if (task != null) {
            task.cancel();
        } else {
            entry.state = DownloadEntry.State.cancelled;
            mDownloadWaitQueues.remove(entry);
            DownloadChanger.getInstance().notifyDataChanged(entry);
        }
        DownloadChanger.getInstance().deleteOperationTasks(entry);
        DownloadDBController.getInstance().delete(entry);
    }

    private void stopAll() {
        DLog.d("stopAll");
    }

    private void recoverAll() {
        DLog.d("recoverAll");
    }
}
