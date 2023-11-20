package com.qw.example.multithreading;

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
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.qw.download.DownloadInfo;
import com.qw.download.manager.DownloadManager;
import com.qw.download.manager.DownloadWatcher;
import com.qw.download.manager.FileDownload;
import com.qw.example.base.BaseActivity;
import com.qw.example.base.QProgress;
import com.qw.example.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;

/**
 * Created by qinwei on 2016/4/15 15:37
 * email:qinwei_it@163.com
 */
public class MultithreadingDownloadListActivity extends BaseActivity {
    private ListView mDownloadLv;
    private final ArrayList<ApkEntry> modules = new ArrayList<>();
    private DownloadAdapter adapter;
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onChanged(DownloadInfo e) {
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
        int id = item.getItemId();
        if (id == R.id.pauseAll) {
            FileDownload.pauseAll();
        } else if (id == R.id.resumeAll) {
            FileDownload.recoverAll();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.multithread_list_activity);
    }

    @Override
    protected void initializeView() {
        mDownloadLv = findViewById(R.id.mDownloadLv);
        adapter = new DownloadAdapter();
        mDownloadLv.setAdapter(adapter);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        setTitle("多任务多线程断点下载");
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
        entry.url = "https://ip218338852.out.azhimalayanvh.com/fs08/2022/03/22/10/110_57f4911d9460f343f73b57b59bbc9bed.apk?yingid=wdj_web&fname=QQ&productid=2011&pos=wdj_web%2Fdetail_normal_dl%2F0&appid=566489&packageid=201080855&apprd=566489&iconUrl=http%3A%2F%2Fandroid-artworks.25pp.com%2Ffs08%2F2022%2F04%2F01%2F6%2F110_2431ce87beb7edaa6061e9e5ba3ad2be_con.png&pkg=com.tencent.mobileqq&did=0356ba5bacc5058a13a2d02805479160&vcode=2654&md5=1e3493b2e78f4f11247ec3c9a56e0606&ali_redirect_domain=alissl.ucdl.pp.uc.cn&ali_redirect_ex_ftag=86f43da289ad52bc50c0e23dd018f58ee1663c57d386180b&ali_redirect_ex_tmining_ts=1650107491&ali_redirect_ex_tmining_expire=3600&ali_redirect_ex_hot=111";
        apkEntries.add(entry);
        entry = new ApkEntry();
        entry.name = "支付宝";
        entry.cover = "https://img0.baidu.com/it/u=3310987054,2404591959&fm=253&fmt=auto&app=138&f=JPEG?w=400&h=400";
        entry.url = "https://t.alipayobjects.com/L1/71/100/and/alipay_wap_main.apk";
        apkEntries.add(entry);
        return apkEntries;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //重新激活需要刷新页面
        adapter.notifyDataSetChanged();
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
                convertView = LayoutInflater.from(MultithreadingDownloadListActivity.this).inflate(R.layout.multithread_list_item_layout, null);
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
        private ApkEntry apk;

        public void initializeView(View v) {
            mDownloadTitleLabel = v.findViewById(R.id.mDownloadTitleLabel);
            mDownloadProgressDesLabel = v.findViewById(R.id.mDownloadProgressDesLabel);
            mDownloadProgress = v.findViewById(R.id.mDownloadProgress);
            mDownloadLabel = v.findViewById(R.id.mDownloadLabel);
            mDownloadStateLabel = v.findViewById(R.id.mDownloadStateLabel);
            mDownloadIconImg = v.findViewById(R.id.mDownloadIconImg);
            mDownloadLabel.setOnClickListener(this);
        }

        public void initializeData(int position) {
            apk = modules.get(position);
            Glide.with(MultithreadingDownloadListActivity.this)
                    .load(apk.cover)
                    .into(mDownloadIconImg);
            mDownloadTitleLabel.setText(apk.name);
            mDownloadStateLabel.setText("");
            mDownloadProgressDesLabel.setText("");
            DownloadInfo info = FileDownload.getInfo(apk.id());
            if (info != null) {
                mDownloadStateLabel.setText(formatSize(info.getSpeed()) + "/s");
                mDownloadProgressDesLabel.setText(formatSize(info.getCurrentLength()) + "/" + formatSize(info.getContentLength()));
                if (info.getContentLength() == 0L) {
                    mDownloadProgress.setMax(1);
                    mDownloadProgress.notifyDataChanged(0);
                } else {
                    mDownloadProgress.setMax((int) info.getContentLength());
                    mDownloadProgress.notifyDataChanged((int) info.getCurrentLength());
                }
            }

            if (info == null || info.isIdle()) {
                mDownloadLabel.setText("下载");
            } else if (info.isPaused()) {
                mDownloadLabel.setText("继续");
                mDownloadStateLabel.setText("已暂停");
            } else if (info.isWait()) {
                mDownloadLabel.setText("等待");
            } else if (info.isConnecting()) {
                mDownloadStateLabel.setText("连接中...");
                mDownloadLabel.setText("暂停");
            } else if (info.isDownloading()) {
                mDownloadLabel.setText("暂停");
            } else if (info.isError()) {
                mDownloadLabel.setText("重试");
            } else if (info.isIdle()) {
                mDownloadLabel.setText("下载");
            } else if (info.isDone()) {
                mDownloadLabel.setText("打开");
                mDownloadStateLabel.setText("已完成");
            }

        }

        @Override
        public void onClick(View v) {
            String id = apk.id();
            DownloadInfo info = FileDownload.getInfo(id);

            if (info == null || info.isIdle()) {
                new FileDownload.Builder(id)
                        .setName(apk.name + ".apk")
                        .setUrl(apk.url)
                        .add();
            } else if (info.isPaused() || info.isError()) {
                FileDownload.resume(id);
            } else if (info.isConnecting() || info.isDownloading() || info.isWait()) {
                mDownloadLabel.setText("暂停");
                FileDownload.pause(id);
            } else if (info.isDone()) {
                File apkFile = new File(info.getPath());
                if (!apkFile.exists()) {
                    Toast.makeText(MultithreadingDownloadListActivity.this, "文件已被删除！", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 通过Intent安装APK文件
                loadInstallApk(MultithreadingDownloadListActivity.this, apkFile);
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