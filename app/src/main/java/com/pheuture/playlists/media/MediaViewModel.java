package com.pheuture.playlists.media;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
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
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;
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

    public MediaViewModel(@NonNull Application application, PlaylistEntity playlistEntity) {
        super(application);
        this.playlistEntity = playlistEntity;

        limit = 20;
        reachedLast = new MutableLiveData<>(false);
        searchQuery = new MutableLiveData<>("");

        showProgress = new MutableLiveData<>(false);
        updateParent = new MutableLiveData<>(false);

        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
        mediaEntitiesLive = new MutableLiveData<>();
    }

    public void getFreshData() {
        //reset the last Id
        lastID = 0;

        final String url = Url.MEDIA_TRENDING;

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

                    List<MediaEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(responseJsonObject.optString(ApiConstant.DATA), MediaEntity[].class));
                    /*videoDao.deleteAll();
                    videoDao.insertAll(list);*/
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
                    Logger.e(TAG, e.toString());
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
                params.put(ApiConstant.SEARCH_QUERY, searchQuery.getValue());
                params.put(ApiConstant.LIMIT, String.valueOf(limit));
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        stringRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(stringRequest);
    }

    public MutableLiveData<List<MediaEntity>> getVideosLive() {
        return mediaEntitiesLive;
    }

    public void getMoreData() {
        assert reachedLast.getValue()!=null;
        if (reachedLast.getValue()){
            return;
        }

        final String url = Url.MEDIA_TRENDING;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, response);

                    JSONObject responseJsonObject = new JSONObject(response);

                    if (!responseJsonObject.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<MediaEntity> newDataList = Arrays.asList(ParserUtil.getInstance().fromJson(responseJsonObject.optString(ApiConstant.DATA), MediaEntity[].class));

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
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                    params.put(ApiConstant.LIMIT, String.valueOf(limit));
                    params.put(ApiConstant.SEARCH_QUERY, searchQuery.getValue());
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

    public void setSearchQuery(String query) {
        searchQuery.postValue(query);
    }

    public MutableLiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void addMediaToPlaylist(final int adapterPosition, final PlaylistMediaEntity playlistMediaEntity) {
        //change non persistent data for removing delay in data update from API
        List<MediaEntity> mediaEntities = new ArrayList<>(mediaEntitiesLive.getValue());
        mediaEntities.remove(adapterPosition);
        mediaEntitiesLive.setValue(mediaEntities);

        //save changes in persistent storage
        playlistMediaEntity.setPlaylistID(playlistEntity.getPlaylistID());
        playlistMediaDao.insert(playlistMediaEntity);

        playlistEntity.setSongsCount(playlistEntity.getSongsCount() + 1);
        playlistEntity.setPlayDuration(playlistEntity.getPlayDuration() + playlistMediaEntity.getPlayDuration());
        playlistDao.insert(playlistEntity);

        final String url = Url.PLAYLIST_MEDIA_ADD;

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

                    //data updated successfully on server

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
                    params.put(ApiConstant.PLAYLIST_ID, String.valueOf(playlistEntity.getPlaylistID()));
                    params.put(ApiConstant.MEDIA_ID, String.valueOf(playlistMediaEntity.getMediaID()));
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

    public MutableLiveData<Boolean> getNeedToUpdateParent() {
        return updateParent;
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }
}