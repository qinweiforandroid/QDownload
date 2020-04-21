package com.qw.example;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.qw.download.DownloadConfig;
import com.qw.download.DownloadManager;
import com.qw.example.core.BaseActivity;
import com.qw.example.utils.PermissionHelper;

/**
 * Created by qinwei on 2016/4/22 10:51
 * email:qinwei_it@163.com
 */
public class HomeActivity extends BaseActivity {
    private TextView mHomeDownloadDirPathLabel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionHelper.requestPermission(this, PermissionHelper.Permission.STORAGE, PermissionHelper.ASK_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_home, false);
    }

    @Override
    protected void initializeView() {
        mHomeDownloadDirPathLabel = (TextView) findViewById(R.id.mHomeDownloadDirPathLabel);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        setTitle("QDownload示例");
        //初始化下载服务
        DownloadManager.getInstance(this);
        mHomeDownloadDirPathLabel.setText("下载文件夹路径:" + DownloadConfig.getInstance().getDownloadDir().getAbsolutePath());
    }

    public void goSingleDownload(View v) {
        Intent intent = new Intent(this, SingleDownloadActivity.class);
        startActivity(intent);
    }


    public void goMultithreadList(View v) {
        Intent intent = new Intent(this, MultithreadDownloadListActivity.class);
        startActivity(intent);
    }
}
