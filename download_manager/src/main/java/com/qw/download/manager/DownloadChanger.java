package com.qw.download.manager;


import com.qw.download.db.DownloadEntry;
import com.qw.download.entities.DownloadFile;

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
    private LinkedHashMap<String, DownloadEntry> mDownloadEntries;

    public DownloadChanger() {
        mDownloadEntries = new LinkedHashMap<>();
    }

    public static DownloadChanger getInstance() {
        synchronized (DownloadChanger.class) {
            if (mInstance == null) {
                mInstance = new DownloadChanger();
            }
            return mInstance;
        }
    }

    public void notifyDataChanged(DownloadFile file) {
        setChanged();
        notifyObservers(file);
    }

    public void addOperationTasks(DownloadEntry entity) {
        mDownloadEntries.put(entity.id, entity);
    }

    public DownloadEntry get(String id) {
        return mDownloadEntries.get(id);
    }

    public void setDownloadEntries(ArrayList<DownloadEntry> list) {
        for (int i = 0; i < list.size(); i++) {
            addOperationTasks(list.get(i));
        }
    }

    public LinkedHashMap<String, DownloadEntry> getDownloadEntries() {
        return mDownloadEntries;
    }

    public void deleteOperationTasks(DownloadEntry d) {
        mDownloadEntries.remove(d.id);
    }
}
