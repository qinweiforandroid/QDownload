package com.qw.example;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import com.qw.download.DownloadFileUtil;
import com.qw.download.DownloadManager;
import com.qw.download.DownloadWatcher;

import java.io.File;

public class MultithreadDownloadActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mDownloadInfoLabel;
    private Button mDownloadAddBtn;
    private Button mDownloadStopBtn;
    private Button mDownloadResumeBtn;
    private Button mDownloadCancelBtn;
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onDataChanged(DownloadEntity e) {
            entity = e;
            switch (e.state) {
                case cancelled:
                    break;
                case paused:
                    if (mNotificationManager != null) {
                        mBuilder.setContentText("暂停 " + e.currentLength + "/" + e.contentLength);
                        mBuilder.setProgress(100, (int) (e.currentLength * 100.0 / e.contentLength), false);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                    break;
                case resume:
                    break;
                case ing:
                    if (mNotificationManager != null) {
                        mBuilder.setContentText(e.currentLength + "/" + e.contentLength);
                        mBuilder.setProgress(100, (int) (e.currentLength * 100.0 / e.contentLength), false);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                    break;
                case done:
                    if (mNotificationManager != null) {
                        mBuilder.setContentText("下载完成，点击打开 ");
                        mBuilder.setProgress(0, 0, false);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                    break;
                case connect:
                    if (mNotificationManager != null) {
                        mBuilder.setContentText("连接中");
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                    break;
            }
            mDownloadInfoLabel.setText(e.toString());
            Log.e("MultithreadDownloadActivity", e.toString());
        }
    };

    DownloadEntity entity = null;
    private TextView mDownloadTitleLabel;
    private Button mDownloadClearBtn;
    private Button mDownloadNotificationShowBtn;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multithread);
        DownloadManager.getInstance(this);
        initView();
        initData();
    }

    private void initView() {
        mDownloadInfoLabel = (TextView) findViewById(R.id.mDownloadInfoLabel);
        mDownloadTitleLabel = (TextView) findViewById(R.id.mDownloadTitleLabel);
        mDownloadAddBtn = (Button) findViewById(R.id.mDownloadAddBtn);
        mDownloadStopBtn = (Button) findViewById(R.id.mDownloadStopBtn);
        mDownloadResumeBtn = (Button) findViewById(R.id.mDownloadResumeBtn);
        mDownloadCancelBtn = (Button) findViewById(R.id.mDownloadCancelBtn);
        mDownloadClearBtn = (Button) findViewById(R.id.mDownloadClearBtn);
        mDownloadNotificationShowBtn = (Button) findViewById(R.id.mDownloadNotificationShowBtn);

        mDownloadAddBtn.setOnClickListener(this);
        mDownloadStopBtn.setOnClickListener(this);
        mDownloadResumeBtn.setOnClickListener(this);
        mDownloadCancelBtn.setOnClickListener(this);
        mDownloadClearBtn.setOnClickListener(this);
        mDownloadNotificationShowBtn.setOnClickListener(this);
        mDownloadTitleLabel.setText("下载文件夹路径:" + DownloadConfig.getInstance().getDownloadDirPath());
        reset();
    }

    private void initData() {
        setTitle("多线程断点下载");
        DownloadEntity e = DownloadDBController.getInstance(getApplicationContext()).findById("weixin680.apk");
        if (e != null) {
            entity = e;
            mDownloadInfoLabel.setText(entity.toString());
        }
    }

    public void reset() {
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
                File file = new File(DownloadFileUtil.getDownloadPath("weixin680.apk"));
                DLog.d("Main", file.getAbsolutePath());
                if (file.exists()) {
                    file.delete();
                }
                if (DownloadDBController.getInstance(getApplicationContext()).delete(entity)) {
                    entity = new DownloadEntity();
                    entity.id = "weixin680.apk";
                    entity.url = "http://gdown.baidu.com/data/wisegame/a2216288661d09b4/weixin_680.apk";
                    Toast.makeText(MultithreadDownloadActivity.this, "clear successfully!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.mDownloadNotificationShowBtn:
                showDownloadNotification();
                break;
            default:
                break;
        }
    }

    private void showDownloadNotification() {
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle("download notification")
                        .setContentText("download")
                        .setProgress(100, 0, false);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MultithreadDownloadActivity.class);
// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MultithreadDownloadActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
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
        Intent intent = new Intent(this, MultithreadDownloadListActivity.class);
        startActivity(intent);
    }
}
