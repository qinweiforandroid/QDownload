package com.qw.download.utilities;

/**
 * Created by qinwei on 2019-06-05 13:11
 * email: qinwei_it@163.com
 */
public class TickTack {
    private static TickTack mInstance;
    private long mLastStamp;

    private TickTack() {

    }

    public synchronized static TickTack getInstance() {
        if (mInstance == null) {
            mInstance = new TickTack();
        }
        return mInstance;
    }

    public synchronized boolean needToNotify() {
        long stamp = System.currentTimeMillis();
        if (stamp - mLastStamp > 1000) {
            mLastStamp = stamp;
            return true;
        }
        return false;
    }
}
