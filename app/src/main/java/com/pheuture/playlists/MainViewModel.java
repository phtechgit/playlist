package com.pheuture.playlists;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer;
    private MutableLiveData<PlaylistEntity> playlist;
    private MutableLiveData<List<VideoEntity>> videos;

    public MainViewModel(@NonNull Application application) {
        super(application);
        dataSourceFactory = new DefaultDataSourceFactory(application,
                Util.getUserAgent(application, TAG));
        exoPlayer = ExoPlayerFactory.newSimpleInstance(application);
        playlist = new MutableLiveData<>();
        videos = new MutableLiveData<>();
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public void setPlaylist(PlaylistEntity newPlaylist) {
        playlist.postValue(newPlaylist);
    }

    public void setVideos(List<VideoEntity> newVideos) {
        videos.postValue(newVideos);
    }

    public MutableLiveData<PlaylistEntity> getPlaylist() {
        return playlist;
    }

    public MutableLiveData<List<VideoEntity>> getVideos() {
        return videos;
    }

    public DataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }
}
