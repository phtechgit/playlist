package com.pheuture.playlists.videos;

import android.app.Application;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.video_handler.VideoDao;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
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

public class VideosViewModel extends AndroidViewModel {
    private static final String TAG = VideosViewModel.class.getSimpleName();

    private LiveData<List<VideoEntity>> videos;
    private VideoDao videoDao;
    private String lastID;
    private long limit;
    private MutableLiveData<String> searchQuery;
    private MutableLiveData<Boolean> reachedLast;

    public VideosViewModel(@NonNull Application application) {
        super(application);
        lastID = "0";
        limit = 20;
        reachedLast = new MutableLiveData<>(false);
        searchQuery = new MutableLiveData<>("");

        videoDao = LocalRepository.getInstance(application).videoDao();
        videos = videoDao.getVideosLive();

        getFreshData();
    }

    public void getFreshData() {
        final String url = Url.VIDEOS;


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Logger.e(url + ApiConstant.RESPONSE, response);
                try {
                    JSONObject responseJsonObject = new JSONObject(response);
                    if (!responseJsonObject.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<VideoEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(responseJsonObject.optString("videos"), VideoEntity[].class));
                    videoDao.deleteAll();
                    videoDao.insertAll(list);

                    if (list.size()>0){
                        VideoEntity videoEntity = list.get(list.size() - 1);
                        lastID = videoEntity.getId();

                        if (list.size()<limit) {
                            reachedLast.postValue(true);
                        } else {
                            reachedLast.postValue(false);
                        }
                    } else {
                        lastID = "0";
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
                params.put(ApiConstant.LAST_ID, lastID);
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

    public LiveData<List<VideoEntity>> getVideosLive() {
        return videos;
    }

    public void getMoreData() {
        assert reachedLast.getValue()!=null;
        if (reachedLast.getValue()){
            return;
        }

        final String url = Url.VIDEOS;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Logger.e(url + ApiConstant.RESPONSE, response);
                try {
                    JSONObject responseJsonObject = new JSONObject(response);
                    if (!responseJsonObject.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<VideoEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(responseJsonObject.optString("videos"), VideoEntity[].class));

                    videoDao.insertAll(list);

                    if (list.size()>0){
                        VideoEntity videoEntity = list.get(list.size() - 1);
                        lastID = videoEntity.getId();

                        if (list.size()<limit) {
                            reachedLast.postValue(true);
                        } else {
                            reachedLast.postValue(false);
                        }
                    } else {
                        lastID = "0";
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
                params.put(ApiConstant.LAST_ID, lastID);
                params.put(ApiConstant.LIMIT, String.valueOf(limit));
                params.put(ApiConstant.SEARCH_QUERY, searchQuery.getValue());
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
}