package com.pheuture.playlists.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.pheuture.playlists.base.datasource.local.LocalRepository;
import com.pheuture.playlists.media.OfflineMediaLocalDao;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.Logger;

import java.io.File;

public class SettingsViewModel extends AndroidViewModel {
    private static final String TAG = SettingsViewModel.class.getSimpleName();
    private OfflineMediaLocalDao offlineMediaLocalDao;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        offlineMediaLocalDao = LocalRepository.getInstance(application).offlineMediaLocalDao();
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
                    offlineMediaLocalDao.deleteAll();
                }
            }
        }
    }
}