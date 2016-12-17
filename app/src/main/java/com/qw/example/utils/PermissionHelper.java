package com.qw.example.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by qinwei on 2016/11/22 10:46
 * email:qinwei_it@163.com
 */

public class PermissionHelper {
    public static final int ASK_WRITE_EXTERNAL_STORAGE = 100;
    public static final int ASK_READ_PHONE_STATE = 101;
    public static final int ASK_CAMERA = 102;
    public static final int ASK_CALL_PHONE = 103;

    public static final class Permission{
        public static final String STORAGE= Manifest.permission.WRITE_EXTERNAL_STORAGE;
        public static final String CAMERA= Manifest.permission.CAMERA;
        public static final String CALL_PHONE= Manifest.permission.CALL_PHONE;
        public static final String READ_PHONE_STATE= Manifest.permission.READ_PHONE_STATE;
        public static final String SEND_SMS= Manifest.permission.SEND_SMS;
    }
    public static boolean requestPermission(Activity context, String permission, int requestCode) {
        return requestPermission(context, new String[]{permission}, requestCode);
    }

    public static boolean requestPermission(Activity context, String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkPermission = ContextCompat.checkSelfPermission(context, permissions[0]);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, permissions, requestCode);
                return false;
            }
        }
        return true;
    }
}
