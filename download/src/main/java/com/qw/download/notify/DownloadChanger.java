package com.qw.download.notify;

import android.content.Context;

import com.qw.download.entities.DownloadEntity;
import com.qw.download.db.DownloadDBController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Observable;

/**
 * download 改变者
 * Created by qinwei on 2016/4/14 16:06
 * email:qinwei_it@163.com
 */
public class DownloadChanger extends Observable {
    private static DownloadChanger mInstance;
    private Context context;
    /**
     * 用于暂停恢复下载缓存
     */
    private LinkedHashMap<String, DownloadEntity> mOperationTasks;

    public DownloadChanger(Context context) {
        this.context = context;
        mOperationTasks = new LinkedHashMap<>();
    }

    public static DownloadChanger getInstance(Context context) {
        synchronized (DownloadChanger.class) {
            if (mInstance == null) {
                mInstance = new DownloadChanger(context);
            }
            return mInstance;
        }
    }

    public void notifyDataChanged(DownloadEntity entity) {
        mOperationTasks.put(entity.id, entity);
        setChanged();
        notifyObservers(entity);
    }

    public void addOperationTasks(DownloadEntity entity) {
        mOperationTasks.put(entity.id, entity);
    }

    public DownloadEntity findById(String id) {
        return mOperationTasks.get(id);
    }

    public void init(ArrayList<DownloadEntity> es) {
        for (int i = 0; i < es.size(); i++) {
            addOperationTasks(es.get(i));
        }
    }

    public void deleteOperationTasks(DownloadEntity entity) {
        mOperationTasks.remove(entity.id);
    }
}
