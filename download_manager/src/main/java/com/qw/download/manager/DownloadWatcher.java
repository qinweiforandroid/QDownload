package com.qw.download.manager;

import com.qw.download.entities.DownloadFile;

import java.util.Observable;
import java.util.Observer;

/**
 * 下载观察者
 * Created by qinwei on 2016/4/14 16:06
 * email:qinwei_it@163.com
 */
public abstract class DownloadWatcher implements Observer {
    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof DownloadFile) {
            onChanged((DownloadFile) data);
        }
    }

    protected abstract void onChanged(DownloadFile d);
}
