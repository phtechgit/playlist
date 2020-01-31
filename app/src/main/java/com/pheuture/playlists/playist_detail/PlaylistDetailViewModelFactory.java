package com.pheuture.playlists.playist_detail;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

class PlaylistDetailViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private long playlistID;

    @NotNull
    @Override
    public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        return (T) new PlaylistDetailViewModel(mApplication, playlistID);
    }

    public PlaylistDetailViewModelFactory(Application application, long playlistID) {
        this.mApplication = application;
        this.playlistID = playlistID;
    }
}
