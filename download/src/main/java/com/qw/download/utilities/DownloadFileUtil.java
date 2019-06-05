package com.qw.download.utilities;

import android.os.Environment;

import com.qw.download.DownloadConfig;

import java.io.File;

/**
 * 下载框架数据配置
 * Created by qinwei on 2016/4/14 16:08
 * email:qinwei_it@163.com
 */
public class DownloadFileUtil {

    public static boolean checkSDCard() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getDownloadPath(String id) {
        return DownloadConfig.getInstance().getDownloadDir() + File.separator + id;
    }
}
