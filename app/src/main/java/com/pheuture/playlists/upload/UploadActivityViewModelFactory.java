package com.pheuture.playlists.upload;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.pheuture.playlists.playlist.detail.PlaylistDetailViewModel;

import org.jetbrains.annotations.NotNull;

class UploadActivityViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private Uri mediaUri;

    @NotNull
    @Override
    public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        return (T) new UploadViewModel(mApplication, mediaUri);
    }

    public UploadActivityViewModelFactory(Application application, Uri mediaUri) {
        this.mApplication = application;
        this.mediaUri = mediaUri;
    }
}
