package com.pheuture.playlists.playlist.detail;

import android.app.Application;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.pheuture.playlists.datasource.local.pending_api.PendingApiDao;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.media_handler.offline.OfflineMediaDao;
import com.pheuture.playlists.datasource.local.media_handler.offline.OfflineMediaEntity;
import com.pheuture.playlists.service.PendingApiExecutorService;
import com.pheuture.playlists.interfaces.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.DOWNLOAD_SERVICE;

public class PlaylistDetailViewModel extends AndroidViewModel {
    private static final String TAG = PlaylistDetailViewModel.class.getSimpleName();
    private long playlistID;
    private UserEntity user;
    private int offset;
    private int limit = 20;
    private String searchQuery = "";
    private boolean reachedLast;
    private LiveData<PlaylistEntity> playlistEntity;
    private PlaylistDao playlistDao;
    private PlaylistMediaDao playlistMediaDao;
    private OfflineMediaDao offlineMediaDao;
    private DownloadManager downloadManager;
    private PendingApiDao pendingApiDao;
    private MutableLiveData<List<PlaylistMediaEntity>> playlistMediaEntitiesMutableLiveData;

    public PlaylistDetailViewModel(@NonNull Application application, long playlistID) {
        super(application);
        this.playlistID = playlistID;

        downloadManager = (DownloadManager) application.getSystemService(DOWNLOAD_SERVICE);
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        pendingApiDao = LocalRepository.getInstance(application).pendingApiDao();
        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
        offlineMediaDao = LocalRepository.getInstance(application).offlineMediaDao();

        playlistMediaEntitiesMutableLiveData = new MutableLiveData<>(new ArrayList<>());

        playlistEntity = playlistDao.getPlaylistLive(playlistID);

        getFreshData();
    }

    public LiveData<PlaylistEntity> getPlaylistEntity() {
        return playlistEntity;
    }

    public void getFreshData() {
        offset = RecyclerView.NO_POSITION;
        reachedLast = false;
        List<PlaylistMediaEntity> playlistMediaEntities;
        if (searchQuery.length() == 0){
            playlistMediaEntities = playlistMediaDao.getPlaylistMediaEntities(playlistID, limit, ++offset);
        } else {
            playlistMediaEntities = playlistMediaDao.getPlaylistMediaEntities(playlistID, "%" + searchQuery + "%", limit, ++offset);
        }
        reachedLast = playlistMediaEntities.size() < limit;
        playlistMediaEntitiesMutableLiveData.postValue(playlistMediaEntities);
    }

    public void setSearchQuery(String query) {
        searchQuery = query;
        getFreshData();
    }

    public MutableLiveData<List<PlaylistMediaEntity>> getPlaylistMediaEntitiesMutableLiveData() {
        return playlistMediaEntitiesMutableLiveData;
    }

    public void getMoreData() {
        if (reachedLast){
            return;
        }
        List<PlaylistMediaEntity> oldPlaylistMediaEntities = playlistMediaEntitiesMutableLiveData.getValue();
        if (oldPlaylistMediaEntities == null){
            oldPlaylistMediaEntities = new ArrayList<>();
        }
        List<PlaylistMediaEntity> newPlaylistMediaEntities;
        if (searchQuery.length() == 0){
            newPlaylistMediaEntities = playlistMediaDao.getPlaylistMediaEntities(playlistID, limit, ++offset);
        } else {
            newPlaylistMediaEntities = playlistMediaDao.getPlaylistMediaEntities(playlistID, "%" + searchQuery + "%", limit, ++offset);
        }
        reachedLast = newPlaylistMediaEntities.size() < limit;

        oldPlaylistMediaEntities.addAll(newPlaylistMediaEntities);
        playlistMediaEntitiesMutableLiveData.postValue(oldPlaylistMediaEntities);
    }

    public synchronized void addToOfflineMedia(List<PlaylistMediaEntity> videoEntities) {
        try {
            for (int i = 0; i< videoEntities.size(); i++){
                PlaylistMediaEntity mediaEntity = videoEntities.get(i);

                if (mediaNotAlreadyDownloadedOrInDownloadQueue(mediaEntity.getMediaID())){
                    OfflineMediaEntity offlineVideoEntity = new OfflineMediaEntity();

                    offlineVideoEntity.setMediaID(mediaEntity.getMediaID());
                    offlineVideoEntity.setMediaName(mediaEntity.getMediaName());
                    offlineVideoEntity.setMediaTitle(mediaEntity.getMediaTitle());
                    offlineVideoEntity.setMediaDescription(mediaEntity.getMediaDescription());
                    offlineVideoEntity.setMediaThumbnail(mediaEntity.getMediaThumbnail());
                    offlineVideoEntity.setMediaUrl(mediaEntity.getMediaUrl());
                    offlineVideoEntity.setPostDate(mediaEntity.getPostDate());
                    offlineVideoEntity.setStatus(mediaEntity.getStatus());
                    offlineVideoEntity.setDownloadedFilePath(getFile(offlineVideoEntity).getPath());
                    offlineVideoEntity.setDownloadStatus(DownloadManager.STATUS_PENDING);

                    boolean downloadOnCellularStatus = SharedPrefsUtils.getBooleanPreference(getApplication(),
                            Constants.DOWNLOAD_USING_CELLULAR, false);
                    boolean downloadWhileRoamingStatus = SharedPrefsUtils.getBooleanPreference(getApplication(),
                            Constants.DOWNLOAD_WHILE_ROAMING, false);

                    //add media to *download manager*
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(offlineVideoEntity.getMediaUrl()))
                            .setTitle(offlineVideoEntity.getMediaName())// Title of the Download Notification
                            .setDescription(offlineVideoEntity.getMediaDescription())// Description of the Download Notification
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                            .setDestinationUri(Uri.fromFile(new File(offlineVideoEntity.getDownloadedFilePath())))// Uri of the destination file
                            .setAllowedOverMetered(downloadOnCellularStatus)// Set if download is allowed on Mobile network
                            .setAllowedOverRoaming(downloadWhileRoamingStatus);// Set if download is allowed on roaming network

                    long downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.

                    offlineVideoEntity.setDownloadID(downloadID);
                    offlineMediaDao.insert(offlineVideoEntity);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
    }

    private File getFile(OfflineMediaEntity offlineVideoEntity) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                offlineVideoEntity.getMediaName());

        if (!file.exists()){
            try {
                if (!file.createNewFile()){
                    Logger.e(TAG, "failed to create file");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                file.delete();
                file.createNewFile();
            } catch (Exception e) {
                Logger.e(TAG, e.toString());
            }
        }
        return file;
    }

    private boolean mediaNotAlreadyDownloadedOrInDownloadQueue(long id) {
        OfflineMediaEntity offlineVideoEntity = offlineMediaDao.getOfflineMedia(id);
        return offlineVideoEntity == null;
    }

    public void removeMediaFromPlaylist(int position, PlaylistMediaEntity playlistMediaEntity) {
        List<PlaylistMediaEntity> playlistMediaEntities = playlistMediaEntitiesMutableLiveData.getValue();
        playlistMediaEntities.remove(position);
        playlistMediaEntitiesMutableLiveData.postValue(playlistMediaEntities);

        //update playlist media
        playlistMediaDao.deleteMediaFromPlaylist(playlistID, playlistMediaEntity.getMediaID());

        Calendar calendar = Calendar.getInstance();
        long date = calendar.getTimeInMillis();

        //update playlist
        PlaylistEntity newPlaylistEntity = playlistEntity.getValue();
        newPlaylistEntity.setSongsCount(newPlaylistEntity.getSongsCount() - 1);
        newPlaylistEntity.setPlayDuration(newPlaylistEntity.getPlayDuration() - playlistMediaEntity.getPlayDuration());
        newPlaylistEntity.setModifiedOn(date);
        playlistDao.insert(newPlaylistEntity);

        //add to pending uploads
        JSONObject params = new JSONObject();
        try {
            params.put(ApiConstant.PLAYLIST_ID, playlistMediaEntity.getPlaylistID());
            params.put(ApiConstant.MEDIA_ID, playlistMediaEntity.getMediaID());
            params.put(ApiConstant.USER_ID, user.getUserID());
            params.put(ApiConstant.MODIFIED_ON, date);
            params.put(ApiConstant.CREATED_ON, date);
        } catch (JSONException e) {
            Logger.e(TAG, e.toString());
        }

        PendingApiEntity pendingFileUploadEntity = new PendingApiEntity();
        pendingFileUploadEntity.setUrl(Url.PLAYLIST_MEDIA_REMOVE);
        pendingFileUploadEntity.setParams(params.toString());
        pendingApiDao.insert(pendingFileUploadEntity);

        //start ExecutorService
        PendingApiExecutorService.startService(getApplication());
    }

    public void deletePlaylist() {
        playlistMediaDao.deleteAllMediaFromPlaylist(playlistID);
        playlistDao.deletePlaylist(playlistID);

        PendingApiEntity pendingApiEntity = new PendingApiEntity();
        pendingApiEntity.setUrl(Url.PLAYLIST_DELETE);
        pendingApiEntity.setParams(ParserUtil.getInstance().toJson(playlistEntity.getValue(), PlaylistEntity.class));
        pendingApiDao.insert(pendingApiEntity);

        PendingApiExecutorService.startService(getApplication());
    }
}