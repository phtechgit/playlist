package com.pheuture.playlists.playlist;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.pheuture.playlists.auth.user_detail.UserModel;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadDao;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadEntity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class PlaylistViewModel extends AndroidViewModel {
    private static final String TAG = PlaylistViewModel.class.getSimpleName();
    private long lastID;
    private long limit;
    private PlaylistDao playlistDao;
    private MutableLiveData<String> searchQuery;
    private MutableLiveData<Boolean> reachedLast;
    private MutableLiveData<Boolean> showProgress;
    private LiveData<List<PlaylistEntity>> playlists;
    private PlaylistMediaDao playlistMediaDao;
    private PendingUploadDao pendingUploadDao;
    private UserModel user;

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserModel.class);

        limit = 20;

        reachedLast = new MutableLiveData<>();
        searchQuery = new MutableLiveData<>();

        showProgress = new MutableLiveData<>();

        pendingUploadDao = LocalRepository.getInstance(application).pendingUploadDao();
        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
        playlists = playlistDao.getPlaylistsLive();

        getFreshData();
    }

    public void createPlaylist(String playlistName) {
        PlaylistEntity playlistEntity = new PlaylistEntity();
        playlistEntity.setPlaylistID(generatePlaylistID());
        playlistEntity.setPlaylistName(playlistName);
        playlistEntity.setCreatedByUserID(user.getUserId());
        playlistEntity.setCreatedByUserName(user.getUserName());
        playlistEntity.setSongsCount(0);
        playlistEntity.setPlayDuration(0);

        //insert newly created playlist
        playlistDao.insert(playlistEntity);

        //add to pending uploads
        PendingUploadEntity pendingUploadEntity = new PendingUploadEntity();
        pendingUploadEntity.setUrl(Url.PLAYLIST_CREATE);
        pendingUploadEntity.setParams(ParserUtil.getInstance().toJson(playlistEntity, PlaylistEntity.class));
        pendingUploadDao.insert(pendingUploadEntity);

        //start ExecutorService
        PendingApiExecutorService.startService(getApplication());
    }

    private long generatePlaylistID() {
        return user.getUserId() + Calendar.getInstance().getTimeInMillis();
    }

    public LiveData<List<PlaylistEntity>> getPlaylistEntities() {
        return playlists;
    }

    public void setSearchQuery(String query) {
        searchQuery.postValue(query);
    }

    public MutableLiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void getFreshData() {
        //reset the last Id
        lastID = 0;

        final String url = Url.PLAYLIST_LIST;

        JSONObject params = new JSONObject();
        try {
            params.put(ApiConstant.LAST_ID, lastID);
            params.put(ApiConstant.SEARCH_QUERY, ((searchQuery.getValue()==null)?"":searchQuery.getValue()));
            params.put(ApiConstant.LIMIT, limit);
            params.put(ApiConstant.USER_ID, user.getUserId());
        } catch (JSONException e) {
            Logger.e(TAG, e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    showProgress.postValue(false);

                    Logger.e(url + ApiConstant.RESPONSE, response.toString());

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<PlaylistEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(response.optString(ApiConstant.DATA), PlaylistEntity[].class));
                    playlistDao.deleteAll();
                    playlistDao.insertAll(list);

                    if (list.size()>0){
                        PlaylistEntity videoEntity = list.get(list.size() - 1);
                        lastID = videoEntity.getPlaylistID();

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
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                try {
                    showProgress.postValue(false);
                    Logger.e(url, e.toString());
                } catch (Exception ex) {
                    Logger.e(TAG, ex.toString());
                }
            }
        });
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public void getMoreData() {
        if (reachedLast.getValue()!=null && reachedLast.getValue()){
            return;
        }

        final String url = Url.PLAYLIST_LIST;

        JSONObject params = new JSONObject();
        try {
            params.put(ApiConstant.LAST_ID, lastID);
            params.put(ApiConstant.LIMIT, limit);
            params.put(ApiConstant.SEARCH_QUERY, ((searchQuery.getValue()==null)?"":searchQuery.getValue()));
            params.put(ApiConstant.USER_ID, user.getUserId());
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, response.toString());

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<PlaylistEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(response.optString(ApiConstant.DATA), PlaylistEntity[].class));

                    playlistDao.insertAll(list);

                    if (list.size()>0){
                        PlaylistEntity videoEntity = list.get(list.size() - 1);
                        lastID = videoEntity.getPlaylistID();

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
                Logger.e(TAG, e.toString());
            }
        });
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public void deletePlaylist(PlaylistEntity playlistModel) {
        playlistMediaDao.deleteAllMediaFromPlaylist(playlistModel.getPlaylistID());
        playlistDao.deletePlaylist(playlistModel.getPlaylistID());

        PendingUploadEntity pendingUploadEntity = new PendingUploadEntity();
        pendingUploadEntity.setUrl(Url.PLAYLIST_DELETE);
        pendingUploadEntity.setParams(ParserUtil.getInstance().toJson(playlistModel, PlaylistEntity.class));
        pendingUploadDao.insert(pendingUploadEntity);

        PendingApiExecutorService.startService(getApplication());
    }

    public boolean isExistingPlaylist(String playlistName) {
        List<PlaylistEntity> existingPlaylist = playlistDao.getPlaylist(playlistName);
        return existingPlaylist != null && existingPlaylist.size() != 0;
    }
}