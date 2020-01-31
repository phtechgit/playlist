package com.pheuture.playlists.base;

import android.app.Application;
import android.content.IntentFilter;

import com.pheuture.playlists.base.receiver.ConnectivityChangeReceiver;
import com.pheuture.playlists.base.service.PendingApiExecutorService;
import com.pheuture.playlists.base.service.PendingFileUploadService;

public class AppController extends Application implements ConnectivityChangeReceiver.ConnectivityChangeListener{
    private static final String TAG = AppController.class.getSimpleName();
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        setupConnectivityChangeBroadcastReceiver();
    }

    private void setupConnectivityChangeBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        registerReceiver(connectivityChangeReceiver, intentFilter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(connectivityChangeReceiver);
    }

    @Override
    public void onConnectivityChange(boolean connected) {
        PendingApiExecutorService.startService(this);
        PendingFileUploadService.startService(this);
    }
}
