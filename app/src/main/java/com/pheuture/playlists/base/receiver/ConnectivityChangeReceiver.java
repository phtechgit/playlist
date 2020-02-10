package com.pheuture.playlists.base.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pheuture.playlists.base.utils.NetworkUtils;
import com.pheuture.playlists.base.utils.SingleMutableLiveData;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    private static final String TAG = ConnectivityChangeReceiver.class.getSimpleName();
    private ConnectivityChangeListener connectivityChangeListener;
    private SingleMutableLiveData networkStatus = new SingleMutableLiveData();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context instanceof ConnectivityChangeListener){
            connectivityChangeListener = (ConnectivityChangeListener) context;
        }

        String action = intent.getAction();
        if (action==null){
            return;
        }

        if (!action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
           return;
        }

        /*if (!isInitialStickyBroadcast()) {
            // Do not ignore this call, as this is not the sticky broadcast and the connectivity
            // state has actually changed
            if (connectivityChangeListener!=null){
                connectivityChangeListener.onConnectivityChange(NetworkUtils.isConnected(context));
            }
        }*/
        if (connectivityChangeListener!=null){
            connectivityChangeListener.onConnectivityChange(NetworkUtils.isConnected(context));
        }
        networkStatus.postValue(NetworkUtils.isConnected(context));
    }

    public interface ConnectivityChangeListener{
        void onConnectivityChange(boolean connected);
    }
}
