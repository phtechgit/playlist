package com.pheuture.playlists.playlists.detail;

import android.app.Application;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineMediaDao;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineMediaEntity;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
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

    public PlaylistDetailViewModel(@NonNull Application application, PlaylistEntity model) {
        super(application);
        this.playlistID = model.getPlaylistID();

        limit = 20;
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

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    showProgress.postValue(false);
                    Logger.e(url + ApiConstant.RESPONSE, response);

                    JSONObject responseJsonObject = new JSONObject(response);

                    if (!responseJsonObject.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    /*long songsCount = responseJsonObject.optLong("total_songs", 0);
                    long playbackDuration = responseJsonObject.optLong("total_duration", 0);

                    //update playlist entity
                    PlaylistEntity newPlaylistEntity = playlistEntity.getValue();
                    assert newPlaylistEntity != null;
                    newPlaylistEntity.setPlayDuration(playbackDuration);
                    newPlaylistEntity.setSongsCount(songsCount);
                    playlistDao.insert(newPlaylistEntity);*/

                    List<PlaylistMediaEntity> list = Arrays.asList(ParserUtil.getInstance()
                            .fromJson(responseJsonObject.optString(ApiConstant.DATA),
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
                    Logger.e(TAG, e.toString());
                } catch (Exception ex) {
                    Logger.e(TAG, ex.toString());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put(ApiConstant.USER, ApiConstant.DUMMY_USER);
                    params.put(ApiConstant.PLAYLIST_ID, String.valueOf(playlistID));
                    params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                    params.put(ApiConstant.LIMIT, String.valueOf(limit));
                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        stringRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(stringRequest);
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
                    offlineVideoEntity.setVideoName(mediaEntity.getVideoName());
                    offlineVideoEntity.setVideoDescription(mediaEntity.getVideoDescription());
                    offlineVideoEntity.setVideoThumbnail(mediaEntity.getVideoThumbnail());
                    offlineVideoEntity.setVideoUrl(mediaEntity.getVideoUrl());
                    offlineVideoEntity.setPostDate(mediaEntity.getPostDate());
                    offlineVideoEntity.setStatus(mediaEntity.getStatus());
                    offlineVideoEntity.setDownloadedFilePath(getFile(offlineVideoEntity).getPath());
                    offlineVideoEntity.setDownloadStatus(DownloadManager.STATUS_PENDING);

                    //add media to *download manager*
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(offlineVideoEntity.getVideoUrl()))
                            .setTitle(offlineVideoEntity.getVideoName())// Title of the Download Notification
                            .setDescription(offlineVideoEntity.getVideoDescription())// Description of the Download Notification
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
                offlineVideoEntity.getVideoName());

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

    public void removeMediaFromPlaylist(PlaylistMediaEntity model) {
        //save changes in persistent storage finally after API response
        playlistMediaDao.deleteMediaFromPlaylist(playlistID, model.getMediaID());

        PlaylistEntity newPlaylistEntity = playlistEntity.getValue();
        newPlaylistEntity.setSongsCount(newPlaylistEntity.getSongsCount() - 1);
        newPlaylistEntity.setPlayDuration(newPlaylistEntity.getPlayDuration() - model.getPlayDuration());
        playlistDao.insert(newPlaylistEntity);

        final String url = Url.PLAYLIST_MEDIA_REMOVE;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    showProgress.postValue(false);

                    Logger.e(url + ApiConstant.RESPONSE, response);

                    JSONObject responseJsonObject = new JSONObject(response);

                    if (!responseJsonObject.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
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
                    Logger.e(TAG, e.toString());
                } catch (Exception ex) {
                    Logger.e(TAG, ex.toString());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put(ApiConstant.PLAYLIST_ID, String.valueOf(playlistID));
                    params.put(ApiConstant.MEDIA_ID, String.valueOf(model.getMediaID()));
                    params.put(ApiConstant.USER, ApiConstant.DUMMY_USER);
                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        stringRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(stringRequest);
    }
}