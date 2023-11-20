package com.qw.download.v2

/**
 * Created by qinwei on 2023/6/2 17:24
 * email: qinwei_it@163.com
 */

class DownloadTask {
    private lateinit var downloadThread: IDownloadThread
    fun start() {
        //1.load file info
        //2、if support range execute multi download  else single download
        downloadThread = SingleDownloadThread()
        downloadThread = MultiDownloadThread()
        downloadThread.start()
        downloadThread.pause()
        downloadThread.resume()
        downloadThread.stop()
        downloadThread.cancel()
    }
}

interface IDownloadThread {
    fun start()
    fun pause()
    fun resume()
    fun stop()
    fun cancel()
}

class SingleDownloadThread : IDownloadThread {
    //下载地址
    //存储路径
    override fun start() {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun stop() {
    }

    override fun cancel() {
    }
}


interface AbsMultiDownloadThread : IDownloadThread

class MultiDownloadThread : AbsMultiDownloadThread {
    override fun start() {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun stop() {
    }

    override fun cancel() {
    }
}

class TestMultiDownloadThread : AbsMultiDownloadThread {

    override fun start() {

    }

    override fun pause() {
    }

    override fun resume() {

    }

    override fun stop() {

    }

    override fun cancel() {

    }
}


