package com.pheuture.playlists.media;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.pheuture.playlists.playlist.PlaylistEntity;

import org.jetbrains.annotations.NotNull;

class MediaViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private PlaylistEntity model;

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        return (T) new MediaViewModel(mApplication, model);
    }

    public MediaViewModelFactory(Application application, PlaylistEntity model) {
        this.mApplication = application;
        this.model = model;
    }
}
