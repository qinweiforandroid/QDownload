package com.qw.download.db;


import androidx.annotation.RestrictTo;

import com.qw.download.entities.DownloadFile;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by qinwei on 2016/4/14 17:16
 * email:qinwei_it@163.com
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DownloadEntry implements Serializable {

    /**
     * 唯一标识
     */
    public String id;

    /**
     * 文件地址
     */
    public String url;

    /**
     * 是否支持断点下载
     */
    private boolean range;

    /**
     * 文件长度
     */
    public long contentLength;

    /**
     * 已下载文件长度
     */
    public long currentLength;

    /**
     * 文件狀態
     */
    public DownloadState state;

    /**
     * 存储各个线程对应的起始块
     */
    public HashMap<Integer, Long> ranges;

    /**
     * 下载速度
     */
    public int speed;

    /**
     * 指定下载目录
     */
    private String dir;

    /**
     * 指定下载文件名称
     */
    private String name;

    {
        //默认为支持断点下载
        range = true;
    }

    public String getDir() {
        return dir;
    }

    public DownloadEntry setDir(String dir) {
        if (currentLength == 0) {
            this.dir = dir;
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public DownloadEntry setName(String name) {
        if (currentLength == 0) {
            this.name = name;
        }
        return this;
    }

    public static DownloadEntry obtain(String id, String url) {
        return new DownloadEntry(id, url);
    }

    public DownloadEntry() {
        state = DownloadState.IDLE;
    }

    private DownloadEntry(String id, String url) {
        this.id = id;
        this.url = url;
        state = DownloadState.IDLE;
    }

    public DownloadEntry setRange(boolean range) {
        this.range = range;
        return this;
    }

    /**
     * 是否支持断点下载
     *
     * @return
     */
    public boolean isRange() {
        return range;
    }

    public void reset() {
        state = DownloadState.IDLE;
        speed = 0;
        currentLength = 0;
        ranges = null;
    }


    @Override
    public String toString() {
        return id + " is " + state.name() + " " + currentLength + "/" + contentLength;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o.hashCode() == this.hashCode();
    }

    public boolean isDone() {
        return state == DownloadState.DONE;
    }

    public final DownloadFile file = new DownloadFile() {
        public boolean isConnecting() {
            return state == DownloadState.CONNECT;
        }

        public boolean isPaused() {
            return state == DownloadState.PAUSED;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getPath() {
            return getDir() + "/" + getName();
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public long getCurrentLength() {
            return currentLength;
        }

        @Override
        public long getContentLength() {
            return contentLength;
        }

        @Override
        public long getSpeed() {
            return speed;
        }

        @Override
        public boolean isDone() {
            return state == DownloadState.DONE;
        }

        @Override
        public boolean isDownloading() {
            return state == DownloadState.ING;
        }

        @Override
        public boolean isError() {
            return state == DownloadState.ERROR;
        }

        @Override
        public boolean isIdle() {
            return state == DownloadState.IDLE;
        }

        @Override
        public boolean isWait() {
            return state == DownloadState.WAIT;
        }
    };
}