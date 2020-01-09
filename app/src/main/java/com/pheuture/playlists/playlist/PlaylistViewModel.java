package com.pheuture.playlists.playlist;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiDao;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.service.PendingApiExecutorService;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistViewModel extends AndroidViewModel {
    private static final String TAG = PlaylistViewModel.class.getSimpleName();
    private int offset;
    private int limit = 20;
    private String searchQuery = "";
    private boolean reachedLast;
    private UserEntity user;
    private PlaylistDao playlistDao;
    private PendingApiDao pendingApiDao;
    private PlaylistMediaDao playlistMediaDao;
    private MutableLiveData<List<PlaylistEntity>> playlistEntitiesMutableLiveData;

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        pendingApiDao = LocalRepository.getInstance(application).pendingApiDao();
        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
        playlistEntitiesMutableLiveData = new MutableLiveData<>();

        getFreshData();
    }

    private void getFreshData() {
        offset = 0;
        reachedLast = false;

        List<PlaylistEntity> playlistEntities = playlistEntitiesMutableLiveData.getValue();
        playlistEntities.addAll(playlistDao.getPlaylistList(searchQuery, limit, offset++));
        playlistEntitiesMutableLiveData.postValue(playlistEntities);

        reachedLast = playlistEntities.size() < limit;
    }

    public MutableLiveData<List<PlaylistEntity>> getPlaylistEntitiesMutableLiveData() {
        return playlistEntitiesMutableLiveData;
    }

    public void setSearchQuery(String query) {
        searchQuery = query;
        getFreshData();
    }

    public void getMoreData() {
        if (reachedLast){
            return;
        }
        List<PlaylistEntity> playlistEntities = playlistEntitiesMutableLiveData.getValue();
        playlistEntities.addAll(playlistDao.getPlaylistList(searchQuery, limit, offset++));
        playlistEntitiesMutableLiveData.postValue(playlistEntities);

        reachedLast = playlistEntities.size() < limit;
    }

    private long generatePlaylistID() {
        return Long.valueOf(user.getUserID() + "" + Calendar.getInstance().getTimeInMillis());
    }

    public void createPlaylist(String playlistName) {
        Calendar calendar = Calendar.getInstance();
        PlaylistEntity playlistEntity = new PlaylistEntity();
        playlistEntity.setPlaylistID(generatePlaylistID());
        playlistEntity.setPlaylistName(playlistName);
        playlistEntity.setUserID(user.getUserID());
        playlistEntity.setUserFirstName(user.getUserFirstName());
        playlistEntity.setSongsCount(0);
        playlistEntity.setPlayDuration(0);
        playlistEntity.setCreatedOn(calendar.getTimeInMillis());
        playlistEntity.setModifiedOn(calendar.getTimeInMillis());

        //insert newly created playlist
        playlistDao.insert(playlistEntity);

        //add to pending uploads
        PendingApiEntity pendingFileUploadEntity = new PendingApiEntity();
        pendingFileUploadEntity.setUrl(Url.PLAYLIST_CREATE);
        pendingFileUploadEntity.setParams(ParserUtil.getInstance().toJson(playlistEntity, PlaylistEntity.class));
        pendingApiDao.insert(pendingFileUploadEntity);

        //start ExecutorService
        PendingApiExecutorService.startService(getApplication());
    }

    public void deletePlaylist(PlaylistEntity playlistModel) {
        playlistMediaDao.deleteAllMediaFromPlaylist(playlistModel.getPlaylistID());
        playlistDao.deletePlaylist(playlistModel.getPlaylistID());

        PendingApiEntity pendingApiEntity = new PendingApiEntity();
        pendingApiEntity.setUrl(Url.PLAYLIST_DELETE);
        pendingApiEntity.setParams(ParserUtil.getInstance().toJson(playlistModel, PlaylistEntity.class));
        pendingApiDao.insert(pendingApiEntity);

        PendingApiExecutorService.startService(getApplication());
    }

    public boolean isExistingPlaylist(String playlistName) {
        List<PlaylistEntity> existingPlaylist = playlistDao.getPlaylist(playlistName);
        return existingPlaylist != null && existingPlaylist.size() != 0;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}