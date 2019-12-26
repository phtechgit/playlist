package com.pheuture.playlists.auth.user_detail;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadDao;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserModel;
import com.pheuture.playlists.service.PendingApiExecutorService;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;

public class UserProfileViewModel extends AndroidViewModel {
    private static final String TAG = UserProfileViewModel.class.getSimpleName();
    private UserModel userModel;
    private PendingUploadDao pendingUploadDao;

    public UserProfileViewModel(@NonNull Application application, UserModel user) {
        super(application);
        this.userModel = user;
        pendingUploadDao = LocalRepository.getInstance(application).pendingUploadDao();
    }

    public void updateUserDetail(String firstName, String lastName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(firstName);
        if (TextUtils.getTrimmedLength(lastName) >0) {
            stringBuilder.append(" ");
            stringBuilder.append(lastName);
        }
        userModel.setUserName(stringBuilder.toString());

        SharedPrefsUtils.setStringPreference(getApplication(), Constants.USER,
                ParserUtil.getInstance().toJson(userModel, UserModel.class));

        PendingUploadEntity pendingUploadEntity = new PendingUploadEntity();
        pendingUploadEntity.setUrl(Url.UPDATE_USER_DETAIL);
        pendingUploadEntity.setParams(ParserUtil.getInstance().toJson(userModel, UserModel.class));
        pendingUploadDao.insert(pendingUploadEntity);

        PendingApiExecutorService.startService(getApplication());
    }
}
