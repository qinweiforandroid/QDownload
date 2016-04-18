package com.qw.download;

import android.os.Environment;

import java.io.File;

/**
 * 下载框架数据配置
 * Created by qinwei on 2016/4/14 16:08
 * email:qinwei_it@163.com
 */
public class DownloadConfig {
    public static final int MAX_DOWNLOAD_TASK_SIZE = 2;
    public static final int MAX_DOWNLOAD_THREAD_SIZE = 2;
    public static final int CONNECT_TIME = 15 * 1000;
    public static final int READ_TIME = 15 * 1000;
    public static final int MAX_RETRY_COUNT=3;
    public static String download_dir = "/download/";

    public static String getRoot() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getDownloadDir(String dirName) {
        File file = new File(getRoot() + dirName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static String getDownloadPath(String name) {
        File file = new File(getDownloadDir(download_dir), name);
        return file.getAbsolutePath();
    }

    public static void setDownloadDir(String dir) {
        download_dir = dir;
    }

    public static boolean sdCardExists() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }
}
