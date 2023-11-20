package com.qw.download.manager;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.qw.download.DownloadTask;
import com.qw.download.db.dto.DownloadEntry;
import com.qw.download.db.dto.DownloadState;
import com.qw.download.DownloadInfo;
import com.qw.download.utilities.DConstants;
import com.qw.download.utilities.DLog;
import com.qw.download.utilities.FileUtil;
import com.qw.download.utilities.TickTack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 下载服务
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadService extends Service {

    private static final String TAG = "DownloadService";
    /**
     * 等待队列
     */
    private final LinkedBlockingQueue<DownloadEntry> mQueues = new LinkedBlockingQueue<>();
    /**
     * 正在下载的任务
     */
    private final HashMap<String, DownloadTask> mTasks = new HashMap<>();

    private final ExecutorService mExecutors = Executors.newCachedThreadPool();
    final DownloadConfig config = DownloadConfig.getInstance();
    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            DownloadInfo e = (DownloadInfo) msg.obj;
            DownloadChanger.getInstance().notifyDataChanged(e);
            switch (msg.what) {
                case DConstants.NOTIFY_ERROR:
                case DConstants.NOTIFY_COMPLETED:
                case DConstants.NOTIFY_PAUSED:
                case DConstants.NOTIFY_CANCELLED:
                    executeNext(e.getId());
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 进度更新频率控制
     */
    private final TickTack progressTickTack = new TickTack(1000);

    public void executeNext(String id) {
        DownloadTask task = mTasks.remove(id);
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
                        d("recover " + entryEntry.getValue().id + " " +
                                entryEntry.getValue().currentLength + "/" +
                                entryEntry.getValue().contentLength + " ranges:" +
                                entryEntry.getValue().ranges.size() + " state " +
                                entryEntry.getValue().state.name());
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
        configEntry(entry);
        d(entry.id + " add");
        DownloadChanger.getInstance().addOperationTasks(entry);
        if (mTasks.size() >= DownloadConfig.getInstance().getMaxTask()) {
            addQueues(entry);
        } else {
            start(entry);
        }
    }

    private void configEntry(DownloadEntry entry) {
        String dir = entry.getDir();
        if (TextUtils.isEmpty(dir)) {
            dir = DownloadConfig.getInstance().getDownloadDir();
            entry.setDir(dir);
        }
        String name = entry.getName();
        if (TextUtils.isEmpty(name)) {
            name = FileUtil.getMd5FileName(entry.url);
            entry.setName(name);
        }
    }

    private void addQueues(DownloadEntry entry) {
        if (entry.isDone()) {
            return;
        }
        d(entry.id + " addQueues");
        entry.state = DownloadState.WAIT;
        mQueues.offer(entry);
        DownloadChanger.getInstance().notifyDataChanged(entry.file);
    }


    private void start(DownloadEntry entry) {
        File destFile = new File(entry.getDir(), entry.getName());
        if (!destFile.exists() && entry.currentLength > 0) {
            //check 已下载文件被误删数据恢复初始状态
            entry.reset();
        }
        if (entry.isDone()) {
            return;
        }
        d(entry.id + " start");
        DownloadTask task = new DownloadTask(entry, mExecutors, progressTickTack);
        task.setConnectTimeout(config.getConnectTimeout());
        task.setReadTimeout(config.getReadTimeout());
        task.setMaxThread(config.getMaxThread());
        task.setMaxRetryCount(config.getMaxRetryCount());
        task.setDao(config.getDao());
        task.setHttpURLConnectionListener(config.getHttpURLConnectionListener());
        task.setOnDownloadTaskListener((what, file) -> {
            Message msg = Message.obtain();
            msg.what = what;
            msg.obj = file;
            handler.sendMessage(msg);
        });
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
            entry.state = DownloadState.PAUSED;
            mQueues.remove(entry);
            DownloadChanger.getInstance().notifyDataChanged(entry.file);
        }
    }

    private void cancel(DownloadEntry entry) {
        d(entry.id + " cancel");
        DownloadTask task = mTasks.get(entry.id);
        if (task != null) {
            task.cancel();
        } else {
            entry.state = DownloadState.CANCELLED;
            mQueues.remove(entry);
            DownloadChanger.getInstance().notifyDataChanged(entry.file);
        }
        DownloadChanger.getInstance().deleteOperationTasks(entry);
        DownloadConfig.getInstance().getDao().delete(entry.id);
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
            if (entry.getValue().state != DownloadState.DONE) {
                add(entry.getValue());
            }
        }
    }
}