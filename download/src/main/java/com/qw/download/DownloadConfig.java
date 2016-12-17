package com.qw.download;

/**
 * Created by qinwei on 2016/4/21 10:09
 * email:qinwei_it@163.com
 */
public class DownloadConfig {
    private static DownloadConfig mInstance;
    public static final int MAX_DOWNLOAD_TASK_SIZE = 2;
    public static final int MAX_DOWNLOAD_THREAD_SIZE = 2;
    public static final int CONNECT_TIME = 5 * 1000;
    public static final int READ_TIME = 5 * 1000;
    public static final int MAX_RETRY_COUNT = 3;
    public boolean isDevelop = true;
    public static final String DOWNLOAD_DEFAULT_DIR = "/download";
    private  String downloadDirPath;

    public static DownloadConfig getInstance(){
        synchronized (DownloadConfig.class) {
            if(mInstance==null){
                mInstance=new DownloadConfig();
            }
            return mInstance;
        }
    }
    private DownloadConfig(){
        //默认下载文件夹路径
        downloadDirPath=DownloadFileUtil.getDownloadDir(DOWNLOAD_DEFAULT_DIR);
    }

    public DownloadConfig setDebug(boolean flag) {
        isDevelop = flag;
        return this;
    }

    /**
     *
     * @param downloadDirPath 下载文件夹完整路径
     * @return
     */
    public DownloadConfig setDownloadDirPath(String downloadDirPath) {
        this.downloadDirPath =DownloadFileUtil.getDownloadDir(downloadDirPath);
        return this;
    }

    public String getDownloadDirPath() {
        return downloadDirPath;
    }

}
