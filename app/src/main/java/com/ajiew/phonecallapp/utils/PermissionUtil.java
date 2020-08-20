package com.ajiew.phonecallapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


public class PermissionUtil {
    public static boolean checkHasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    public static String[] initPermissions(){
        String[] permissions = new String[27];
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        permissions[1] = Manifest.permission.RECEIVE_BOOT_COMPLETED;
        permissions[2] = Manifest.permission.BLUETOOTH;
        permissions[3] = Manifest.permission.RECORD_AUDIO;
        permissions[4] = Manifest.permission.INTERNET;
        permissions[5] = Manifest.permission.READ_PHONE_STATE;
        permissions[6] = Manifest.permission.DISABLE_KEYGUARD;
        permissions[7] = Manifest.permission.WAKE_LOCK;
        permissions[8] = Manifest.permission.READ_EXTERNAL_STORAGE;
        permissions[9] = Manifest.permission.CAMERA;
        permissions[10] = Manifest.permission.VIBRATE;
        permissions[11] = Manifest.permission.CHANGE_NETWORK_STATE;
        permissions[12] = Manifest.permission.ACCESS_NETWORK_STATE;
        permissions[13] = Manifest.permission.ACCESS_WIFI_STATE;
        permissions[14] = Manifest.permission.PROCESS_OUTGOING_CALLS;
        permissions[15] = Manifest.permission.ACCESS_COARSE_LOCATION;
        permissions[16] = Manifest.permission.CHANGE_WIFI_STATE;
        permissions[17] = Manifest.permission.ACCESS_FINE_LOCATION;
        permissions[18] = Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS;
        permissions[19] = Manifest.permission.SEND_SMS;
        permissions[20] = Manifest.permission.WRITE_CONTACTS;
        permissions[21] = Manifest.permission.READ_CONTACTS;
        permissions[22] = Manifest.permission.WRITE_CALL_LOG;
        permissions[23] = Manifest.permission.READ_CALL_LOG;
        permissions[24] = Manifest.permission.ACCESS_COARSE_LOCATION;
        permissions[25] = Manifest.permission.ACCESS_FINE_LOCATION;
        permissions[26] = Manifest.permission.READ_SMS;
        return permissions;
    }
    public static boolean checkAllPermissions(Context context) {
        boolean isHasPermission = true;
        for (String permission : initPermissions()) {
            if (!checkHasPermission(context, permission)) {
                isHasPermission = false;
                break;
            }
        }
        return isHasPermission;
    }
    public static void requestAllPermissions(Activity activity, boolean isHasPermission){
        if (!isHasPermission) {
            showConfirmPermission(activity, initPermissions());
        }
    }
    private static void showConfirmPermission(Activity activity, String[] permissions) {
        ActivityCompat.requestPermissions(activity, permissions, 1);
    }
    public static void initPermission(Context context, Activity activity){
        if (Build.VERSION.SDK_INT >= 23) {
            boolean isHasPermission = PermissionUtil.checkAllPermissions(context);
            PermissionUtil.requestAllPermissions(activity,isHasPermission);
        }
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)) {
            //悬浮窗权限
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+context.getPackageName()));
            activity.startActivity(intent);
        }
    }
}
