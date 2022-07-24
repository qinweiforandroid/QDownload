package com.qw.download;


import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by qinwei on 2016/4/14 17:16
 * email:qinwei_it@163.com
 */
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
    public State state;

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

    public DownloadEntry(){
        state = State.IDLE;
    }
    private DownloadEntry(String id, String url) {
        this.id = id;
        this.url = url;
        state = State.IDLE;
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
        state = State.IDLE;
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
        return state == State.DONE;
    }

    public boolean isConnecting() {
        return state == State.CONNECT;
    }

    public boolean isPaused() {
        return state == State.PAUSED;
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
}
