package com.pheuture.playlists;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineVideoDao;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class FileMoveIntentService extends IntentService {
    private static final String ACTION_MOVE = "com.pheuture.playlists.action.MOVE";
    private static final String ACTION_COPY = "com.pheuture.playlists.action.COPY";
    private static final String TAG = FileMoveIntentService.class.getSimpleName();
    private OfflineVideoDao offlineVideoDao;

    public FileMoveIntentService() {
        super("FileMoveIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        offlineVideoDao = LocalRepository.getInstance(this).offlineVideoDao();
    }

    public static void startActionMove(Context context, long downloadID, String source) {
        Intent intent = new Intent(context, FileMoveIntentService.class);
        intent.setAction(ACTION_MOVE);
        intent.putExtra(Constants.ARG_PARAM1, downloadID);
        intent.putExtra(Constants.ARG_PARAM2, source);
        context.startService(intent);
    }

    public static void startActionCopy(Context context, String param1, String param2) {
        Intent intent = new Intent(context, FileMoveIntentService.class);
        intent.setAction(ACTION_COPY);
        intent.putExtra(Constants.ARG_PARAM1, param1);
        intent.putExtra(Constants.ARG_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MOVE.equals(action)) {
                final long param1 = intent.getLongExtra(Constants.ARG_PARAM1, -1);
                final String param2 = intent.getStringExtra(Constants.ARG_PARAM2);
                handleActionMove(param1, param2);
            } else if (ACTION_COPY.equals(action)) {
                final String param1 = intent.getStringExtra(Constants.ARG_PARAM1);
                final String param2 = intent.getStringExtra(Constants.ARG_PARAM2);
                handleActionCopy(param1, param2);
            }
        }
    }

    private void handleActionMove(long downloadId, String source) {
        try {
            Calendar calendar = Calendar.getInstance();
            File destinationFile = new File(getFilesDir(), String.valueOf(calendar.getTimeInMillis()));
            if (!destinationFile.exists()){
                if (!destinationFile.createNewFile()) {
                    Logger.e(TAG, "failed to create destination file");
                    return;
                }
                Logger.e(TAG, "destination file created successfully");
            }

            InputStream in = new FileInputStream(source);
            OutputStream out = openFileOutput(destinationFile.getName(), MODE_PRIVATE);

            byte[] buf = new byte[512];
            int len;
            long total = 0;
            while ((len = in.read(buf)) != -1) {
                total += len;

               /* publishProgress((int)((total*100)/lenghtOfFile));*/
                out.write(buf, 0, len);
            }

            in.close();
            out.close();

            //update file path in room db
            offlineVideoDao.updateOfflineVideoStatus(downloadId, destinationFile.getAbsolutePath(), DownloadManager.STATUS_SUCCESSFUL);

            //delete external storage downloaded file
            File sourceFile = new File(source);
            if (sourceFile.exists()){
                if (sourceFile.delete()) {
                    Logger.e(TAG, "source file deleted after moving to app storage");
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
    }

    private void handleActionCopy(String param1, String param2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
