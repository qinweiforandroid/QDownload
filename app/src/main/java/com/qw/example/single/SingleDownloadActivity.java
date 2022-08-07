package com.qw.example.single;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qw.download.entities.DownloadFile;
import com.qw.download.manager.DownloadManager;
import com.qw.download.manager.DownloadWatcher;
import com.qw.download.manager.FileRequest;
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
        protected void onChanged(DownloadFile e) {
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
        mSingleDownloadInfoLabel = (TextView) findViewById(R.id.mSingleDownloadInfoLabel);
        mSingleDownloadAddBtn = (Button) findViewById(R.id.mSingleDownloadAddBtn);
        mSingleDownloadStopBtn = (Button) findViewById(R.id.mSingleDownloadPauseBtn);
        mSingleDownloadAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSingleDownloadAddBtn.setEnabled(false);
                mSingleDownloadStopBtn.setEnabled(true);
                FileRequest.create(id)//创建request 生成一个唯一id
                        .setRange(false)//不适用断点下载
                        .setName("weixin_680.apk")//设置下载的文件名称
                        .setUrl("http://gdown.baidu.com/weixin_680.apk")//设置下载链接
                        .setDir(getExternalCacheDir().getAbsolutePath())//设置下载的文件路径
                        .addDownload();//执行下载
            }
        });
        mSingleDownloadStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSingleDownloadAddBtn.setEnabled(true);
                mSingleDownloadStopBtn.setEnabled(false);
                FileRequest.create(id).pauseDownload();
            }
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