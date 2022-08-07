package com.qw.download.utilities;

import android.util.Log;


/**
 * Created by qinwei on 2016/4/14 17:40
 * email:qinwei_it@163.com
 */
public class DLog {
    public static final String TAG = "QDownload";
    public static boolean debug = false;

    public static void d(String msg) {
        if (debug) {
            Log.e(TAG, "" + msg);
        }
    }
}