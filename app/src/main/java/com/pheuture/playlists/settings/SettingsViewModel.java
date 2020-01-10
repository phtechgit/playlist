package com.pheuture.playlists.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.media_handler.offline.OfflineMediaDao;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;

import java.io.File;

public class SettingsViewModel extends AndroidViewModel {
    private static final String TAG = SettingsViewModel.class.getSimpleName();
    private OfflineMediaDao offlineMediaDao;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        offlineMediaDao = LocalRepository.getInstance(application).offlineMediaDao();
    }

    public void deleteOfflineMedia() {
        File directory = new File(getApplication().getFilesDir(), Constants.OFFLINE_MEDIA_FOLDER);
        if (directory.exists()){
            if (directory.canExecute()){
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.delete()){
                            Logger.e(TAG, "file deleted");
                        }
                    }
                }
                if (directory.delete()) {
                    offlineMediaDao.deleteAll();
                }
            }
        }
    }
}