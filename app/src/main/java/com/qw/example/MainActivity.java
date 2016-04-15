package com.qw.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.qw.download.DownloadEntity;
import com.qw.download.DownloadManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.mDownloadBtn).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        DownloadEntity entity=new DownloadEntity();
        entity.id="qq.apk"+System.currentTimeMillis();
        entity.url="http://www.baidu.com";
        DownloadManager.getInstance(this).addDownload(entity);
    }
}
