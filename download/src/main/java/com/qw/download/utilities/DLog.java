package com.qw.download.utilities;

import android.util.Log;

import com.qw.download.DownloadConfig;

/**
 * Created by qinwei on 2016/4/14 17:40
 * email:qinwei_it@163.com
 */
public class DLog {
    public static final String TAG = "QDownload";

    public static void d(String msg) {
        Log.e(TAG, "--> " + msg);
    }
}
