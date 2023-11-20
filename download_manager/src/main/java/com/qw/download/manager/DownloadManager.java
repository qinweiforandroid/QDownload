package com.qw.download.manager;

import android.content.Context;
import android.content.Intent;

import com.qw.download.db.dto.DownloadEntry;
import com.qw.download.DownloadInfo;
import com.qw.download.utilities.DConstants;
import com.qw.download.utilities.DLog;

import java.util.ArrayList;


/**
 * 下载框架总控制器
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadManager {
    public static final String TAG = "DownloadManager";
    private static DownloadManager mInstance;
    private final Context context;

    private DownloadManager(Context context) {
        this.context = context.getApplicationContext();
    }

    private static void checkInit() {
        if (mInstance == null) {
            throw new IllegalArgumentException("downloadManager not init");
        }
    }

    private static void d(String msg) {
        DLog.d(TAG + "--> " + msg);
    }

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadManager(context);
            DownloadConfig.getInstance().getDao().init(context.getApplicationContext());
            ArrayList<DownloadEntry> entries = DownloadConfig.getInstance().getDao().queryAll();
            DownloadChanger.getInstance().setDownloadEntries(entries);
            if (DownloadConfig.getInstance().isAutoResume()) {
                Intent intent = new Intent(mInstance.context, DownloadService.class);
                context.startService(intent);
            }
        }
    }

    private static DownloadEntry build(FileRequest request) {
        DownloadEntry obtain = DownloadEntry.obtain(request.getId(), request.getUrl());
        obtain.setDir(request.getDir())
                .setName(request.getName())
                .setRange(request.isRange());
        return obtain;
    }

    /**
     * 开启下载
     */
    public static void add(FileRequest request) {
        checkInit();
        DownloadEntry entry = build(request);
        d(entry.id + " add");
        Intent intent = new Intent(mInstance.context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ENTRY, entry);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_ADD);
        mInstance.context.startService(intent);
    }

    /**
     * 暂停下载
     *
     * @param id
     */
    public static void pause(String id) {
        checkInit();
        d(id + " pause");
        DownloadEntry entry = findById(id);
        if (entry == null) return;
        Intent intent = new Intent(mInstance.context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ENTRY, entry);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_PAUSE);
        mInstance.context.startService(intent);
    }

    public static void pauseAll() {
        checkInit();
        d("pauseAll");
        Intent intent = new Intent(mInstance.context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_PAUSE_ALL);
        mInstance.context.startService(intent);
    }

    /**
     * 恢复下载
     *
     * @param id
     */
    public static void resume(String id) {
        checkInit();
        d(id + " resume");
        DownloadEntry entry = findById(id);
        if (entry == null) return;
        Intent intent = new Intent(mInstance.context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ENTRY, entry);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_RESUME);
        mInstance.context.startService(intent);
    }

    public static void recoverAll() {
        checkInit();
        d("recoverAll");
        Intent intent = new Intent(mInstance.context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_RECOVER_ALL);
        mInstance.context.startService(intent);
    }

    /**
     * 取消下载
     *
     * @param id
     */
    public static void cancel(String id) {
        checkInit();
        d(id + " cancel");
        DownloadEntry entry = findById(id);
        if (entry == null) return;
        Intent intent = new Intent(mInstance.context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ENTRY, entry);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_CANCEL);
        mInstance.context.startService(intent);
    }

    public static void addObserver(DownloadWatcher watcher) {
        checkInit();
        DownloadChanger.getInstance().addObserver(watcher);
    }

    public static void removeObserver(DownloadWatcher watcher) {
        checkInit();
        DownloadChanger.getInstance().deleteObserver(watcher);
    }

    private static DownloadEntry findById(String id) {
        return DownloadChanger.getInstance().get(id);
    }

    public static DownloadInfo getFile(String id) {
        DownloadEntry entry = findById(id);
        if (entry == null) {
            return null;
        }
        return entry.file;
    }
}