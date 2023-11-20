package com.qw.download.manager;

import android.content.Context;
import android.os.Environment;

import com.qw.download.core.HttpURLConnectionListener;
import com.qw.download.db.sqlite.impl.DownloadDao;
import com.qw.download.db.IDownloadDao;
import com.qw.download.utilities.DLog;

/**
 * Created by qinwei on 2016/4/21 10:09
 * email:qinwei_it@163.com
 */
public class DownloadConfig {
    private static DownloadConfig mInstance;
    private int max_tasks;
    private int max_threads;
    private int max_retry_count;
    private int connect_timeout;
    private int read_timeout;
    private String downloadDir;
    private boolean autoResume;
    private boolean logEnable;
    private Context context;

    private IDownloadDao dao = new DownloadDao();

    private HttpURLConnectionListener httpURLConnectionListener;

    public synchronized static DownloadConfig getInstance() {
        if (mInstance == null) {
            throw new IllegalArgumentException("must be call init method to config");
        }
        return mInstance;
    }

    public static void init(DownloadConfig config) {
        if (mInstance == null) {
            mInstance = config;
            DLog.setDebug(mInstance.logEnable);
        }
    }

    private DownloadConfig() {
    }

    public int getMaxTask() {
        return max_tasks;
    }

    public int getMaxThread() {
        return max_threads;
    }

    public int getMaxRetryCount() {
        return max_retry_count;
    }

    public int getConnectTimeout() {
        return connect_timeout;
    }

    public int getReadTimeout() {
        return read_timeout;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public boolean isAutoResume() {
        return autoResume;
    }

    public IDownloadDao getDao() {
        return dao;
    }

    public HttpURLConnectionListener getHttpURLConnectionListener() {
        return httpURLConnectionListener;
    }

    public Context getContext() {
        return context;
    }

    public static class Builder {
        private Context context;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
            //默认下载文件存在cache dir 目录
            this.downloadDir = context.getCacheDir().getPath();
        }

        private int max_tasks = 3;
        private int max_threads = 2;
        private int max_retry_count = 3;
        private int connect_timeout = 10 * 1000;
        private int read_timeout = 10 * 1000;
        private boolean autoResume;
        private String downloadDir;
        private boolean logEnable;
        private HttpURLConnectionListener httpURLConnectionListener;

        public Builder setMaxTask(int count) {
            this.max_tasks = count;
            return this;
        }

        public Builder setHttpURLConnectionListener(HttpURLConnectionListener httpURLConnectionListener) {
            this.httpURLConnectionListener = httpURLConnectionListener;
            return this;
        }

        public Builder setMaxThread(int count) {
            this.max_threads = count;
            return this;
        }

        public Builder setRetryCount(int count) {
            this.max_retry_count = count;
            return this;
        }

        public Builder setConnectTimeout(int time) {
            this.connect_timeout = time;
            return this;
        }

        public Builder setReadTimeout(int time) {
            this.read_timeout = time;
            return this;
        }

        public Builder setAutoResume(boolean autoResume) {
            this.autoResume = autoResume;
            return this;
        }

        public Builder setDownloadDir(String downloadDir) {
            this.downloadDir = downloadDir;
            return this;
        }

        public Builder setLogEnable(boolean logEnable) {
            this.logEnable = logEnable;
            return this;
        }

        public DownloadConfig builder() {
            DownloadConfig config = new DownloadConfig();
            config.context = context;
            config.max_tasks = max_tasks;
            config.max_threads = max_threads;
            config.max_retry_count = max_retry_count;
            config.connect_timeout = connect_timeout;
            config.read_timeout = read_timeout;
            config.downloadDir = downloadDir;
            config.autoResume = autoResume;
            config.logEnable = this.logEnable;
            config.httpURLConnectionListener = httpURLConnectionListener;
            return config;
        }
    }
}