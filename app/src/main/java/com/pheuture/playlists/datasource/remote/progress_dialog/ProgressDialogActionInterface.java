package com.pheuture.playlists.datasource.remote.progress_dialog;

import android.content.Context;

public interface ProgressDialogActionInterface {
    void show(String title);
    void dismiss();
    boolean isShowing();
    void setProgress(int progress);
    void setMessage(String message);

    interface ClickListener{
        void onCancelled();
    }
}
