package com.qw.download;

import android.content.Context;
import android.content.Intent;

import com.qw.download.db.DownloadDBManager;
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
        DownloadDBManager.init(this.context);
        ArrayList<DownloadEntry> entries = DownloadDBManager.getInstance().queryAll();
        DownloadChanger.getInstance().setDownloadEntries(entries);
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
        }
    }

    /**
     * 开启下载
     */
    public static void add(DownloadEntry entry) {
        checkInit();
        d(entry.id + " add");
        Intent intent = new Intent(mInstance.context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ENTRY, entry);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_ADD);
        mInstance.context.startService(intent);
    }

    /**
     * 暂停下载
     *
     * @param entry
     */
    public static void pause(DownloadEntry entry) {
        checkInit();
        d(entry.id + " pause");
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
     * @param entry
     */
    public static void resume(DownloadEntry entry) {
        checkInit();
        d(entry.id + " resume");
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
     * @param entry
     */
    public static void cancel(DownloadEntry entry) {
        checkInit();
        d(entry.id + " cancel");
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

    public static DownloadEntry findById(String id) {
        checkInit();
        DownloadEntry entry = DownloadChanger.getInstance().get(id);
        if (entry != null) {
            d("findById " + entry.toString());
        }
        return entry;
    }
}