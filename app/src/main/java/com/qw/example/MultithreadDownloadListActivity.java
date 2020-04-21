package com.qw.example;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.qw.download.entities.DownloadEntity;
import com.qw.download.utilities.DownloadFileUtil;
import com.qw.download.DownloadManager;
import com.qw.download.notify.DownloadWatcher;
import com.qw.example.core.BaseActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by qinwei on 2016/4/15 15:37
 * email:qinwei_it@163.com
 */
public class MultithreadDownloadListActivity extends BaseActivity {
    private ListView mDownloadLv;
    private ArrayList<DownloadEntity> datas = new ArrayList<>();
    private DownloadAdapter adapter;
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onDataChanged(DownloadEntity e) {
            if (e.state == DownloadEntity.State.cancelled) {
                datas.remove(e);
            }
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_multithread_list);
    }

    @Override
    protected void initializeView() {
        mDownloadLv = (ListView) findViewById(R.id.mDownloadLv);
        adapter = new DownloadAdapter();
        mDownloadLv.setAdapter(adapter);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        setTitle("多任务多线程断点下载");
        datas.add(new DownloadEntity("weixin_1420.apk", "http://gdown.baidu.com/data/wisegame/00984e94e708c913/weixin_1420.apk"));
        datas.add(new DownloadEntity("mobileqq_android.apk", "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk"));
        datas.add(new DownloadEntity("douyin.apk", "http://ws.yingyonghui.com/fde654f91a2f49741933840c4d0c367c/5cf720b4/apk/6447153/a1e5c3b63c3f9cf28b27baefaa0b315b"));
        datas.add(new DownloadEntity("虾米.apk", "http://download.taobaocdn.com/wireless/xiami-android-spark/latest/xiami-android-spark_701287.apk"));
        adapter.notifyDataSetChanged();
    }


    class DownloadAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                holder = new Holder();
                convertView = LayoutInflater.from(MultithreadDownloadListActivity.this).inflate(R.layout.layout_multithread_list_item, null);
                holder.initializeView(convertView);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.initializeData(position);
            return convertView;
        }
    }

    class Holder implements View.OnClickListener {
        private TextView mDownloadItemInfoLabel;
        private Button mDownloadItemOperationBtn;
        private DownloadEntity e;

        public void initializeView(View v) {
            mDownloadItemInfoLabel = (TextView) v.findViewById(R.id.mDownloadItemInfoLabel);
            mDownloadItemOperationBtn = (Button) v.findViewById(R.id.mDownloadItemOperationBtn);
            mDownloadItemOperationBtn.setOnClickListener(this);
        }

        public void initializeData(int position) {
            e = datas.get(position);
            DownloadEntity cache = DownloadManager.getInstance(getApplicationContext()).findById(e.id);
            if (cache != null) {
                e = cache;
            }
            mDownloadItemInfoLabel.setText(e.toString());
            switch (e.state) {
                case paused:
                    mDownloadItemOperationBtn.setText("继续");
                    break;
                case wait:
                    mDownloadItemOperationBtn.setText("等待");
                    break;
                case ing:
                case connect:
                    mDownloadItemOperationBtn.setText("暂停");
                    break;
                case error:
                    mDownloadItemOperationBtn.setText("重试");
                    break;
                case cancelled:
                case idle:
                    mDownloadItemOperationBtn.setText("下载");
                    break;
                case done:
                    mDownloadItemOperationBtn.setText("打开");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            switch (e.state) {
                case paused:
                case error:
                    DownloadManager.getInstance(getApplicationContext()).resumeDownload(e);
                    break;
                case ing:
                case wait:
                case connect:
                    mDownloadItemOperationBtn.setText("暂停");
                    DownloadManager.getInstance(getApplicationContext()).pauseDownload(e);
                    break;
                case cancelled:
                case idle:
                    DownloadManager.getInstance(getApplicationContext()).addDownload(e);
                    break;
                case done:
                    String path = DownloadFileUtil.getDownloadPath(e.id);
                    File apkfile = new File(path);
                    if (!apkfile.exists()) {
                        return;
                    }
                    // 通过Intent安装APK文件
                    loadInstallApk(MultithreadDownloadListActivity.this, apkfile);
                    break;
                default:
                    break;
            }
        }

    }

    public static void loadInstallApk(Context context, File file) {
        if (!file.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(context, "com.qw.example.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
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
