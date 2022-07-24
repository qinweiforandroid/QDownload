package com.qw.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qw.download.DownloadEntry;
import com.qw.download.DownloadManager;
import com.qw.download.DownloadWatcher;
import com.qw.example.core.BaseActivity;

/**
 * Created by qinwei on 2016/4/22 10:50
 * email:qinwei_it@163.com
 */
public class SingleDownloadActivity extends BaseActivity implements View.OnClickListener {
    private DownloadEntry entity;
    private TextView mSingleDownloadInfoLabel;
    private Button mSingleDownloadAddBtn;
    private Button mSingleDownloadStopBtn;
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onChanged(DownloadEntry e) {
            if (entity != null) {
                if (!e.equals(entity)) return;
                mSingleDownloadInfoLabel.setText(e.toString());
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
            entity = DownloadEntry.obtain("weixin680_for_single_download.apk",
                            "http://gdown.baidu.com/data/wisegame/a2216288661d09b4/weixin_680.apk")
                    .setRange(false)
                    .setDir(getExternalCacheDir().getAbsolutePath())
                    .setName("weixin_680.apk");
        }
        switch (v.getId()) {
            case R.id.mSingleDownloadAddBtn:
                mSingleDownloadAddBtn.setEnabled(false);
                mSingleDownloadStopBtn.setEnabled(true);
                DownloadManager.add(entity);
                break;
            case R.id.mSingleDownloadPauseBtn:
                mSingleDownloadAddBtn.setEnabled(true);
                mSingleDownloadStopBtn.setEnabled(false);
                DownloadManager.pause(entity);
                break;
            default:
                break;
        }
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
