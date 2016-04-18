package com.qw.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.qw.download.DownloadEntity;
import com.qw.download.DownloadManager;
import com.qw.download.DownloadWatcher;

import java.util.ArrayList;

/**
 * Created by qinwei on 2016/4/15 15:37
 * email:qinwei_it@163.com
 */
public class ListActivity extends AppCompatActivity {
    private ListView mDownloadLv;
    private ArrayList<DownloadEntity> datas;
    private DownloadAdapter adapter;
    private DownloadWatcher watcher = new DownloadWatcher() {
        @Override
        protected void onDataChanged(DownloadEntity e) {
//            Log.e("ListActivity", e.toString());
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        assignViews();
    }


    private void assignViews() {
        mDownloadLv = (ListView) findViewById(R.id.mDownloadLv);
        datas = new ArrayList<>();
        initializeDatas();
        adapter = new DownloadAdapter();
        mDownloadLv.setAdapter(adapter);
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
                convertView = LayoutInflater.from(ListActivity.this).inflate(R.layout.activity_list_item, null);
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
                case idle:
                    DownloadManager.getInstance(getApplicationContext()).addDownload(e);
                    break;
                default:
                    break;
            }
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

    public void initializeDatas() {
        datas.add(new DownloadEntity("baidu.apk", "baidu"));
        datas.add(new DownloadEntity("tengxun.apk", "tengxun"));
        datas.add(new DownloadEntity("alibaba.apk", "alibaba"));
        datas.add(new DownloadEntity("xiaomi.apk", "xiaomi"));
        datas.add(new DownloadEntity("meizu.apk", "meizu"));
    }
}
