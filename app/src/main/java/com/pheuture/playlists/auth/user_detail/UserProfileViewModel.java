package com.pheuture.playlists.auth.user_detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiDao;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiEntity;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.service.PendingApiExecutorService;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;

public class UserProfileViewModel extends AndroidViewModel  {
    private static final String TAG = UserProfileViewModel.class.getSimpleName();
    private UserEntity userEntity;
    private PendingApiDao pendingApiDao;

    public UserProfileViewModel(@NonNull Application application, UserEntity user) {
        super(application);
        this.userEntity = user;
        pendingApiDao = LocalRepository.getInstance(application).pendingApiDao();
    }

    public void updateUserDetail(String firstName, String lastName) {
        userEntity.setUserFirstName(firstName);
        userEntity.setUserLastName(lastName);

        SharedPrefsUtils.setStringPreference(getApplication(), Constants.USER,
                ParserUtil.getInstance().toJson(userEntity, UserEntity.class));

        PendingApiEntity pendingFileUploadEntity = new PendingApiEntity();
        pendingFileUploadEntity.setUrl(Url.UPDATE_USER_DETAIL);
        pendingFileUploadEntity.setParams(ParserUtil.getInstance().toJson(userEntity, UserEntity.class));
        pendingApiDao.insert(pendingFileUploadEntity);

        PendingApiExecutorService.startService(getApplication());
    }
}
