package com.pheuture.playlists.auth.user_detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadDao;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.service.PendingApiExecutorService;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;

public class UserProfileViewModel extends AndroidViewModel implements PendingUploadEntity.UploadType {
    private static final String TAG = UserProfileViewModel.class.getSimpleName();
    private UserEntity userEntity;
    private PendingUploadDao pendingUploadDao;

    public UserProfileViewModel(@NonNull Application application, UserEntity user) {
        super(application);
        this.userEntity = user;
        pendingUploadDao = LocalRepository.getInstance(application).pendingUploadDao();
    }

    public void updateUserDetail(String firstName, String lastName) {
        userEntity.setUserFirstName(firstName);
        userEntity.setUserLastName(lastName);

        SharedPrefsUtils.setStringPreference(getApplication(), Constants.USER,
                ParserUtil.getInstance().toJson(userEntity, UserEntity.class));

        PendingUploadEntity pendingUploadEntity = new PendingUploadEntity();
        pendingUploadEntity.setType(SIMPLE);
        pendingUploadEntity.setUrl(Url.UPDATE_USER_DETAIL);
        pendingUploadEntity.setParams(ParserUtil.getInstance().toJson(userEntity, UserEntity.class));
        pendingUploadDao.insert(pendingUploadEntity);

        PendingApiExecutorService.startService(getApplication());
    }
}
