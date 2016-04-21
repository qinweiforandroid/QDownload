package com.qw.download;

import android.os.Environment;

import java.io.File;

/**
 * 下载框架数据配置
 * Created by qinwei on 2016/4/14 16:08
 * email:qinwei_it@163.com
 */
public class DownloadFileUtil {
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


    public static String getDownloadPath(String fileName) {
        File file = new File(DownloadConfig.getInstance().getDownloadDirPath(), fileName);
        return file.getAbsolutePath();
    }

    public static boolean sdCardExists() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }
}
