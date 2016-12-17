package com.qw.example;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.qw.download.DownloadConfig;
import com.qw.download.DownloadFileUtil;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testName() throws Exception {
        DownloadConfig.getInstance().setDownloadDirPath("/ecpay/apk/");
        String downloadPath=DownloadFileUtil.getDownloadPath("1.apk");
        System.out.print(downloadPath);
    }
}