package com.qw.example;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.TextView;

import com.qw.download.DownloadConfig;
import com.qw.example.core.BaseActivity;
import com.qw.permission.OnRequestPermissionsResultListener;
import com.qw.permission.Permission;
import com.qw.permission.PermissionResult;

import org.jetbrains.annotations.NotNull;

/**
 * Created by qinwei on 2016/4/22 10:51
 * email:qinwei_it@163.com
 */
public class HomeActivity extends BaseActivity {
    private TextView mHomeDownloadDirPathLabel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Permission.Companion.init(this)
                .permissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .setOnRequestPermissionsResultListener(new OnRequestPermissionsResultListener() {
                    @Override
                    public void onRequestPermissionsResult(@NotNull PermissionResult permissionResult) {
                        
                    }
                })
                .request();
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
        mHomeDownloadDirPathLabel.setText("下载文件夹路径:" + DownloadConfig.getInstance().getDownloadDir());
    }

    public void goSingleDownload(View v) {
        Intent intent = new Intent(this, SingleDownloadActivity.class);
        startActivity(intent);
    }


    public void goMultithreadDownloadListActivity(View v) {
        Intent intent = new Intent(this, MultithreadDownloadListActivity.class);
        startActivity(intent);
    }
}
