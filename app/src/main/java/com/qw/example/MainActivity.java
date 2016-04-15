package com.qw.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qw.download.DownloadEntity;
import com.qw.download.DownloadManager;
import com.qw.download.DownloadWatcher;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mDownloadInfoLabel;
    private Button mDownloadAddBtn;
    private Button mDownloadStopBtn;
    private Button mDownloadResumeBtn;
    private Button mDownloadCancelBtn;
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onDataChanged(DownloadEntity e) {
            entity = e;
            mDownloadInfoLabel.setText(e.toString());
            Log.e("MainActivity", e.toString());
        }
    };

    DownloadEntity entity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
    }

    private void assignViews() {
        mDownloadInfoLabel = (TextView) findViewById(R.id.mDownloadInfoLabel);
        mDownloadAddBtn = (Button) findViewById(R.id.mDownloadAddBtn);
        mDownloadStopBtn = (Button) findViewById(R.id.mDownloadStopBtn);
        mDownloadResumeBtn = (Button) findViewById(R.id.mDownloadResumeBtn);
        mDownloadCancelBtn = (Button) findViewById(R.id.mDownloadCancelBtn);
        mDownloadAddBtn.setOnClickListener(this);
        mDownloadStopBtn.setOnClickListener(this);
        mDownloadResumeBtn.setOnClickListener(this);
        mDownloadCancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (entity == null) {
            entity = new DownloadEntity();
            entity.id = "qq.apk" + System.currentTimeMillis();
            entity.url = "http://www.baidu.com";
        }
        switch (v.getId()) {
            case R.id.mDownloadAddBtn:
                DownloadManager.getInstance(this).addDownload(entity);
                break;
            case R.id.mDownloadStopBtn:
                DownloadManager.getInstance(this).pauseDownload(entity);
                break;
            case R.id.mDownloadResumeBtn:
                DownloadManager.getInstance(this).resumeDownload(entity);
                break;
            case R.id.mDownloadCancelBtn:
                DownloadManager.getInstance(this).cancelDownload(entity);
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
