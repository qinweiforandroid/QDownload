package com.qw.example.single;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qw.download.DownloadInfo;
import com.qw.download.manager.DownloadManager;
import com.qw.download.manager.DownloadWatcher;
import com.qw.download.manager.FileDownload;
import com.qw.example.R;
import com.qw.example.base.BaseActivity;

/**
 * Created by qinwei on 2016/4/22 10:50
 * email:qinwei_it@163.com
 */
public class SingleDownloadActivity extends BaseActivity {
    private TextView mSingleDownloadInfoLabel;
    private Button mSingleDownloadAddBtn;
    private Button mSingleDownloadStopBtn;
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onChanged(DownloadInfo e) {
            if (TextUtils.equals(id, e.getId())) {
                mSingleDownloadInfoLabel.setText(e.getId() + "\n" + e.getCurrentLength() + "/"
                        + e.getContentLength());
            }
        }
    };

    private String id = "weixin680_for_single_download.apk";

    @Override
    protected void setContentView() {
        setContentView(R.layout.single_download_activity);
    }

    @Override
    protected void initializeView() {
        mSingleDownloadInfoLabel = findViewById(R.id.mSingleDownloadInfoLabel);
        mSingleDownloadAddBtn = findViewById(R.id.mSingleDownloadAddBtn);
        mSingleDownloadStopBtn = findViewById(R.id.mSingleDownloadPauseBtn);
        mSingleDownloadAddBtn.setOnClickListener(view -> {
            mSingleDownloadAddBtn.setEnabled(false);
            mSingleDownloadStopBtn.setEnabled(true);
            //创建request 生成一个唯一id
            new FileDownload.Builder(id)
                    //不启用断点下载
                    .setRange(false)
                    //设置下载的文件名称
                    .setName("weixin_680.apk")
                    //设置下载链接
                    .setUrl("http://gdown.baidu.com/weixin_680.apk")
                    .setDir(getExternalCacheDir().getAbsolutePath())//设置下载的文件路径
                    .add();
        });
        mSingleDownloadStopBtn.setOnClickListener(view -> {
            mSingleDownloadAddBtn.setEnabled(true);
            mSingleDownloadStopBtn.setEnabled(false);
            FileDownload.pause(id);
        });
        mSingleDownloadStopBtn.setEnabled(false);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        setTitle("单线程下载");
    }


    @Override
    protected void onResume() {
        super.onResume();
        DownloadManager.addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadManager.removeObserver(watcher);
    }
}