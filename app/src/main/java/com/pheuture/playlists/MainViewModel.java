package com.pheuture.playlists;

import android.app.Application;
import android.app.DownloadManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineVideoDao;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineVideoEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;

import java.io.File;
import java.util.List;

import static android.content.Context.DOWNLOAD_SERVICE;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer1;
    private SimpleExoPlayer exoPlayer2;
    private MutableLiveData<PlaylistEntity> playlist;
    private MutableLiveData<List<VideoEntity>> videos;
    private OfflineVideoDao offlineVideoDao;

    public MainViewModel(@NonNull Application application) {
        super(application);
        dataSourceFactory = new DefaultDataSourceFactory(application,
                Util.getUserAgent(application, TAG));
        exoPlayer1 = ExoPlayerFactory.newSimpleInstance(application);
        exoPlayer2 = ExoPlayerFactory.newSimpleInstance(application);

        offlineVideoDao = LocalRepository.getInstance(application).offlineVideoDao();

        playlist = new MutableLiveData<>();
        videos = new MutableLiveData<>();
    }

    public SimpleExoPlayer getExoPlayer1() {
        return exoPlayer1;
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

    public SimpleExoPlayer getExoPlayer2() {
        return exoPlayer2;
    }

    public OfflineVideoEntity getOfflineMediaForMediaID(long mediaID) {
        return offlineVideoDao.getOfflineMedia(mediaID);
    }
}
