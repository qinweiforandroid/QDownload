package com.qw.download;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.qw.download.db.DownloadDBManager;
import com.qw.download.utilities.DConstants;
import com.qw.download.utilities.DLog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static com.qw.download.DownloadEntry.State;

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

    private static final String TAG = "DownloadService";
    /**
     * 保存等待下载的任务队列
     */
    private final LinkedBlockingQueue<DownloadEntry> mQueues = new LinkedBlockingQueue<>();
    /**
     * 保存正在下载的任务
     */
    private final HashMap<String, DownloadTask> mTasks = new HashMap<>();
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
                default:
                    break;
            }
        }
    };

    public void executeNext(DownloadEntry e) {
        DownloadTask task = mTasks.remove(e.id);
        if (task != null) {
            DownloadEntry entity = mQueues.poll();
            if (entity != null) {
                d(entity.id + " from mQueues poll");
                add(entity);
            }
        }
        d("mTasks size " + mTasks.size());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DownloadConfig.getInstance().isAutoResume()) {
            Set<Map.Entry<String, DownloadEntry>> entries = DownloadChanger.getInstance().getDownloadEntries().entrySet();
            for (Map.Entry<String, DownloadEntry> entryEntry : entries) {
                switch (entryEntry.getValue().state) {
                    case ING:
                    case WAIT:
                        add(entryEntry.getValue());
                        break;
                }
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
            case DConstants.KEY_ACTION_PAUSE_ALL:
                pauseAll();
                break;
            case DConstants.KEY_ACTION_RECOVER_ALL:
                recoverAll();
                break;
            default:
                break;
        }
    }

    private void add(DownloadEntry entry) {
        if (entry.isDone()) {
            return;
        }
        d(entry.id + " add");
        DownloadChanger.getInstance().addOperationTasks(entry);
        if (mTasks.size() >= DownloadConfig.getInstance().getMaxTask()) {
            addQueues(entry);
        } else {
            start(entry);
        }
    }

    private void addQueues(DownloadEntry entry) {
        if (entry.isDone()) {
            return;
        }
        d(entry.id + " addQueues");
        entry.state = State.WAIT;
        mQueues.offer(entry);
        DownloadChanger.getInstance().notifyDataChanged(entry);
    }


    private void start(DownloadEntry entry) {
        File destFile = DownloadConfig.getInstance().getDownloadFile(entry.url);
        //check 已下载文件被误删数据恢复初始状态
        if (!destFile.exists() && entry.currentLength > 0) {
            entry.reset();
        }
        if (entry.isDone()) {
            return;
        }

        d(entry.id + " start");
        DownloadTask task = new DownloadTask(entry, mExecutors, handler);
        mTasks.put(entry.id, task);
        task.start();
    }

    private void d(String msg) {
        DLog.d(TAG + "--> " + msg);
    }

    private void resume(DownloadEntry entry) {
        d(entry.id + " resume");
        add(entry);
    }

    private void pause(DownloadEntry entry) {
        d(entry.id + " pause");
        DownloadTask task = mTasks.get(entry.id);
        if (task != null) {
            //正在进行的task 暂停操作
            task.pause();
        } else {
            //下载队列的task 暂停操作
            entry.state = State.PAUSED;
            mQueues.remove(entry);
            DownloadChanger.getInstance().notifyDataChanged(entry);
        }
    }

    private void cancel(DownloadEntry entry) {
        d(entry.id + " cancel");
        DownloadTask task = mTasks.get(entry.id);
        if (task != null) {
            task.cancel();
        } else {
            entry.state = State.CANCELLED;
            mQueues.remove(entry);
            DownloadChanger.getInstance().notifyDataChanged(entry);
        }
        DownloadChanger.getInstance().deleteOperationTasks(entry);
        DownloadDBManager.getInstance().delete(entry);
    }

    private void pauseAll() {
        d("pauseAll");
        //1.清除队列
        mQueues.clear();
        for (Map.Entry<String, DownloadTask> entry : mTasks.entrySet()) {
            //暂停当前task
            entry.getValue().pause();
        }
    }

    private void recoverAll() {
        d("recoverAll");
        //恢复下载
        for (Map.Entry<String, DownloadEntry> entry : DownloadChanger.getInstance().getDownloadEntries().entrySet()) {
            if (entry.getValue().state != State.DONE) {
                add(entry.getValue());
            }
        }
    }
}
