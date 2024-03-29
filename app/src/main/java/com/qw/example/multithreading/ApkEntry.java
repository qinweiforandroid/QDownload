package com.qw.example.multithreading;

import android.text.TextUtils;

import com.qw.download.utilities.FileUtil;

/**
 * create by qinwei at 2022/1/27 21:13
 */
public class ApkEntry {
    private String id;
    public String cover;
    public String name;
    public String url;

    public String id() {
        if (TextUtils.isEmpty(id)) {
            id = FileUtil.getMd5FileName(url);
        }
        return id;
    }
}
