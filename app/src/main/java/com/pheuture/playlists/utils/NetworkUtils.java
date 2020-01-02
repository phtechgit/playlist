package com.pheuture.playlists.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.pheuture.playlists.service.PendingApiExecutorService;

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static boolean online(Context context) {
        if (context==null){
            Logger.e(TAG, "context is null");
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            Logger.e(TAG, "cm is null");
            return false;
        }
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    public static boolean aeroplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
}
