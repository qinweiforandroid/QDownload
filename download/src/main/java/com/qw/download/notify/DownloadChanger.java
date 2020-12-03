package com.qw.download.notify;

import android.content.Context;

import com.qw.download.entities.DownloadEntry;

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
    private LinkedHashMap<String, DownloadEntry> mOperationTasks;

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

    public void notifyDataChanged(DownloadEntry entity) {
        mOperationTasks.put(entity.id, entity);
        setChanged();
        notifyObservers(entity);
    }

    public void addOperationTasks(DownloadEntry entity) {
        mOperationTasks.put(entity.id, entity);
    }

    public DownloadEntry findById(String id) {
        return mOperationTasks.get(id);
    }

    public void init(ArrayList<DownloadEntry> es) {
        for (int i = 0; i < es.size(); i++) {
            addOperationTasks(es.get(i));
        }
    }

    public void deleteOperationTasks(DownloadEntry entity) {
        mOperationTasks.remove(entity.id);
    }
}
