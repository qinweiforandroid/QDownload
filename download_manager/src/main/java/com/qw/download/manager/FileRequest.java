package com.qw.download.manager;

import com.qw.download.entities.DownloadFile;

public class FileRequest {
    private FileRequest() {
    }

    public static FileRequest create(String id) {
        FileRequest request = new FileRequest();
        request.id = id;
        return request;
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



    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public FileRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDir() {
        return dir;
    }

    public FileRequest setDir(String dir) {
        this.dir = dir;
        return this;
    }

    public String getName() {
        return name;
    }

    public FileRequest setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isRange() {
        return range;
    }

    public FileRequest setRange(boolean range) {
        this.range = range;
        return this;
    }

    public void addDownload(){
        DownloadManager.add(this);
    }
    public void pauseDownload(){
        DownloadManager.pause(id);
    }
    public void resumeDownload(){
        DownloadManager.resume(id);
    }

    public void cancelDownload(){
        DownloadManager.cancel(id);
    }
    public static DownloadFile getFile(String id) {
        return DownloadManager.getFile(id);
    }
}