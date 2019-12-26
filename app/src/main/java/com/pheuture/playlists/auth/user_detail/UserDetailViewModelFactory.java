package com.pheuture.playlists.auth.user_detail;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.pheuture.playlists.datasource.local.user_handler.UserModel;

import org.jetbrains.annotations.NotNull;

class UserDetailViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private UserModel userModel;

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        return (T) new UserDetailViewModel(mApplication, userModel);
    }

    public UserDetailViewModelFactory(Application application, UserModel userModel) {
        this.mApplication = application;
        this.userModel = userModel;
    }
}
