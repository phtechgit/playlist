package com.pheuture.playlists.media;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.base.service.PendingApiLocalDao;
import com.pheuture.playlists.base.service.PendingApiEntity;
import com.pheuture.playlists.auth.UserEntity;
import com.pheuture.playlists.base.datasource.local.LocalRepository;
import com.pheuture.playlists.playlist.PlaylistLocalDao;
import com.pheuture.playlists.playlist.PlaylistEntity;
import com.pheuture.playlists.playist_detail.PlaylistMediaLocalDao;
import com.pheuture.playlists.playist_detail.PlaylistMediaEntity;
import com.pheuture.playlists.base.service.PendingApiExecutorService;
import com.pheuture.playlists.base.constants.ApiConstant;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.Logger;
import com.pheuture.playlists.base.utils.ParserUtil;
import com.pheuture.playlists.base.utils.SharedPrefsUtils;
import com.pheuture.playlists.base.constants.Url;
import com.pheuture.playlists.base.utils.VolleyClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaViewModel extends AndroidViewModel {
    private static final String TAG = MediaViewModel.class.getSimpleName();
    private MutableLiveData<List<MediaEntity>> mediaEntitiesMutableLiveData;
    private PlaylistMediaLocalDao playlistMediaLocalDao;
    private PlaylistLocalDao playlistLocalDao;
    private long lastID;
    private UserEntity user;
    private long limit = 50;
    private String searchQuery = "";
    private boolean reachedLast;
    private PlaylistEntity playlistEntity;
    private PendingApiLocalDao pendingApiLocalDao;

    public MediaViewModel(@NonNull Application application, PlaylistEntity playlistEntity) {
        super(application);
        this.playlistEntity = playlistEntity;
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        pendingApiLocalDao = LocalRepository.getInstance(application).pendingApiLocalDao();
        playlistLocalDao = LocalRepository.getInstance(application).playlistLocalDao();
        playlistMediaLocalDao = LocalRepository.getInstance(application).playlistMediaLocalDao();

        mediaEntitiesMutableLiveData = new MutableLiveData<>(new ArrayList<>());

        getFreshData();
    }

    public void getFreshData() {
        //reset the last Id
        lastID = 0;

        final String url = Url.BASE_URL + Url.MEDIA_LIST;

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    JSONObject response = new JSONObject(stringResponse);

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<MediaEntity> newDataList = Arrays.asList(ParserUtil.getInstance()
                            .fromJson(response.optString(ApiConstant.DATA), MediaEntity[].class));

                    if (newDataList.size()>0){
                        MediaEntity mediaEntity = newDataList.get(newDataList.size() - 1);
                        lastID = mediaEntity.getMediaID();
                        reachedLast = newDataList.size() < limit;

                    } else {
                        reachedLast = true;
                    }

                    mediaEntitiesMutableLiveData.postValue(newDataList);
                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                try {
                    Logger.e(url, e.toString());
                } catch (Exception ex) {
                    Logger.e(TAG, ex.toString());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                params.put(ApiConstant.SEARCH_QUERY, ((searchQuery==null)?"":searchQuery));
                params.put(ApiConstant.LIMIT, String.valueOf(limit));
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public MutableLiveData<List<MediaEntity>> getPlaylistMediaListLive() {
        return mediaEntitiesMutableLiveData;
    }

    public void getMoreData() {
        if (reachedLast){
            return;
        }

        final String url = Url.BASE_URL + Url.MEDIA_LIST;

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    JSONObject response = new JSONObject(stringResponse);

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<MediaEntity> newDataList = Arrays.asList(ParserUtil.getInstance()
                            .fromJson(response.optString(ApiConstant.DATA), MediaEntity[].class));

                    if (newDataList.size()>0){
                        MediaEntity mediaEntity = newDataList.get(newDataList.size() - 1);
                        lastID = mediaEntity.getMediaID();
                        reachedLast = newDataList.size() < limit;
                    } else {
                        reachedLast = true;
                    }

                    List<MediaEntity> oldList = new ArrayList<>(mediaEntitiesMutableLiveData.getValue());
                    oldList.addAll(newDataList);
                    mediaEntitiesMutableLiveData.postValue(oldList);
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
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.PLAYLIST_ID, String.valueOf(playlistEntity.getPlaylistID()));
                params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                params.put(ApiConstant.LIMIT, String.valueOf(limit));
                params.put(ApiConstant.SEARCH_QUERY, ((searchQuery == null) ? "" : searchQuery));
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public void addMediaToPlaylist(final int adapterPosition, final PlaylistMediaEntity playlistMediaEntity) {
        Calendar calendar = Calendar.getInstance();
        long date = calendar.getTimeInMillis();

        //update playlist media
        /*List<MediaEntity> mediaEntities = new ArrayList<>(Objects.requireNonNull(mediaEntitiesMutableLiveData.getValue()));
        mediaEntities.remove(adapterPosition);
        mediaEntitiesMutableLiveData.setValue(mediaEntities);*/

        playlistMediaEntity.setPlaylistID(playlistEntity.getPlaylistID());
        playlistMediaEntity.setCreatedOn(date);
        playlistMediaEntity.setModifiedOn(date);
        playlistMediaLocalDao.insert(playlistMediaEntity);

        //update playlist
        playlistEntity.setSongsCount(playlistEntity.getSongsCount() + 1);
        playlistEntity.setPlayDuration(playlistEntity.getPlayDuration() + playlistMediaEntity.getPlayDuration());
        playlistEntity.setModifiedOn(date);
        playlistLocalDao.insert(playlistEntity);

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
        pendingFileUploadEntity.setUrl(Url.PLAYLIST_MEDIA_ADD);
        pendingFileUploadEntity.setParams(params.toString());
        pendingApiLocalDao.insert(pendingFileUploadEntity);

        //start ExecutorService
        PendingApiExecutorService.startService(getApplication());
    }

    public void setSearchQuery(String query) {
        searchQuery = query;
        getFreshData();
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public boolean mediaAlreadyAddedToPlaylist(long mediaID) {
        PlaylistMediaEntity playlistMediaEntity = playlistMediaLocalDao.getPlaylistMedia(playlistEntity.getPlaylistID(), mediaID);
        if (playlistMediaEntity==null){
            return false;
        }
        return true;
    }
}