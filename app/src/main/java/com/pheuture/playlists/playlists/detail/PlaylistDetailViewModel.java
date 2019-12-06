package com.pheuture.playlists.playlists.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
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

public class PlaylistDetailViewModel extends AndroidViewModel {
    private static final String TAG = PlaylistDetailViewModel.class.getSimpleName();
    private PlaylistEntity model;
    private long lastID;
    private long limit;
    private MutableLiveData<String> searchQuery;
    private MutableLiveData<Boolean> reachedLast;
    private MutableLiveData<Boolean> showProgress;
    private MutableLiveData<List<VideoEntity>> videos;
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;

    public PlaylistDetailViewModel(@NonNull Application application, PlaylistEntity model) {
        super(application);
        this.model = model;

        limit = 20;
        reachedLast = new MutableLiveData<>(false);
        searchQuery = new MutableLiveData<>("");
        showProgress = new MutableLiveData<>(true);
        videos = new MutableLiveData<>();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(application);
        playerView = new PlayerView(application);
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
                    params.put(ApiConstant.PLAYLIST_ID, String.valueOf(model.getId()));
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
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(stringRequest);
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public void setProgressStatus(boolean b) {
        showProgress.postValue(b);
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public PlayerView getPlayerView() {
        return playerView;
    }
}