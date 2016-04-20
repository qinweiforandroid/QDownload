package com.qw.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qw.download.DLog;
import com.qw.download.DownloadConfig;
import com.qw.download.DownloadDBController;
import com.qw.download.DownloadEntity;
import com.qw.download.DownloadManager;
import com.qw.download.DownloadWatcher;

import java.io.File;

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
            switch (e.state){
                case cancelled:
                    break;
                case paused:
                    break;
                case resume:
                    break;
            }
            mDownloadInfoLabel.setText(e.toString());
            Log.e("MainActivity", e.toString());
        }
    };

    DownloadEntity entity = null;
    private TextView mDownloadTitleLabel;
    private Button mDownloadClearBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadManager.getInstance(this);
        assignViews();
        initData();
    }



    private void assignViews() {
        mDownloadInfoLabel = (TextView) findViewById(R.id.mDownloadInfoLabel);
        mDownloadTitleLabel = (TextView) findViewById(R.id.mDownloadTitleLabel);
        mDownloadAddBtn = (Button) findViewById(R.id.mDownloadAddBtn);
        mDownloadStopBtn = (Button) findViewById(R.id.mDownloadStopBtn);
        mDownloadResumeBtn = (Button) findViewById(R.id.mDownloadResumeBtn);
        mDownloadCancelBtn = (Button) findViewById(R.id.mDownloadCancelBtn);
        mDownloadClearBtn = (Button) findViewById(R.id.mDownloadClearBtn);
        mDownloadAddBtn.setOnClickListener(this);
        mDownloadStopBtn.setOnClickListener(this);
        mDownloadResumeBtn.setOnClickListener(this);
        mDownloadCancelBtn.setOnClickListener(this);
        mDownloadClearBtn.setOnClickListener(this);
        mDownloadTitleLabel.setText("下载文件夹路径:" + DownloadConfig.getDownloadDir(DownloadConfig.download_dir));
        reset();
    }
    private void initData() {
       DownloadEntity e= DownloadDBController.getInstance(getApplicationContext()).findById("weixin680.apk");
        if(e!=null){
            entity=e;
            mDownloadInfoLabel.setText(entity.toString());
        }
    }
    public void reset(){
        mDownloadAddBtn.setEnabled(true);
        mDownloadStopBtn.setEnabled(false);
        mDownloadResumeBtn.setEnabled(false);
        mDownloadCancelBtn.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        if (entity == null) {
            entity = new DownloadEntity();
            entity.id = "weixin680.apk";
            entity.url = "http://gdown.baidu.com/data/wisegame/a2216288661d09b4/weixin_680.apk";
        }
        switch (v.getId()) {
            case R.id.mDownloadAddBtn:
                DownloadManager.getInstance(this).addDownload(entity);
                mDownloadAddBtn.setEnabled(false);
                mDownloadStopBtn.setEnabled(true);
                mDownloadCancelBtn.setEnabled(true);
                break;
            case R.id.mDownloadStopBtn:
                DownloadManager.getInstance(this).pauseDownload(entity);
                mDownloadStopBtn.setEnabled(false);
                mDownloadCancelBtn.setEnabled(false);
                mDownloadResumeBtn.setEnabled(true);
                mDownloadClearBtn.setEnabled(true);
                break;
            case R.id.mDownloadResumeBtn:
                DownloadManager.getInstance(this).resumeDownload(entity);
                mDownloadStopBtn.setEnabled(true);
                mDownloadCancelBtn.setEnabled(true);
                mDownloadResumeBtn.setEnabled(false);
                mDownloadClearBtn.setEnabled(false);
                break;
            case R.id.mDownloadCancelBtn:
                mDownloadCancelBtn.setEnabled(false);
                mDownloadStopBtn.setEnabled(false);
                mDownloadResumeBtn.setEnabled(true);
                mDownloadClearBtn.setEnabled(true);
                DownloadManager.getInstance(this).cancelDownload(entity);
                break;
            case R.id.mDownloadClearBtn:
                reset();
                mDownloadInfoLabel.setText("下载过程文件基本信息显示");
                File file=new File(DownloadConfig.getDownloadPath("weixin680.apk"));
                DLog.d("Main",file.getAbsolutePath());
                if(file.exists()){
                    file.delete();
                }
               if(DownloadDBController.getInstance(getApplicationContext()).delete(entity)){
                   entity = new DownloadEntity();
                   entity.id = "weixin680.apk";
                   entity.url = "http://gdown.baidu.com/data/wisegame/a2216288661d09b4/weixin_680.apk";
                   Toast.makeText(MainActivity.this, "clear successfully!", Toast.LENGTH_SHORT).show();
               }
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

    public void goList(View v) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }
}
