package com.pheuture.playlists.playlists;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistsViewModel extends AndroidViewModel {
    private static final String TAG = PlaylistsViewModel.class.getSimpleName();
    private long lastID;
    private long limit;
    private PlaylistDao playlistDao;
    private MutableLiveData<String> searchQuery;
    private MutableLiveData<Boolean> reachedLast;
    private MutableLiveData<Boolean> showProgress;
    private LiveData<List<PlaylistEntity>> playlists;
    private PlaylistMediaDao playlistMediaDao;

    public PlaylistsViewModel(@NonNull Application application) {
        super(application);
        limit = 20;

        reachedLast = new MutableLiveData<>();
        searchQuery = new MutableLiveData<>();

        showProgress = new MutableLiveData<>();

        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
        playlists = playlistDao.getPlaylistsLive();

        getFreshData();
    }

    public void createPlaylist(String playlistName) {
        showProgress.postValue(true);

        final String url = Url.PLAYLIST_CREATE;

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

                    PlaylistEntity playlistEntity = ParserUtil.getInstance()
                            .fromJson(responseJsonObject.optString(ApiConstant.DATA),
                                    PlaylistEntity.class);

                    //insert newly created playlist in db
                    playlistDao.insert(playlistEntity);

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
                    params.put(ApiConstant.PLAYLIST_NAME, playlistName);
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

    public LiveData<List<PlaylistEntity>> getPlaylists() {
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

                    List<PlaylistEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(responseJsonObject.optString(ApiConstant.DATA), PlaylistEntity[].class));
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
                    params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                    params.put(ApiConstant.SEARCH_QUERY, ((searchQuery.getValue()==null)?"":searchQuery.getValue()));
                    params.put(ApiConstant.LIMIT, String.valueOf(limit));
                    params.put(ApiConstant.USER, ApiConstant.DUMMY_USER);
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

    public void getMoreData() {
        if (reachedLast.getValue()!=null && reachedLast.getValue()){
            return;
        }

        final String url = Url.PLAYLIST_LIST;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Logger.e(url + ApiConstant.RESPONSE, response);
                try {
                    JSONObject responseJsonObject = new JSONObject(response);
                    if (!responseJsonObject.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<PlaylistEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(responseJsonObject.optString(ApiConstant.DATA), PlaylistEntity[].class));

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
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                    params.put(ApiConstant.LIMIT, String.valueOf(limit));
                    params.put(ApiConstant.SEARCH_QUERY, ((searchQuery.getValue()==null)?"":searchQuery.getValue()));
                    params.put(ApiConstant.USER, ApiConstant.DUMMY_USER);
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

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public void deletePlaylist(PlaylistEntity model) {
        playlistMediaDao.deleteAllMediaFromPlaylist(model.getPlaylistID());
        playlistDao.deletePlaylist(model.getPlaylistID());

        final String url = Url.PLAYLIST_DELETE;

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
                    params.put(ApiConstant.PLAYLIST_ID, String.valueOf(model.getPlaylistID()));
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