package com.qw.download.entities;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by qinwei on 2016/4/14 17:16
 * email:qinwei_it@163.com
 */
public class DownloadEntry implements Serializable {
    public String id;
    public String url;
    public boolean isSupportRange;
    public long contentLength;
    public long currentLength;
    public State state;
    public HashMap<Integer, Long> ranges;

    public DownloadEntry() {
        state = State.idle;
    }

    public DownloadEntry(String id, String url) {
        this.id = id;
        this.url = url;
        state = State.idle;
    }

    public void reset() {
        currentLength = 0;
        ranges = null;
    }

    public enum State {
        /**
         * 空闲
         */
        idle,
        /**
         * 连接中
         */
        connect,
        /**
         * 下载中
         */
        ing,
        /**
         * 已暂停
         */
        paused,
        /**
         * 已取消
         */
        cancelled,
        /**
         * 错误
         */
        error,
        /**
         * 完成
         */
        done,
        /**
         * 等待
         */
        wait
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
}
