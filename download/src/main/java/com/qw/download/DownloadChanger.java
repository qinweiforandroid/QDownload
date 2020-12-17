package com.qw.download;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Observable;

/**
 * download 改变者
 * Created by qinwei on 2016/4/14 16:06
 * email:qinwei_it@163.com
 */
class DownloadChanger extends Observable {
    private static DownloadChanger mInstance;
    /**
     * 用于暂停恢复下载缓存
     */
    private LinkedHashMap<String, DownloadEntry> mOperationTasks;

    public DownloadChanger() {
        mOperationTasks = new LinkedHashMap<>();
    }

    public static DownloadChanger getInstance() {
        synchronized (DownloadChanger.class) {
            if (mInstance == null) {
                mInstance = new DownloadChanger();
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

    public void init(ArrayList<DownloadEntry> list) {
        for (int i = 0; i < list.size(); i++) {
            addOperationTasks(list.get(i));
        }
    }

    public void deleteOperationTasks(DownloadEntry d) {
        mOperationTasks.remove(d.id);
    }
}
