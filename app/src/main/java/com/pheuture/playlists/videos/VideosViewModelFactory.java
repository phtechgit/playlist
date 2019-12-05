package com.pheuture.playlists.videos;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.playlists.detail.PlaylistDetailViewModel;

import org.jetbrains.annotations.NotNull;

class VideosViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private PlaylistEntity model;

    @NotNull
    @Override
    public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        return (T) new VideosViewModel(mApplication, model);
    }

    public VideosViewModelFactory(Application application, PlaylistEntity model) {
        this.mApplication = application;
        this.model = model;
    }
}
