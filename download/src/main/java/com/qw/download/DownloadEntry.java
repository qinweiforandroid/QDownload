package com.qw.download;


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
    public int speed;

    public static DownloadEntry obtain(String id, String url) {
        return new DownloadEntry(id, url);
    }

    private DownloadEntry(String id, String url) {
        this.id = id;
        this.url = url;
        state = State.IDLE;
    }

    public void reset() {
        state = State.IDLE;
        speed = 0;
        currentLength = 0;
        ranges = null;
    }

    public enum State {
        /**
         * 空闲
         */
        IDLE,
        /**
         * 连接中
         */
        CONNECT,
        /**
         * 下载中
         */
        ING,
        /**
         * 已暂停
         */
        PAUSED,
        /**
         * 已取消
         */
        CANCELLED,
        /**
         * 错误
         */
        ERROR,
        /**
         * 完成
         */
        DONE,
        /**
         * 等待
         */
        WAIT
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
        return state == State.DONE;
    }

    public boolean isConnecting() {
        return state == State.CONNECT;
    }
}
