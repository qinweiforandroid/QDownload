package com.qw.download;

import java.io.Serializable;

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

    public enum State {
        idle, connect, ing, resume, stopped, cancelled, error, done, wait
    }

}
