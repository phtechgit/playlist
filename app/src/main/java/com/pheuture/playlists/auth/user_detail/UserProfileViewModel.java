package com.pheuture.playlists.auth.user_detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.pheuture.playlists.base.LocalRepository;
import com.pheuture.playlists.base.service.PendingApiLocalDao;
import com.pheuture.playlists.base.service.PendingApiEntity;
import com.pheuture.playlists.auth.UserEntity;
import com.pheuture.playlists.base.service.PendingApiExecutorService;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.ParserUtil;
import com.pheuture.playlists.base.utils.SharedPrefsUtils;
import com.pheuture.playlists.base.constants.Url;

import java.util.Calendar;

public class UserProfileViewModel extends AndroidViewModel  {
    private static final String TAG = UserProfileViewModel.class.getSimpleName();
    private UserEntity userEntity;
    private PendingApiLocalDao pendingApiLocalDao;

    public UserProfileViewModel(@NonNull Application application, UserEntity user) {
        super(application);
        this.userEntity = user;
        pendingApiLocalDao = LocalRepository.getInstance(application).pendingApiLocalDao();
    }

    public void updateUserDetail(String firstName, String lastName) {
        Calendar calendar = Calendar.getInstance();
        userEntity.setUserFirstName(firstName);
        userEntity.setUserLastName(lastName);
        userEntity.setCreatedOn(calendar.getTimeInMillis());
        userEntity.setModifiedOn(calendar.getTimeInMillis());

        SharedPrefsUtils.setStringPreference(getApplication(), Constants.USER,
                ParserUtil.getInstance().toJson(userEntity, UserEntity.class));

        PendingApiEntity pendingFileUploadEntity = new PendingApiEntity();
        pendingFileUploadEntity.setUrl(Url.UPDATE_USER_DETAIL);
        pendingFileUploadEntity.setParams(ParserUtil.getInstance().toJson(userEntity, UserEntity.class));
        pendingApiLocalDao.insert(pendingFileUploadEntity);

        PendingApiExecutorService.startService(getApplication());
    }
}
