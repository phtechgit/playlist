package com.pheuture.playlists;

import android.app.Application;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.media_handler.offline.OfflineMediaDao;
import com.pheuture.playlists.datasource.local.media_handler.offline.OfflineMediaEntity;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.receiver.ConnectivityChangeReceiver;
import com.pheuture.playlists.utils.Constants;

import java.util.List;

public class MainActivityViewModel extends BaseAndroidViewModel implements
        Constants.SnackBarConstants {
    private static final String TAG = MainActivityViewModel.class.getSimpleName();
    private MutableLiveData<String> title;
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer1;
    private SimpleExoPlayer exoPlayer2;
    private MutableLiveData<PlaylistEntity> playlistMutableLiveData;
    private MutableLiveData<List<PlaylistMediaEntity>> playlistMediaEntitesMutableLiveData;
    private OfflineMediaDao offlineMediaDao;
    private MutableLiveData<Boolean> isNewMediaAddedToPlaylist;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);

        title = new MutableLiveData<>();

        dataSourceFactory = new DefaultDataSourceFactory(application,
                Util.getUserAgent(application, TAG));
        exoPlayer1 = ExoPlayerFactory.newSimpleInstance(application);
        exoPlayer2 = ExoPlayerFactory.newSimpleInstance(application);

        offlineMediaDao = LocalRepository.getInstance(application).offlineMediaDao();

        playlistMutableLiveData = new MutableLiveData<>();
        playlistMediaEntitesMutableLiveData = new MutableLiveData<>();
        isNewMediaAddedToPlaylist = new MutableLiveData<>();
    }

    public SimpleExoPlayer getExoPlayer1() {
        return exoPlayer1;
    }

    public void setPlaylistMutableLiveData(PlaylistEntity newPlaylist) {
        playlistMutableLiveData.postValue(newPlaylist);
    }

    public MutableLiveData<PlaylistEntity> getPlaylistMutableLiveData() {
        return playlistMutableLiveData;
    }

    public void setPlaylistMediaEntities(List<PlaylistMediaEntity> playlistMediaEntities) {
        playlistMediaEntitesMutableLiveData.postValue(playlistMediaEntities);
    }

    public MutableLiveData<List<PlaylistMediaEntity>> getPlaylistMediaEntities() {
        return playlistMediaEntitesMutableLiveData;
    }

    public DataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }

    public SimpleExoPlayer getExoPlayer2() {
        return exoPlayer2;
    }

    public OfflineMediaEntity getOfflineMediaForMediaID(long mediaID) {
        return offlineMediaDao.getOfflineMedia(mediaID);
    }

    public void setNewMediaAdded(boolean b) {
        isNewMediaAddedToPlaylist.postValue(b);
    }

    public MutableLiveData<Boolean> isNewMediaAddedToPlaylist(){
        return isNewMediaAddedToPlaylist;
    }

    public MutableLiveData<String> getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title.postValue(title);
    }

    @Override
    protected void onCleared() {
        exoPlayer1.release();
        exoPlayer2.release();
        super.onCleared();
    }
}
