package com.pheuture.playlists.base.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pheuture.playlists.base.utils.Logger;

public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = NotificationActionReceiver.class.getSimpleName();
    private NotificationActionInterface notificationActionInterface;

    public void setNotificationActionInterface(NotificationActionInterface notificationActionInterface) {
        this.notificationActionInterface = notificationActionInterface;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.e(TAG, "onReceive");
        if (intent == null){
         return;
        }
        if (intent.getAction() == null){
            return;
        }

        if (intent.getAction().equals(NotificationActions.CANCEL)){
            if (notificationActionInterface!=null) {
                Logger.e(TAG, "onNotificationCancelled called");
                notificationActionInterface.onNotificationCancelled();
            } else {
                Logger.e(TAG, "notificationActionInterface is null");
            }
        }
    }

    public interface NotificationActions{
        String CANCEL = "cancel";
    }

    public interface NotificationActionInterface {
        void onNotificationCancelled();
    }
}
