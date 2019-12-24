package com.pheuture.playlists.auth.verify_otp;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

class VerifyOtpViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private String phone;

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        return (T) new VerifyOtpViewModel(mApplication, phone);
    }

    public VerifyOtpViewModelFactory(Application application, String phone) {
        this.mApplication = application;
        this.phone = phone;
    }
}
