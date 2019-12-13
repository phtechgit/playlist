package com.pheuture.playlists.playlists.detail;

import android.app.Application;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineVideoDao;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineVideoEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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
    private MutableLiveData<List<VideoEntity>> videos;
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;
    private MutableLiveData<Boolean> isPlaying;
    private MutableLiveData<Integer> playerPosition;
    private DataSource.Factory dataSourceFactory;
    private PlaylistDao playlistDao;
    private long playlistID;
    private LiveData<PlaylistEntity> playlistEntity;
    private OfflineVideoDao offlineVideoDao;
    private LiveData<List<OfflineVideoEntity>> offlineVideoEntities;
    private DownloadManager downloadManager;

    public PlaylistDetailViewModel(@NonNull Application application, PlaylistEntity model) {
        super(application);
        this.playlistID = model.getId();

        limit = 20;
        playlistDao = LocalRepository.getInstance(application).playlistDao();
        offlineVideoDao = LocalRepository.getInstance(application).offlineVideoDao();

        playlistEntity = playlistDao.getPlaylist(playlistID);
        offlineVideoEntities = offlineVideoDao.getOfflineVideos();

        reachedLast = new MutableLiveData<>(false);
        searchQuery = new MutableLiveData<>("");
        showProgress = new MutableLiveData<>(true);
        isPlaying = new MutableLiveData<>(false);
        playerPosition = new MutableLiveData<>(RecyclerView.NO_POSITION);
        videos = new MutableLiveData<>();

        downloadManager = (DownloadManager) application.getSystemService(DOWNLOAD_SERVICE);
        dataSourceFactory = new DefaultDataSourceFactory(application, Util.getUserAgent(application, TAG));
        exoPlayer = ExoPlayerFactory.newSimpleInstance(application);
        playerView = new PlayerView(application);
        playerView.setUseController(false);
        playerView.setPlayer(exoPlayer);
    }

    public LiveData<List<VideoEntity>> getVideosLive() {
        return videos;
    }

    public void getFreshData() {
        //reset the last Id
        lastID = 0;

        showProgress.postValue(true);

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

                    List<VideoEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(responseJsonObject.optString(ApiConstant.DATA), VideoEntity[].class));

                    long songsCount = responseJsonObject.optLong("total_songs", 0);
                    long playbackDuration = responseJsonObject.optLong("total_duration", 0);

                    //update playlist entity
                    PlaylistEntity newPlaylistEntity = playlistEntity.getValue();
                    newPlaylistEntity.setPlayDuration(playbackDuration);
                    newPlaylistEntity.setSongsCount(songsCount);
                    playlistDao.insert(newPlaylistEntity);

                    videos.postValue(list);

                    if (list.size()>0){
                        VideoEntity videoEntity = list.get(list.size() - 1);
                        lastID = videoEntity.getId();

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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(stringRequest);
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public void setProgressStatus(boolean b) {
        showProgress.postValue(b);
    }

    public MutableLiveData<Boolean> isPlayling() {
        return isPlaying;
    }

    public void setIsPlaying(boolean b) {
        isPlaying.postValue(b);
    }

    public DataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }

    public MutableLiveData<Integer> getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(int newPlayerPosition) {
        playerPosition.setValue(newPlayerPosition);
    }

    public LiveData<PlaylistEntity> getPlaylistEntity() {
        return playlistEntity;
    }

    public synchronized void addToOfflineMedia(List<VideoEntity> videoEntities) {
        List<OfflineVideoEntity> mediaToDownloadList = new ArrayList<>();

        List<OfflineVideoEntity> offlineVideos = offlineVideoEntities.getValue();
        //filter media which needs to be downloaded
        for (int i = 0; i< videoEntities.size(); i++){
            VideoEntity videoEntity = videoEntities.get(i);

            if (mediaNotAlreadyDownloadedOrInDownloadQueue(videoEntity.getId())){
                OfflineVideoEntity offlineVideoEntity = new OfflineVideoEntity();

                offlineVideoEntity.setId(videoEntity.getId());
                offlineVideoEntity.setVideoName(videoEntity.getVideoName());
                offlineVideoEntity.setVideoDescription(videoEntity.getVideoDescription());
                offlineVideoEntity.setVideoThumbnail(videoEntity.getVideoThumbnail());
                offlineVideoEntity.setVideoUrl(videoEntity.getVideoUrl());
                offlineVideoEntity.setPostDate(videoEntity.getPostDate());
                offlineVideoEntity.setStatus(videoEntity.getStatus());
                offlineVideoEntity.setDownloadedFilePath(getFile(offlineVideoEntity).getPath());
                offlineVideoEntity.setDownloadStatus(DownloadManager.STATUS_PENDING);

                //add media to *download manager*
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(offlineVideoEntity.getVideoUrl()))
                        .setTitle(offlineVideoEntity.getVideoName())// Title of the Download Notification
                        .setDescription(offlineVideoEntity.getId() + ".mp4")// Description of the Download Notification
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                        .setDestinationUri(Uri.fromFile(new File(offlineVideoEntity.getDownloadedFilePath())))// Uri of the destination file
                        .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                        .setAllowedOverRoaming(false);// Set if download is allowed on roaming network

                long downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.

                offlineVideoEntity.setDownloadID(downloadID);
                offlineVideoDao.insert(offlineVideoEntity);
            }
        }
    }

    private File getFile(OfflineVideoEntity offlineVideoEntity) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                offlineVideoEntity.getId() + ".mp4");
        if (!file.exists()){
            try {
                if (!file.createNewFile()){
                    Logger.e(TAG, "failed to create file");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private boolean mediaNotAlreadyDownloadedOrInDownloadQueue(long id) {
        OfflineVideoEntity offlineVideoEntity = offlineVideoDao.getOfflineMedia(id);
        if (offlineVideoEntity==null){
            return true;
        }
        return false;
    }
}