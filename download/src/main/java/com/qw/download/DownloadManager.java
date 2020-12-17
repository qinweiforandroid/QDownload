package com.qw.download;

import android.content.Context;
import android.content.Intent;

import com.qw.download.utilities.DConstants;

import java.io.File;

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
        context.startService(new Intent(context, DownloadService.class));
    }

    public static DownloadManager getInstance() {
        if (mInstance == null) {
            throw new IllegalArgumentException("downloadManager not init");
        }
        return mInstance;
    }

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadManager(context);
        }
    }

    /**
     * 开启下载
     */
    public void add(DownloadEntry entry) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ENTRY, entry);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_ADD);
        context.startService(intent);
    }

    /**
     * 暂停下载
     *
     * @param entry
     */
    public void pause(DownloadEntry entry) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ENTRY, entry);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_PAUSE);
        context.startService(intent);
    }

    /**
     * 恢复下载
     *
     * @param entry
     */
    public void resume(DownloadEntry entry) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ENTRY, entry);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_RESUME);
        context.startService(intent);
    }

    /**
     * 取消下载
     *
     * @param entry
     */
    public void cancel(DownloadEntry entry) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DConstants.KEY_ENTRY, entry);
        intent.putExtra(DConstants.KEY_ACTION, DConstants.KEY_ACTION_CANCEL);
        context.startService(intent);
    }

    public void addObserver(DownloadWatcher watcher) {
        DownloadChanger.getInstance().addObserver(watcher);
    }

    public void removeObserver(DownloadWatcher watcher) {
        DownloadChanger.getInstance().deleteObserver(watcher);
    }

    public DownloadEntry findById(String id) {
        return DownloadChanger.getInstance().findById(id);
    }
}