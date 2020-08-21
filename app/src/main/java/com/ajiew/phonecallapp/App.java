package com.ajiew.phonecallapp;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.ajiew.phonecallapp.listenphonecall.CallListenerService;
import com.ajiew.phonecallapp.logger.FileFormatStrategy;
import com.ajiew.phonecallapp.utils.PermissionUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class App extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

//        CallRecord builder = new CallRecord(this);
//        builder.startCallReceiver();
        Intent callListener = new Intent(this, CallListenerService.class);
        startService(callListener);

        if (PermissionUtil.checkHasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            FormatStrategy fileFormatStrategy = FileFormatStrategy.newBuilder()
                    .tag("MyApp V" + BuildConfig.VERSION_NAME)
                    .build(getPackageName());
            Logger.addLogAdapter(new DiskLogAdapter(fileFormatStrategy));
            FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                    .tag("MyApp V" + BuildConfig.VERSION_NAME)
                    .build();
            Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));

        } else {
            Logger.d("没有读写权限");
        }
    }
}
