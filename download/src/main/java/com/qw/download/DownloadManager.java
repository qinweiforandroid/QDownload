package com.qw.download;

import android.content.Context;
import android.content.Intent;

/**
 * 下载框架总控制器
 * Created by qinwei on 2016/4/14 16:04
 * email:qinwei_it@163.com
 */
public class DownloadManager {
    public static final String TAG = "DownloadManager";
    private static DownloadManager mInstance;
    private Context context;

    public DownloadManager(Context context) {
        this.context = context;
        context.startService(new Intent(context, DownloadService.class));
    }

    public static DownloadManager getInstance(Context context) {
        synchronized (DownloadManager.class) {
            if (mInstance == null) {
                mInstance = new DownloadManager(context);
            }
            return mInstance;
        }
    }

    /**
     * 开启下载
     */
    public void addDownload(DownloadEntity entity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadConstants.KEY_DOWNLOAD_ENTITY, entity);
        intent.putExtra(DownloadConstants.KEY_DOWNLOAD_ACTION, DownloadConstants.KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }

    /**
     * 暂停下载
     *
     * @param entity
     */
    public void pauseDownload(DownloadEntity entity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadConstants.KEY_DOWNLOAD_ENTITY, entity);
        intent.putExtra(DownloadConstants.KEY_DOWNLOAD_ACTION, DownloadConstants.KEY_DOWNLOAD_ACTION_PAUSE);
        context.startService(intent);
    }

    /**
     * 恢复下载
     *
     * @param entity
     */
    public void resumeDownload(DownloadEntity entity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadConstants.KEY_DOWNLOAD_ENTITY, entity);
        intent.putExtra(DownloadConstants.KEY_DOWNLOAD_ACTION, DownloadConstants.KEY_DOWNLOAD_ACTION_RESUME);
        context.startService(intent);
    }

    /**
     * 取消下载
     *
     * @param entity
     */
    public void cancelDownload(DownloadEntity entity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadConstants.KEY_DOWNLOAD_ENTITY, entity);
        intent.putExtra(DownloadConstants.KEY_DOWNLOAD_ACTION, DownloadConstants.KEY_DOWNLOAD_ACTION_CANCEL);
        context.startService(intent);
    }

    public void addObserver(DownloadWatcher watcher) {
        DownloadChanger.getInstance(context).addObserver(watcher);
    }

    public void removeObserver(DownloadWatcher watcher) {
        DownloadChanger.getInstance(context).deleteObserver(watcher);
    }
}
