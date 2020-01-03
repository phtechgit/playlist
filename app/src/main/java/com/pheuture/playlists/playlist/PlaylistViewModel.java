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

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private PendingApiDao pendingApiDao;
    private UserEntity user;

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        limit = 20;

        reachedLast = new MutableLiveData<>();
        searchQuery = new MutableLiveData<>();

        showProgress = new MutableLiveData<>();

        pendingApiDao = LocalRepository.getInstance(application).pendingApiDao();
        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
        playlists = playlistDao.getPlaylistsLive();

        getFreshData();
    }

    public void createPlaylist(String playlistName) {
        PlaylistEntity playlistEntity = new PlaylistEntity();
        playlistEntity.setPlaylistID(generatePlaylistID());
        playlistEntity.setPlaylistName(playlistName);
        playlistEntity.setUserID(user.getUserID());
        playlistEntity.setUserFirstName(user.getUserFirstName());
        playlistEntity.setSongsCount(0);
        playlistEntity.setPlayDuration(0);

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

    private long generatePlaylistID() {
        return Long.valueOf(user.getUserID() + "" + Calendar.getInstance().getTimeInMillis());
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
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                params.put(ApiConstant.SEARCH_QUERY, ((searchQuery.getValue()==null)?"":searchQuery.getValue()));
                params.put(ApiConstant.LIMIT, String.valueOf(limit));
                params.put(ApiConstant.USER_ID, String.valueOf(user.getUserID()));
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public void getMoreData() {
        if (reachedLast.getValue()!=null && reachedLast.getValue()){
            return;
        }

        final String url = Url.PLAYLIST_LIST;

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    JSONObject response = new JSONObject(stringResponse);

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
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                params.put(ApiConstant.LIMIT, String.valueOf(limit));
                params.put(ApiConstant.SEARCH_QUERY, ((searchQuery.getValue()==null)?"":searchQuery.getValue()));
                params.put(ApiConstant.USER_ID, String.valueOf(user.getUserID()));
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
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
}