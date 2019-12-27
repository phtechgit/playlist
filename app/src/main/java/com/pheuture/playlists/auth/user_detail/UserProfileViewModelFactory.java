package com.pheuture.playlists.auth.user_detail;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.pheuture.playlists.datasource.local.user_handler.UserEntity;

import org.jetbrains.annotations.NotNull;

class UserProfileViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private UserEntity userEntity;

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        return (T) new UserProfileViewModel(mApplication, userEntity);
    }

    public UserProfileViewModelFactory(Application application, UserEntity userEntity) {
        this.mApplication = application;
        this.userEntity = userEntity;
    }
}
