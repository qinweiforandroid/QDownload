package com.qw.example.core;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by qinwei on 2016/4/22 11:17
 * email:qinwei_it@163.com
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        initializeView();
        initializeData(savedInstanceState);
    }

    protected void setContentView(int resId, boolean isBack) {
        super.setContentView(resId);
        getSupportActionBar().setDisplayHomeAsUpEnabled(isBack);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        setContentView(layoutResID, true);
    }

    protected abstract void setContentView();

    protected abstract void initializeView();

    protected abstract void initializeData(Bundle savedInstanceState);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
