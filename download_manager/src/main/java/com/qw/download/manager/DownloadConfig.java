package com.qw.download.manager;

import android.os.Environment;

import com.qw.download.db.DownloadDao;
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
    private boolean debug;
    private IDownloadDao dao = new DownloadDao();

    public synchronized static DownloadConfig getInstance() {
        if (mInstance == null) {
            init(new Builder().builder());
        }
        return mInstance;
    }

    public static void init(DownloadConfig config) {
        if (mInstance == null) {
            mInstance = config;
            DLog.setDebug(mInstance.debug);
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

    public static class Builder {
        private int max_tasks = 3;
        private int max_threads = 2;
        private int max_retry_count = 3;
        private int connect_timeout = 10 * 1000;
        private int read_timeout = 10 * 1000;
        private boolean autoResume;
        private String downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        private boolean debug;

        public Builder setMaxTask(int count) {
            this.max_tasks = count;
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

        public Builder setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public DownloadConfig builder() {
            DownloadConfig config = new DownloadConfig();
            config.max_tasks = max_tasks;
            config.max_threads = max_threads;
            config.max_retry_count = max_retry_count;
            config.connect_timeout = connect_timeout;
            config.read_timeout = read_timeout;
            config.downloadDir = downloadDir;
            config.autoResume = autoResume;
            config.debug = this.debug;
            return config;
        }
    }
}