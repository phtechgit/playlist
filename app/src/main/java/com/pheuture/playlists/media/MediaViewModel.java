package com.pheuture.playlists.media;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiDao;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiEntity;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaViewModel extends AndroidViewModel {
    private static final String TAG = MediaViewModel.class.getSimpleName();
    private MutableLiveData<Boolean> showProgress;
    private MutableLiveData<List<MediaEntity>> mediaEntitiesLive;
    private PlaylistMediaDao playlistMediaDao;
    private PlaylistDao playlistDao;
    private long lastID;
    private long limit;
    private MutableLiveData<String> searchQuery;
    private MutableLiveData<Boolean> reachedLast;
    private MutableLiveData<Boolean> updateParent;
    private PlaylistEntity playlistEntity;
    private UserEntity user;
    private PendingApiDao pendingApiDao;

    public MediaViewModel(@NonNull Application application, PlaylistEntity playlistEntity) {
        super(application);
        this.playlistEntity = playlistEntity;
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        limit = 20;
        reachedLast = new MutableLiveData<>(false);
        searchQuery = new MutableLiveData<>("");

        showProgress = new MutableLiveData<>(false);
        updateParent = new MutableLiveData<>(false);

        pendingApiDao = LocalRepository.getInstance(application).pendingApiDao();
        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
        mediaEntitiesLive = new MutableLiveData<>();
    }

    public void getFreshData() {
        //reset the last Id
        lastID = 0;

        final String url = Url.BASE_URL + Url.MEDIA_TRENDING;

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

                    List<MediaEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(response.optString(ApiConstant.DATA), MediaEntity[].class));
                    mediaEntitiesLive.postValue(list);

                    if (list.size()>0){
                        MediaEntity mediaEntity = list.get(list.size() - 1);
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
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.PLAYLIST_ID, String.valueOf(playlistEntity.getPlaylistID()));
                params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                params.put(ApiConstant.SEARCH_QUERY, ((searchQuery.getValue()==null)?"":searchQuery.getValue()));
                params.put(ApiConstant.LIMIT, String.valueOf(limit));
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public MutableLiveData<List<MediaEntity>> getVideosLive() {
        return mediaEntitiesLive;
    }

    public void getMoreData() {
        if (reachedLast.getValue()!=null && reachedLast.getValue()){
            return;
        }

        final String url = Url.BASE_URL + Url.MEDIA_TRENDING;

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    JSONObject response = new JSONObject(stringResponse);

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<MediaEntity> newDataList = Arrays.asList(ParserUtil.getInstance().fromJson(response.optString(ApiConstant.DATA), MediaEntity[].class));

                    List<MediaEntity> oldList = mediaEntitiesLive.getValue();
                    oldList.addAll(newDataList);

                    mediaEntitiesLive.postValue(oldList);

                    if (newDataList.size()>0){
                        MediaEntity mediaEntity = newDataList.get(newDataList.size() - 1);
                        lastID = mediaEntity.getMediaID();

                        if (newDataList.size()<limit) {
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
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public void setSearchQuery(String query) {
        searchQuery.postValue(query);
    }

    public MutableLiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void addMediaToPlaylist(final int adapterPosition, final PlaylistMediaEntity playlistMediaEntity) {
        //update playlist media
        List<MediaEntity> mediaEntities = new ArrayList<>(mediaEntitiesLive.getValue());
        mediaEntities.remove(adapterPosition);
        mediaEntitiesLive.setValue(mediaEntities);

        playlistMediaEntity.setPlaylistID(playlistEntity.getPlaylistID());
        playlistMediaDao.insert(playlistMediaEntity);

        //update playlist
        playlistEntity.setSongsCount(playlistEntity.getSongsCount() + 1);
        playlistEntity.setPlayDuration(playlistEntity.getPlayDuration() + playlistMediaEntity.getPlayDuration());
        playlistDao.insert(playlistEntity);

        //add to pending uploads
        JSONObject params = new JSONObject();
        try {
            params.put(ApiConstant.PLAYLIST_ID, playlistMediaEntity.getPlaylistID());
            params.put(ApiConstant.MEDIA_ID, playlistMediaEntity.getMediaID());
            params.put(ApiConstant.USER_ID, user.getUserID());
        } catch (JSONException e) {
            Logger.e(TAG, e.toString());
        }
        PendingApiEntity pendingFileUploadEntity = new PendingApiEntity();
        pendingFileUploadEntity.setUrl(Url.PLAYLIST_MEDIA_ADD);
        pendingFileUploadEntity.setParams(params.toString());
        pendingApiDao.insert(pendingFileUploadEntity);

        //start ExecutorService
        PendingApiExecutorService.startService(getApplication());
    }

    public MutableLiveData<Boolean> getNeedToUpdateParent() {
        return updateParent;
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }
}