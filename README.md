# what QDownload
a  MultithreadDownload libs，support simple api for dev 
# how to use

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
# power by stay
该lib参考Stay的[自己动手写多任务下载](http://www.stay4it.com/course/6)课程中，如果想知道它是如何一步步封装出来的，不妨跟着课程系统学习一下，以后有需求扩展也知道怎么改了。
