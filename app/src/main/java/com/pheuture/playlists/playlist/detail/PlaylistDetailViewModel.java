package com.pheuture.playlists.playlist.detail;

import android.app.Application;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadDao;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineMediaDao;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineMediaEntity;
import com.pheuture.playlists.service.PendingApiExecutorService;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.DOWNLOAD_SERVICE;

public class PlaylistDetailViewModel extends AndroidViewModel {
    private static final String TAG = PlaylistDetailViewModel.class.getSimpleName();
    private long lastID;
    private long limit;
    private MutableLiveData<String> searchQuery;
    private MutableLiveData<Boolean> reachedLast;
    private MutableLiveData<Boolean> showProgress;
    private LiveData<List<PlaylistMediaEntity>> playlistMediaLive;
    private long playlistID;
    private LiveData<PlaylistEntity> playlistEntity;
    private PlaylistDao playlistDao;
    private PlaylistMediaDao playlistMediaDao;
    private OfflineMediaDao offlineMediaDao;
    private DownloadManager downloadManager;
    private UserEntity user;
    private PendingUploadDao pendingUploadDao;

    public PlaylistDetailViewModel(@NonNull Application application, long playlistID) {
        super(application);
        this.playlistID = playlistID;

        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        limit = 20;
        pendingUploadDao = LocalRepository.getInstance(application).pendingUploadDao();
        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
        offlineMediaDao = LocalRepository.getInstance(application).offlineVideoDao();

        playlistEntity = playlistDao.getPlaylistLive(playlistID);

        reachedLast = new MutableLiveData<>();
        searchQuery = new MutableLiveData<>();
        showProgress = new MutableLiveData<>();
        playlistMediaLive = playlistMediaDao.getPlaylistMediaLive(playlistID);

        downloadManager = (DownloadManager) application.getSystemService(DOWNLOAD_SERVICE);

        getFreshData();
    }

    public LiveData<List<PlaylistMediaEntity>> getPlaylistMediaLive() {
        return playlistMediaLive;
    }

    public void getFreshData() {
        //reset the last Id
        lastID = 0;

        final String url = Url.PLAYLIST_VIDEOS;

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    showProgress.postValue(false);
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);
                    JSONObject response = new JSONObject(stringResponse);

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<PlaylistMediaEntity> list = Arrays.asList(ParserUtil.getInstance()
                            .fromJson(response.optString(ApiConstant.DATA),
                                    PlaylistMediaEntity[].class));

                    updateDbPlaylistMedia(list);

                    if (list.size()>0){
                        PlaylistMediaEntity mediaEntity = list.get(list.size() - 1);
                        lastID = mediaEntity.getMediaID();

                        if (list.size()<limit) {
                            reachedLast.postValue(true);
                        } else {
                            reachedLast.postValue(false);
                        }
                    } else {
                        lastID = 0;
                        reachedLast.postValue(true);
                    }
                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                try {
                    showProgress.postValue(false);
                    Logger.e(url, e.toString());
                } catch (Exception ex) {
                    Logger.e(TAG, ex.toString());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.USER_ID, String.valueOf(user.getUserID()));
                params.put(ApiConstant.PLAYLIST_ID, String.valueOf(playlistID));
                params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                params.put(ApiConstant.LIMIT, String.valueOf(limit));
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    private void updateDbPlaylistMedia(List<PlaylistMediaEntity> list) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<list.size(); i++){
                    PlaylistMediaEntity model = list.get(i);
                    if (playlistMediaDao.getPlaylistMedia(model.getPlaylistID(), model.getMediaID()) == null) {
                        playlistMediaDao.insert(model);
                    }
                }
            }
        });
        thread.start();
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public void setProgressStatus(boolean b) {
        showProgress.postValue(b);
    }

    public LiveData<PlaylistEntity> getPlaylistEntity() {
        return playlistEntity;
    }

    public synchronized void addToOfflineMedia(List<PlaylistMediaEntity> videoEntities) {
        try {
            for (int i = 0; i< videoEntities.size(); i++){
                PlaylistMediaEntity mediaEntity = videoEntities.get(i);

                if (mediaNotAlreadyDownloadedOrInDownloadQueue(mediaEntity.getMediaID())){
                    OfflineMediaEntity offlineVideoEntity = new OfflineMediaEntity();

                    offlineVideoEntity.setMediaID(mediaEntity.getMediaID());
                    offlineVideoEntity.setMediaName(mediaEntity.getMediaName());
                    offlineVideoEntity.setMediaDescription(mediaEntity.getMediaDescription());
                    offlineVideoEntity.setMediaThumbnail(mediaEntity.getMediaThumbnail());
                    offlineVideoEntity.setMediaUrl(mediaEntity.getMediaUrl());
                    offlineVideoEntity.setPostDate(mediaEntity.getPostDate());
                    offlineVideoEntity.setStatus(mediaEntity.getStatus());
                    offlineVideoEntity.setDownloadedFilePath(getFile(offlineVideoEntity).getPath());
                    offlineVideoEntity.setDownloadStatus(DownloadManager.STATUS_PENDING);

                    //add media to *download manager*
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(offlineVideoEntity.getMediaUrl()))
                            .setTitle(offlineVideoEntity.getMediaName())// Title of the Download Notification
                            .setDescription(offlineVideoEntity.getMediaDescription())// Description of the Download Notification
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                            .setDestinationUri(Uri.fromFile(new File(offlineVideoEntity.getDownloadedFilePath())))// Uri of the destination file
                            .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                            .setAllowedOverRoaming(true);// Set if download is allowed on roaming network

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

    public void removeMediaFromPlaylist(PlaylistMediaEntity playlistMediaEntity) {
        //update playlist media
        playlistMediaDao.deleteMediaFromPlaylist(playlistID, playlistMediaEntity.getMediaID());

        //update playlist
        PlaylistEntity newPlaylistEntity = playlistEntity.getValue();
        newPlaylistEntity.setSongsCount(newPlaylistEntity.getSongsCount() - 1);
        newPlaylistEntity.setPlayDuration(newPlaylistEntity.getPlayDuration() - playlistMediaEntity.getPlayDuration());
        playlistDao.insert(newPlaylistEntity);

        //add to pending uploads
        //add to pending uploads
        JSONObject params = new JSONObject();
        try {
            params.put(ApiConstant.PLAYLIST_ID, playlistMediaEntity.getPlaylistID());
            params.put(ApiConstant.MEDIA_ID, playlistMediaEntity.getMediaID());
            params.put(ApiConstant.USER_ID, user.getUserID());
        } catch (JSONException e) {
            Logger.e(TAG, e.toString());
        }

        PendingUploadEntity pendingUploadEntity = new PendingUploadEntity();
        pendingUploadEntity.setUrl(Url.PLAYLIST_MEDIA_REMOVE);
        pendingUploadEntity.setParams(params.toString());
        pendingUploadDao.insert(pendingUploadEntity);

        //start ExecutorService
        PendingApiExecutorService.startService(getApplication());
    }
}