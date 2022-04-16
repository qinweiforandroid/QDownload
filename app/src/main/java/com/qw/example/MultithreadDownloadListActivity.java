package com.qw.example;

import android.Manifest;
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
import com.qw.download.DownloadConfig;
import com.qw.download.DownloadEntry;
import com.qw.download.DownloadManager;
import com.qw.download.DownloadWatcher;
import com.qw.example.core.BaseActivity;
import com.qw.example.widget.QProgress;
import com.qw.permission.OnRequestPermissionsResultListener;
import com.qw.permission.Permission;
import com.qw.permission.PermissionResult;

import org.jetbrains.annotations.NotNull;

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

        Permission.Companion.init(this)
                .permissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .setOnRequestPermissionsResultListener(new OnRequestPermissionsResultListener() {
                    @Override
                    public void onRequestPermissionsResult(@NotNull PermissionResult permissionResult) {
                        if (!permissionResult.isGrant()) {
                            Toast.makeText(MultithreadDownloadListActivity.this, "需要sdcard读写权限", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .request();
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
                if (entry.contentLength == 0L) {
                    mDownloadProgress.setMax(1);
                    mDownloadProgress.notifyDataChanged(0);
                } else {
                    mDownloadProgress.setMax((int) entry.contentLength);
                    mDownloadProgress.notifyDataChanged((int) entry.currentLength);
                }
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
                entry = DownloadEntry.obtain(d.id(), d.url);
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
