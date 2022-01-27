package com.qw.example;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.qw.download.DownloadConfig;
import com.qw.download.DownloadEntry;
import com.qw.download.DownloadManager;
import com.qw.download.DownloadWatcher;
import com.qw.example.core.BaseActivity;
import com.qw.example.widget.QProgress;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;

/**
 * Created by qinwei on 2016/4/15 15:37
 * email:qinwei_it@163.com
 */
public class MultithreadDownloadListActivity extends BaseActivity {
    private ListView mDownloadLv;
    private final ArrayList<ApkEntry> modules = new ArrayList<>();
    private DownloadAdapter adapter;
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onChanged(DownloadEntry e) {
            if (e.state == DownloadEntry.State.CANCELLED) {
                modules.remove(e);
            }
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pauseAll:
                DownloadManager.pauseAll();
                break;
            case R.id.resumeAll:
                DownloadManager.recoverAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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
//        modules.add(new DownloadEntry("weixin_1420.apk", "http://gdown.baidu.com/data/wisegame/00984e94e708c913/weixin_1420.apk"));
//        modules.add(new DownloadEntry("mobileqq_android.apk", "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk"));
//        modules.add(new DownloadEntry("douyin.apk", "http://ws.yingyonghui.com/fde654f91a2f49741933840c4d0c367c/5cf720b4/apk/6447153/a1e5c3b63c3f9cf28b27baefaa0b315b"));
//        modules.add(new DownloadEntry("虾米.apk", "http://download.taobaocdn.com/wireless/xiami-android-spark/latest/xiami-android-spark_701287.apk"));
        modules.addAll(gen());
        adapter.notifyDataSetChanged();
    }

    private ArrayList<ApkEntry> gen() {
        ArrayList<ApkEntry> apkEntries = new ArrayList<>();
        ApkEntry entry = new ApkEntry();
        entry.name = "微信";
        entry.cover = "https://tse2-mm.cn.bing.net/th/id/OIP-C.6xMKyRmO4h6rBaiEPteNigHaHE?w=196&h=188&c=7&r=0&o=5&pid=1.7";
        entry.url = "https://dldir1.qq.com/weixin/android/weixin8019android2080_arm64.apk";
        apkEntries.add(entry);
        entry = new ApkEntry();
        entry.name = "QQ";
        entry.cover = "https://image.16pic.com/00/31/19/16pic_3119624_s.jpg?imageView2/0/format/png";
        entry.url = "https://e2ac3417f3e17ad4cac7098653e78ea6.dlied1.cdntips.net/dlied1.qq.com/qqweb/QQ_1/android_apk/Android_8.8.68.7265_537112589_32.apk?mkey=61f28f693af726ff&f=17c9&cip=58.247.0.10&proto=https&access_type=";
        apkEntries.add(entry);
        entry = new ApkEntry();
        entry.name = "支付宝";
        entry.cover = "https://img0.baidu.com/it/u=3310987054,2404591959&fm=253&fmt=auto&app=138&f=JPEG?w=400&h=400";
        entry.url = "https://t.alipayobjects.com/L1/71/100/and/alipay_wap_main.apk";
        apkEntries.add(entry);
        return apkEntries;
    }


    class DownloadAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return modules.size();
        }

        @Override
        public Object getItem(int position) {
            return modules.get(position);
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
        private TextView mDownloadTitleLabel;
        private TextView mDownloadProgressDesLabel;
        private TextView mDownloadStateLabel;
        private TextView mDownloadLabel;
        private QProgress mDownloadProgress;
        private ImageView mDownloadIconImg;
        private ApkEntry d;

        public void initializeView(View v) {
            mDownloadTitleLabel = (TextView) v.findViewById(R.id.mDownloadTitleLabel);
            mDownloadProgressDesLabel = (TextView) v.findViewById(R.id.mDownloadProgressDesLabel);
            mDownloadProgress = (QProgress) v.findViewById(R.id.mDownloadProgress);
            mDownloadLabel = (TextView) v.findViewById(R.id.mDownloadLabel);
            mDownloadStateLabel = (TextView) v.findViewById(R.id.mDownloadStateLabel);
            mDownloadIconImg = (ImageView) v.findViewById(R.id.mDownloadIconImg);
            mDownloadLabel.setOnClickListener(this);
        }

        public void initializeData(int position) {
            d = modules.get(position);
            mDownloadTitleLabel.setText(d.name);
            mDownloadStateLabel.setText("");
            mDownloadProgressDesLabel.setText("");
            Glide.with(MultithreadDownloadListActivity.this)
                    .load(d.cover)
                    .into(mDownloadIconImg);
            DownloadEntry entry = DownloadManager.findById(d.id());
            if (entry == null) {
                mDownloadLabel.setText("下载");
            } else {
                mDownloadStateLabel.setText(formatSize(entry.speed) + "/s");
                mDownloadProgressDesLabel.setText(formatSize(entry.currentLength) + "/" + formatSize(entry.contentLength));
                mDownloadProgress.setMax((int) entry.contentLength);
                mDownloadProgress.notifyDataChanged((int) entry.currentLength);
                switch (entry.state) {
                    case PAUSED:
                        mDownloadLabel.setText("继续");
                        mDownloadStateLabel.setText("已暂停");
                        break;
                    case WAIT:
                        mDownloadLabel.setText("等待");
                        break;
                    case ING:
                    case CONNECT:
                        mDownloadLabel.setText("暂停");
                        break;
                    case ERROR:
                        mDownloadLabel.setText("重试");
                        break;
                    case CANCELLED:
                    case IDLE:
                        mDownloadLabel.setText("下载");
                        break;
                    case DONE:
                        mDownloadLabel.setText("打开");
                        mDownloadStateLabel.setText("已完成");
                        break;
                    default:
                        break;
                }
            }

        }

        @Override
        public void onClick(View v) {
            DownloadEntry entry = DownloadManager.findById(d.id());
            if (entry == null) {
                entry = new DownloadEntry(d.id(), d.url);
            }
            switch (entry.state) {
                case PAUSED:
                case ERROR:
                    DownloadManager.resume(entry);
                    break;
                case ING:
                case WAIT:
                case CONNECT:
                    mDownloadLabel.setText("暂停");
                    DownloadManager.pause(entry);
                    break;
                case CANCELLED:
                case IDLE:
                    DownloadManager.add(entry);
                    break;
                case DONE:
                    File apkFile = DownloadConfig.getInstance().getDownloadFile(d.url);
                    if (!apkFile.exists()) {
                        return;
                    }
                    // 通过Intent安装APK文件
                    loadInstallApk(MultithreadDownloadListActivity.this, apkFile);
                    break;
                default:
                    break;
            }
        }

    }

    private String formatSize(long size) {
        String unit = "";
        String newSize = "0";
        if (size >= 1024) {
            unit = "KB";
            size = (long) (size / 1024f);
            newSize = size + "";
            if (size >= 1024) {
                unit = "MB";
                newSize = new Formatter().format("%.1f", size / 1024f).toString();
            }
        }
        return newSize + unit;
    }

    public static void loadInstallApk(Context context, File file) {
        if (!file.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(context, "com.qw.download.fileprovider", file);
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
        DownloadManager.addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadManager.removeObserver(watcher);
    }

}
