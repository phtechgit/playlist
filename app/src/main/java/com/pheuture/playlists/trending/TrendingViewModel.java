package com.pheuture.playlists.trending;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.MediaDao;
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

public class TrendingViewModel extends AndroidViewModel {
    private static final String TAG = TrendingViewModel.class.getSimpleName();
    private MutableLiveData<Boolean> showProgress;
    private LiveData<List<MediaEntity>> mediaEntitiesLive;
    private MediaDao mediaDao;
    private long lastID;
    private long limit;
    private MutableLiveData<String> searchQuery;
    private MutableLiveData<Boolean> reachedLast;

    public TrendingViewModel(@NonNull Application application) {
        super(application);
        limit = 10;
        reachedLast = new MutableLiveData<>(false);
        searchQuery = new MutableLiveData<>("");

        showProgress = new MutableLiveData<>(false);

        mediaDao = LocalRepository.getInstance(application).videoDao();
        mediaEntitiesLive = mediaDao.getAllMediaLive();
    }

    public void getFreshData() {
        //reset the last Id
        lastID = 0;

        final String url = Url.MEDIA_TRENDING;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Logger.e(url + ApiConstant.RESPONSE, response);
                try {
                    JSONObject responseJsonObject = new JSONObject(response);
                    if (!responseJsonObject.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<MediaEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(responseJsonObject.optString(ApiConstant.DATA), MediaEntity[].class));
                    mediaDao.deleteAll();
                    mediaDao.insertAll(list);

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
                Logger.e(TAG, e.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.LAST_ID, String.valueOf(lastID));
                params.put(ApiConstant.SEARCH_QUERY, ((searchQuery.getValue()==null)?"":searchQuery.getValue()));
                params.put(ApiConstant.LIMIT, String.valueOf(limit));
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        stringRequest.setTag(TAG);

        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(stringRequest);
    }

    public LiveData<List<MediaEntity>> getVideosLive() {
        return mediaEntitiesLive;
    }

    public void getMoreData() {
        if (reachedLast.getValue()!=null && reachedLast.getValue()){
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

                    List<MediaEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(responseJsonObject.optString(ApiConstant.DATA), MediaEntity[].class));

                    mediaDao.insertAll(list);

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

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }
}