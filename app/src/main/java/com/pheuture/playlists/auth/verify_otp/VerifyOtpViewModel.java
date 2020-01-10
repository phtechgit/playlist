package com.pheuture.playlists.auth.verify_otp;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
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

public class VerifyOtpViewModel extends AndroidViewModel {
    private static final String TAG = VerifyOtpViewModel.class.getSimpleName();
    private String userMobile;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<UserEntity> userModelMutableLiveData = new MutableLiveData<>();
    private StringRequest stringRequest;
    private PlaylistDao playlistDao;
    private PlaylistMediaDao playlistMediaDao;

    public VerifyOtpViewModel(@NonNull Application application, String userMobile) {
        super(application);
        this.userMobile = userMobile;
        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
    }

    public void verifyOtp(String otp) {
        showProgress.postValue(true);

        final String url = Url.BASE_URL + Url.VERIFY_OTP;

        stringRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    JSONObject response = new JSONObject(stringResponse);

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    UserEntity userEntity = ParserUtil.getInstance().fromJson(response.optString(
                            "userdetail"), UserEntity.class);
                    if (userEntity == null){
                        return;
                    }

                    SharedPrefsUtils.setStringPreference(getApplication(), Constants.USER,
                            response.optString("userdetail"));

                    List<PlaylistEntity> playlistEntities = Arrays.asList(ParserUtil.getInstance()
                            .fromJson(response.optString("playlistdetail"),
                                    PlaylistEntity[].class));
                    playlistDao.deleteAll();
                    playlistDao.insertAll(playlistEntities);

                    List<PlaylistMediaEntity> playlistMediaEntities = Arrays.asList(
                            ParserUtil.getInstance().fromJson(
                                    response.optString("mediadetail"),
                                    PlaylistMediaEntity[].class));
                    playlistMediaDao.deleteAll();
                    playlistMediaDao.insertAll(playlistMediaEntities);

                    userModelMutableLiveData.setValue(userEntity);

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
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.USER_MOBILE, userMobile);
                params.put(ApiConstant.OTP, otp);
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        stringRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(stringRequest);
    }

    public MutableLiveData<UserEntity> getUserLive() {
        return userModelMutableLiveData;
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public void cancelAllApiRequests() {
        showProgress.postValue(false);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
    }
}
