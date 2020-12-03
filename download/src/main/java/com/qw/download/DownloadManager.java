package com.qw.download;

import android.content.Context;
import android.content.Intent;

import com.qw.download.core.DownloadService;
import com.qw.download.entities.DownloadEntry;
import com.qw.download.notify.DownloadChanger;
import com.qw.download.notify.DownloadWatcher;
import com.qw.download.utilities.DConstants;

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
    public void addDownload(DownloadEntry entity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DConstants.KEY_DOWNLOAD_ENTRY, entity);
        intent.putExtra(DConstants.KEY_DOWNLOAD_ACTION, DConstants.KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }

    /**
     * 暂停下载
     *
     * @param entity
     */
    public void pauseDownload(DownloadEntry entity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DConstants.KEY_DOWNLOAD_ENTRY, entity);
        intent.putExtra(DConstants.KEY_DOWNLOAD_ACTION, DConstants.KEY_DOWNLOAD_ACTION_PAUSE);
        context.startService(intent);
    }

    /**
     * 恢复下载
     *
     * @param entity
     */
    public void resumeDownload(DownloadEntry entity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DConstants.KEY_DOWNLOAD_ENTRY, entity);
        intent.putExtra(DConstants.KEY_DOWNLOAD_ACTION, DConstants.KEY_DOWNLOAD_ACTION_RESUME);
        context.startService(intent);
    }

    /**
     * 取消下载
     *
     * @param entity
     */
    public void cancelDownload(DownloadEntry entity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DConstants.KEY_DOWNLOAD_ENTRY, entity);
        intent.putExtra(DConstants.KEY_DOWNLOAD_ACTION, DConstants.KEY_DOWNLOAD_ACTION_CANCEL);
        context.startService(intent);
    }

    public void addObserver(DownloadWatcher watcher) {
        DownloadChanger.getInstance(context).addObserver(watcher);
    }

    public void removeObserver(DownloadWatcher watcher) {
        DownloadChanger.getInstance(context).deleteObserver(watcher);
    }

    public DownloadEntry findById(String id) {
        return DownloadChanger.getInstance(context).findById(id);
    }
}
