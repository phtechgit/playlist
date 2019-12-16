package com.pheuture.playlists;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineVideoDao;
import com.pheuture.playlists.utils.Logger;
import java.io.File;
import java.io.IOException;

import static android.content.Context.DOWNLOAD_SERVICE;

public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = DownloadReceiver.class.getSimpleName();
    private OfflineVideoDao offlineVideoDao;

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        offlineVideoDao = LocalRepository.getInstance(context).offlineVideoDao();

        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (downloadId>-1){
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);

            assert downloadManager != null;
            Cursor cursor = downloadManager.query(query);

            if (cursor.moveToFirst()) {
                if (cursor.getCount() > 0) {
                    int statusOfTheDownload = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                    if (statusOfTheDownload != DownloadManager.STATUS_SUCCESSFUL) {
                        offlineVideoDao.updateOfflineVideoStatus(downloadId, new File(uriString).getPath(), statusOfTheDownload);
                    } else {
                        Uri sourceFileUri = Uri.parse(uriString);
                        FileMoveIntentService.startActionMove(context, downloadId, sourceFileUri.getPath());
                    }
                }
            }
        }
    }
}
