package com.pheuture.playlists;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineVideoDao;
import static android.content.Context.DOWNLOAD_SERVICE;

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        OfflineVideoDao offlineVideoDao = LocalRepository.getInstance(context).offlineVideoDao();

        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (downloadId>-1){
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);

            assert downloadManager != null;
            Cursor cursor = downloadManager.query(query);

            if (cursor.moveToFirst()) {
                if (cursor.getCount() > 0) {
                    int statusOfTheDownload = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    offlineVideoDao.updateOfflineVideoStatus(downloadId, statusOfTheDownload);
                }
            }
        }
    }
}
