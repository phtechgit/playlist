package com.pheuture.playlists.trending;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.media_handler.MediaEntity;
import com.pheuture.playlists.datasource.local.media_handler.MediaDao;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrendingViewModel extends AndroidViewModel {
    private static final String TAG = TrendingViewModel.class.getSimpleName();
    private int limit = 20;
    private String searchQuery = "";
    private boolean reachedLast;
    private UserEntity user;
    private MediaDao mediaDao;
    private LiveData<List<MediaEntity>> mediaEntitiesLive;

    public TrendingViewModel(@NonNull Application application) {
        super(application);
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        mediaDao = LocalRepository.getInstance(application).mediaDao();

        mediaEntitiesLive = mediaDao.getTrendingMediaEntitiesLive();

        getFreshData();
    }

    public void getFreshData() {
        //reset the last Id
        final String url = Url.BASE_URL + Url.MEDIA_TRENDING_LIST;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);
                    JSONObject response = new JSONObject(stringResponse);

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    List<MediaEntity> list = Arrays.asList(ParserUtil.getInstance().fromJson(response.optString(ApiConstant.DATA), MediaEntity[].class));
                    mediaDao.deleteAll();
                    mediaDao.insertAll(list);
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
                params.put(ApiConstant.USER_ID, String.valueOf(user.getUserID()));
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        stringRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(stringRequest);
    }

    public LiveData<List<MediaEntity>> getTrendingMediaLive() {
        return mediaEntitiesLive;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchQuery(){
        return searchQuery;
    }
}