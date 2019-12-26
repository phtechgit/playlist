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
import com.pheuture.playlists.datasource.local.user_handler.UserModel;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineMediaDao;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineMediaEntity;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer1;
    private SimpleExoPlayer exoPlayer2;
    private MutableLiveData<PlaylistEntity> playlist;
    private MutableLiveData<List<PlaylistMediaEntity>> playlistMediaEntites;
    private OfflineMediaDao offlineMediaDao;
    private UserModel user;

    public MainViewModel(@NonNull Application application) {
        super(application);
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserModel.class);

        dataSourceFactory = new DefaultDataSourceFactory(application,
                Util.getUserAgent(application, TAG));
        exoPlayer1 = ExoPlayerFactory.newSimpleInstance(application);
        exoPlayer2 = ExoPlayerFactory.newSimpleInstance(application);

        offlineMediaDao = LocalRepository.getInstance(application).offlineVideoDao();

        playlist = new MutableLiveData<>();
        playlistMediaEntites = new MutableLiveData<>();
    }

    public SimpleExoPlayer getExoPlayer1() {
        return exoPlayer1;
    }

    public void setPlaylist(PlaylistEntity newPlaylist) {
        playlist.postValue(newPlaylist);
    }

    public MutableLiveData<PlaylistEntity> getPlaylist() {
        return playlist;
    }

    public void setPlaylistMediaEntities(List<PlaylistMediaEntity> playlistMediaEntities) {
        playlistMediaEntites.postValue(playlistMediaEntities);
    }

    public MutableLiveData<List<PlaylistMediaEntity>> getPlaylistMediaEntities() {
        return playlistMediaEntites;
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
}
