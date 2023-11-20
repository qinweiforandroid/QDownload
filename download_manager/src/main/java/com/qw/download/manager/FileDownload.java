package com.qw.download.manager;

import android.text.TextUtils;

import com.qw.download.DownloadInfo;

/**
 * 下载Api 对DownloadManager调用做了一层wrapper
 */
public class FileDownload {

    private String id;

    private String url;


    private String dir;


    private String name;


    private boolean range = true;

    private FileDownload() {
    }

    public static class Builder {
        public Builder(String id) {
            this.id = id;
        }

        private String id;
        /**
         * 文件路径
         */
        private String url;
        /**
         * 指定下载目录
         */
        private String dir;
        /**
         * 指定下载文件名称
         */
        private String name;
        /**
         * 是否支持断点下载
         */
        private boolean range = true;


        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setDir(String dir) {
            this.dir = dir;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setRange(boolean range) {
            this.range = range;
            return this;
        }

        private FileDownload builder() {
            FileDownload fileDownload = new FileDownload();
            fileDownload.id = id;
            fileDownload.dir = dir;
            fileDownload.name = name;
            fileDownload.range = range;
            fileDownload.url = url;
            return fileDownload;
        }

        public void add() {
            //check add param
            if (TextUtils.isEmpty(id)) {
                throw new IllegalArgumentException("id not be empty");
            }
            //通过builder模式新增一个下载任务
            builder().add();
        }
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getDir() {
        return dir;
    }

    public String getName() {
        return name;
    }

    public boolean isRange() {
        return range;
    }

    public void add() {
        DownloadManager.add(this);
    }

    public static void pause(String id) {
        DownloadManager.pause(id);
    }

    public static void resume(String id) {
        DownloadManager.resume(id);
    }

    public static void recoverAll() {
        DownloadManager.recoverAll();
    }

    public static void pauseAll() {
        DownloadManager.pauseAll();
    }

    public static void cancel(String id) {
        DownloadManager.cancel(id);
    }

    public static DownloadInfo getInfo(String id) {
        return DownloadManager.getFileInfo(id);
    }
}