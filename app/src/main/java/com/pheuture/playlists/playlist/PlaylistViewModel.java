package com.pheuture.playlists.playlist;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.R;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.base.service.PendingApiLocalDao;
import com.pheuture.playlists.base.service.PendingApiEntity;
import com.pheuture.playlists.auth.UserEntity;
import com.pheuture.playlists.base.datasource.local.LocalRepository;
import com.pheuture.playlists.playist_detail.PlaylistMediaLocalDao;
import com.pheuture.playlists.base.service.PendingApiExecutorService;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.ParserUtil;
import com.pheuture.playlists.base.utils.SharedPrefsUtils;
import com.pheuture.playlists.base.constants.Url;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PlaylistViewModel extends BaseAndroidViewModel {
    private static final String TAG = PlaylistViewModel.class.getSimpleName();
    private int offset;
    private int limit = 20;
    private String searchQuery = "";
    private boolean reachedLast;
    private UserEntity user;
    private PlaylistLocalDao playlistLocalDao;
    private PendingApiLocalDao pendingApiLocalDao;
    private PlaylistMediaLocalDao playlistMediaLocalDao;
    private MutableLiveData<List<PlaylistEntity>> playlistEntitiesMutableLiveData;

    public PlaylistViewModel(@NonNull Application application) {
        super(application);

        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        pendingApiLocalDao = LocalRepository.getInstance(application).pendingApiLocalDao();
        playlistLocalDao = LocalRepository.getInstance(application).playlistLocalDao();
        playlistMediaLocalDao = LocalRepository.getInstance(application).playlistMediaLocalDao();
        playlistEntitiesMutableLiveData = new MutableLiveData<>(new ArrayList<>());

        getFreshData();
    }

    private void getFreshData() {
        offset = RecyclerView.NO_POSITION;
        reachedLast = false;
        List<PlaylistEntity> playlistEntities;
        if (searchQuery.length() == 0){
            playlistEntities = playlistLocalDao.getPlaylistEntities(limit, ++offset);
        } else {
            playlistEntities = playlistLocalDao.getPlaylistEntities("%" + searchQuery + "%", limit, ++offset);
        }
        reachedLast = playlistEntities.size() < limit;

        //add create button at the
        addCreateButton(playlistEntities);

        playlistEntitiesMutableLiveData.postValue(playlistEntities);
    }

    private void addCreateButton(List<PlaylistEntity> playlistEntities) {
        if (searchQuery.length() == 0){
            PlaylistEntity addNewPlaylistModel = new PlaylistEntity();
            addNewPlaylistModel.setPlaylistID(RecyclerView.NO_ID);
            addNewPlaylistModel.setPlaylistName(getApplication().getResources().getString(R.string.create_playlist));
            playlistEntities.add(0, addNewPlaylistModel);
        }
    }

    public LiveData<List<PlaylistEntity>> getPlaylistEntitiesMutableLiveData() {
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
        List<PlaylistEntity> oldPlaylistEntities = playlistEntitiesMutableLiveData.getValue();
        if (oldPlaylistEntities == null){
            oldPlaylistEntities = new ArrayList<>();
        }
        List<PlaylistEntity> newPlaylistEntities;
        if (searchQuery.length() == 0){
            newPlaylistEntities = playlistLocalDao.getPlaylistEntities(limit, ++offset);
        } else {
            newPlaylistEntities = playlistLocalDao.getPlaylistEntities("%" + searchQuery + "%", limit, ++offset);
        }
        reachedLast = newPlaylistEntities.size() < limit;

        oldPlaylistEntities.addAll(newPlaylistEntities);
        playlistEntitiesMutableLiveData.postValue(oldPlaylistEntities);
    }

    private long generatePlaylistID() {
        return Long.valueOf(user.getUserID() + "" + Calendar.getInstance().getTimeInMillis());
    }

    public long createPlaylist(String playlistName) {
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
        playlistLocalDao.insert(playlistEntity);
        List<PlaylistEntity> playlistEntities = playlistEntitiesMutableLiveData.getValue();
        if (playlistEntities == null || playlistEntities.size() == 0) {
            playlistEntities = new ArrayList<>();
            playlistEntities.add(0, playlistEntity);

            addCreateButton(playlistEntities);
        } else {
            playlistEntities.add(1, playlistEntity);
        }
        playlistEntitiesMutableLiveData.postValue(playlistEntities);

        //add to pending uploads
        PendingApiEntity pendingFileUploadEntity = new PendingApiEntity();
        pendingFileUploadEntity.setUrl(Url.PLAYLIST_CREATE);
        pendingFileUploadEntity.setParams(ParserUtil.getInstance().toJson(playlistEntity, PlaylistEntity.class));
        pendingApiLocalDao.insert(pendingFileUploadEntity);

        //start ExecutorService
        PendingApiExecutorService.startService(getApplication());
        return playlistEntity.getPlaylistID();
    }

    public void deletePlaylist(int position, PlaylistEntity playlistEntity) {
        List<PlaylistEntity> playlistEntities = playlistEntitiesMutableLiveData.getValue();
        if (playlistEntities == null || playlistEntities.size() == 0){
            playlistEntities = new ArrayList<>();
        }
        playlistEntities.remove(position);

        /*//remove create button
        removeCreateButton(playlistEntities);*/

        playlistEntitiesMutableLiveData.postValue(playlistEntities);

        playlistMediaLocalDao.deleteAllMediaFromPlaylist(playlistEntity.getPlaylistID());
        playlistLocalDao.deletePlaylist(playlistEntity.getPlaylistID());

        PendingApiEntity pendingApiEntity = new PendingApiEntity();
        pendingApiEntity.setUrl(Url.PLAYLIST_DELETE);
        pendingApiEntity.setParams(ParserUtil.getInstance().toJson(playlistEntity, PlaylistEntity.class));
        pendingApiLocalDao.insert(pendingApiEntity);

        PendingApiExecutorService.startService(getApplication());

        showSnackBar(playlistEntity.getPlaylistName() + " deleted.", Snackbar.LENGTH_SHORT);
    }

    private void removeCreateButton(List<PlaylistEntity> playlistEntities) {
        if (searchQuery.length()==0 && playlistEntities.size()==1){
            playlistEntities.remove(0);
        }
    }

    public boolean isExistingPlaylist(String playlistName) {
        List<PlaylistEntity> existingPlaylist = playlistLocalDao.getPlaylistEntities(playlistName);
        return existingPlaylist != null && existingPlaylist.size() != 0;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}