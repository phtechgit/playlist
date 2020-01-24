package com.pheuture.playlists.auth.verify_otp;

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
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.interfaces.ApiConstant;
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
    private String phoneNumber;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<Boolean> showPrimaryProgress = new MutableLiveData<>(true);
    private MutableLiveData<Boolean> userVerifiedMutableLiveData = new MutableLiveData<>();
    private StringRequest stringRequest;
    private PlaylistDao playlistDao;
    private PlaylistMediaDao playlistMediaDao;
    private MutableLiveData<Boolean> showNextButton;
    private String otp;

    public VerifyOtpViewModel(@NonNull Application application, String phoneNumber) {
        super(application);
        this.phoneNumber = phoneNumber;
        showNextButton = new MutableLiveData<>(false);
        playlistDao = LocalRepository.getInstance(application).playlistDao();
        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
    }

    public void verifyOtp() {
        showProgress.postValue(true);
        showNextButton.postValue(false);

        final String url = Url.BASE_URL + Url.VERIFY_OTP;

        stringRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    JSONObject response = new JSONObject(stringResponse);

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        showProgress.postValue(false);
                        showNextButton.postValue(true);
                        return;
                    }

                    UserEntity userEntity = ParserUtil.getInstance().fromJson(response.optString(
                            "userdetail"), UserEntity.class);
                    if (userEntity == null){
                        showProgress.postValue(false);
                        showNextButton.postValue(true);
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

                    userVerifiedMutableLiveData.setValue(true);

                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                    showProgress.postValue(false);
                    showNextButton.postValue(true);
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
                showNextButton.postValue(true);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.USER_MOBILE, phoneNumber);
                params.put(ApiConstant.OTP, otp);
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        stringRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(stringRequest);
    }

    public MutableLiveData<Boolean> getUserVerifiedStatus() {
        return userVerifiedMutableLiveData;
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public void cancelAllApiRequests() {
        showProgress.postValue(false);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
    }

    public LiveData<Boolean> getShowNextButton() {
        return showNextButton;
    }

    public void setOtp(String otp) {
        this.otp = otp;
        if (otp.length()==6){
            showNextButton.postValue(true);
        } else {
            showNextButton.postValue(false);
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LiveData<Boolean> getPrimaryProgressStatus() {
        return showPrimaryProgress;
    }

    public void setShowPrimaryProgress(boolean show) {
        showPrimaryProgress.postValue(show);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (stringRequest!=null) {
            stringRequest.cancel();
        }
    }
}
