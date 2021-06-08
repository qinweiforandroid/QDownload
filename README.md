# QDownload
一个实现了多任务、多线程、断点的下载工具 
# how to use
## API

```java
public void api(){
    //添加一个下载任务
    DownloadManager.addDownload(entity);
    //暂停一个下载任务
    DownloadManager.pauseDownload(entity);
    //恢复一个下载任务
    DownloadManager.resumeDownload(entity);
    //取消一个下载任务
    DownloadManager.cancelDownload(entity);
}
```

在你需要观测的地方加上

```java
private DownloadWatcher watcher = new DownloadWatcher() {
    @Override
    protected void onDataChanged(DownloadEntity e) {
        entity = e;
	    //这里监听下载的实时信息
        mDownloadInfoLabel.setText(e.toString());
        Log.e("MainActivity", e.toString());
    }
};

@Override
protected void onResume() {
    super.onResume();
    //注册观察者
    DownloadManager.addObserver(watcher);
}

@Override
protected void onPause() {
    super.onPause();
    //删除观察者
    DownloadManager.removeObserver(watcher);
}
```
