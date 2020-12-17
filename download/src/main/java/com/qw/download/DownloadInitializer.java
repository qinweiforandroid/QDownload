package com.qw.download;

import android.content.Context;

import androidx.startup.Initializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qinwei on 12/17/20 2:14 PM
 * email: qinwei_it@163.com
 */
public class DownloadInitializer implements Initializer<DownloadManager> {


    @Override
    public DownloadManager create(Context context) {
        DownloadManager.init(context);
        return DownloadManager.getInstance();
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }

}
