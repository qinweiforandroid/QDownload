package com.qw.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qw.download.DownloadEntity;
import com.qw.download.DownloadManager;
import com.qw.download.DownloadWatcher;
import com.qw.example.core.BaseActivity;

/**
 * Created by qinwei on 2016/4/22 10:50
 * email:qinwei_it@163.com
 */
public class SingleDownloadActivity extends BaseActivity implements View.OnClickListener {
    private DownloadEntity entity;
    private TextView mSingleDownloadInfoLabel;
    private Button mSingleDownloadAddBtn;
    private Button mSingleDownloadStopBtn;
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onDataChanged(DownloadEntity e) {
            if (entity != null) {
                if (!e.equals(entity)) return;
                mSingleDownloadInfoLabel.setText(e.toString());
                Log.e("Multithread", e.toString());
            }
        }
    };

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_single_download);
    }

    @Override
    protected void initializeView() {
        mSingleDownloadInfoLabel = (TextView) findViewById(R.id.mSingleDownloadInfoLabel);
        mSingleDownloadAddBtn = (Button) findViewById(R.id.mSingleDownloadAddBtn);
        mSingleDownloadStopBtn = (Button) findViewById(R.id.mSingleDownloadPauseBtn);
        mSingleDownloadAddBtn.setOnClickListener(this);
        mSingleDownloadStopBtn.setOnClickListener(this);
        mSingleDownloadStopBtn.setEnabled(false);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        setTitle("单线程下载");
    }

    @Override
    public void onClick(View v) {
        if (entity == null) {
            entity = new DownloadEntity();
            entity.id = "weixin680_for_single_download.apk";
            entity.url = "http://gdown.baidu.com/data/wisegame/a2216288661d09b4/weixin_680.apk";
            entity.isSupportRange = false;
            entity.contentLength = 33453820;
        }
        switch (v.getId()) {
            case R.id.mSingleDownloadAddBtn:
                mSingleDownloadAddBtn.setEnabled(false);
                mSingleDownloadStopBtn.setEnabled(true);
                DownloadManager.getInstance(getApplicationContext()).addDownload(entity);
                break;
            case R.id.mSingleDownloadPauseBtn:
                mSingleDownloadAddBtn.setEnabled(true);
                mSingleDownloadStopBtn.setEnabled(false);
                DownloadManager.getInstance(getApplicationContext()).pauseDownload(entity);
                break;
            default:
                break;
        }
    }

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
}
