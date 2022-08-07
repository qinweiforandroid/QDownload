package com.qw.download.db;

public enum DownloadState {
    /**
     * 空闲
     */
    IDLE,
    /**
     * 连接中
     */
    CONNECT,
    /**
     * 下载中
     */
    ING,
    /**
     * 已暂停
     */
    PAUSED,
    /**
     * 已取消
     */
    CANCELLED,
    /**
     * 错误
     */
    ERROR,
    /**
     * 完成
     */
    DONE,
    /**
     * 等待
     */
    WAIT
}
