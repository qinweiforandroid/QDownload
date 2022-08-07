package com.qw.download.entities;

import java.io.Serializable;

public interface DownloadFile extends Serializable {
    String getId();

    String getPath();

    String getUrl();

    long getCurrentLength();

    long getContentLength();

    long getSpeed();

    boolean isDone();

    boolean isConnecting();

    boolean isDownloading();

    boolean isPaused();

    boolean isWait();

    boolean isError();

    boolean isIdle();
}