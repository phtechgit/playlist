package com.pheuture.playlists.playlists.detail;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;

import org.jetbrains.annotations.NotNull;

class PlaylistDetailViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private PlaylistEntity model;

    @NotNull
    @Override
    public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        return (T) new PlaylistDetailViewModel(mApplication, model);
    }

    public PlaylistDetailViewModelFactory(Application application, PlaylistEntity model) {
        this.mApplication = application;
        this.model = model;
    }
}
