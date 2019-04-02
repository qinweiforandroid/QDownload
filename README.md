# what QDownload
a  MultithreadDownload libs，support simple api for dev 
# how to use
# Gradle

    compile 'com.qw.download:download:1.0'

# API

    DownloadManager.getInstance(this).addDownload(entity);//添加一个下载任务
    DownloadManager.getInstance(this).pauseDownload(entity);//暂停一个下载任务
    DownloadManager.getInstance(this).resumeDownload(entity);//恢复一个下载任务
    DownloadManager.getInstance(this).cancelDownload(entity);//取消一个下载任务

在你需要观测的地方加上

    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onDataChanged(DownloadEntity e) {
            entity = e;
            mDownloadInfoLabel.setText(e.toString());
            Log.e("MainActivity", e.toString());
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        DownloadManager.getInstance(this).addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadManager.getInstance(this).removeObserver(watcher);
    }
