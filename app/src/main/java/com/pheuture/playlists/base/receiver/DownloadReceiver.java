package com.pheuture.playlists.base.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.pheuture.playlists.base.service.FileMoveIntentService;
import com.pheuture.playlists.base.LocalRepository;
import com.pheuture.playlists.media.OfflineMediaLocalDao;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = DownloadReceiver.class.getSimpleName();
    private OfflineMediaLocalDao offlineMediaLocalDao;

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        offlineMediaLocalDao = LocalRepository.getInstance(context).offlineMediaLocalDao();

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
                        offlineMediaLocalDao.updateOfflineVideoStatus(downloadId, new File(uriString).getPath(), statusOfTheDownload);
                    } else {
                        Uri sourceFileUri = Uri.parse(uriString);
                        FileMoveIntentService.startActionMove(context, downloadId, sourceFileUri.getPath());
                    }
                }
            }
        }
    }
}
