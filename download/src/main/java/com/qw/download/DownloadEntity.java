package com.qw.download;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by qinwei on 2016/4/14 17:16
 * email:qinwei_it@163.com
 */
public class DownloadEntity implements Serializable {
    public String id;
    public String url;
    public boolean isSupportRange;
    public long contentLength;
    public long currentLength;
    public State state;
    public HashMap<Integer, Long> ranges;

    public DownloadEntity(){}
    public DownloadEntity(String id, String url) {
        this.id = id;
        this.url = url;
        state = State.idle;
    }

    public void reset() {
        currentLength = 0;
        ranges = null;
    }

    public enum State {
        idle, connect, ing, resume, paused, cancelled, error, done, wait
    }

    @Override
    public String toString() {
        return id + " is " + state.name() + " " + currentLength + "/" + contentLength;
    }

}