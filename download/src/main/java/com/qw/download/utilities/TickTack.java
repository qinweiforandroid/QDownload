package com.qw.download.utilities;

/**
 * Created by qinwei on 2019-06-05 13:11
 * email: qinwei_it@163.com
 */
public class TickTack {
    private long mLastStamp;
    private long interval;
    public TickTack(long interval) {
        this.interval=interval;
    }

    public synchronized boolean needToNotify() {
        long stamp = System.currentTimeMillis();
        if (stamp - mLastStamp > interval) {
            mLastStamp = stamp;
            return true;
        }
        return false;
    }
}
