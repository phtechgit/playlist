package com.pheuture.playlists.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReceiver extends BroadcastReceiver {
    private NotificationActionInterface notificationActionInterface;

    public NotificationActionReceiver(NotificationActionInterface notificationActionInterface) {
        this.notificationActionInterface = notificationActionInterface;
    }

    public NotificationActionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null){
         return;
        }
        if (intent.getAction() == null){
            return;
        }

        if (intent.getAction().equals(NotificationActions.CANCEL)){
            if (notificationActionInterface!=null) {
                notificationActionInterface.onNotificationCancelled();
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
