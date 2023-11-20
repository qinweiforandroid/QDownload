package com.qw.download.core;

import java.net.HttpURLConnection;

/**
 * HttpURLConnection监听器
 */
public interface HttpURLConnectionListener {
    /**
     * 获取数据之前HttpURLConnection的配置
     *
     * @param connection
     */
    void config(HttpURLConnection connection);
}