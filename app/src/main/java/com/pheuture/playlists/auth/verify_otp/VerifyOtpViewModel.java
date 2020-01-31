package com.pheuture.playlists.auth.verify_otp;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.AppSignatureHelper;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.base.LocalRepository;
import com.pheuture.playlists.playlist.PlaylistLocalDao;
import com.pheuture.playlists.playlist.PlaylistEntity;
import com.pheuture.playlists.playist_detail.PlaylistMediaLocalDao;
import com.pheuture.playlists.playist_detail.PlaylistMediaEntity;
import com.pheuture.playlists.auth.UserEntity;
import com.pheuture.playlists.base.constants.ApiConstant;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.Logger;
import com.pheuture.playlists.base.utils.ParserUtil;
import com.pheuture.playlists.base.utils.SharedPrefsUtils;
import com.pheuture.playlists.base.constants.Url;
import com.pheuture.playlists.base.utils.VolleyClient;

import org.json.JSONObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyOtpViewModel extends BaseAndroidViewModel {
    private static final String TAG = VerifyOtpViewModel.class.getSimpleName();
    private String phoneNumber;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<Boolean> userVerifiedMutableLiveData = new MutableLiveData<>();
    private StringRequest stringRequest;
    private PlaylistLocalDao playlistLocalDao;
    private PlaylistMediaLocalDao playlistMediaLocalDao;
    private MutableLiveData<Boolean> showNextButton;
    private String otp;
    private MutableLiveData<String> messageMutableLiveData;

    public VerifyOtpViewModel(@NonNull Application application, String phoneNumber) {
        super(application);
        this.phoneNumber = phoneNumber;
        showNextButton = new MutableLiveData<>(false);
        playlistLocalDao = LocalRepository.getInstance(application).playlistLocalDao();
        playlistMediaLocalDao = LocalRepository.getInstance(application).playlistMediaLocalDao();
        messageMutableLiveData = new MutableLiveData<>(application.getResources()
                .getString(R.string.waiting_to_automatically_detect_an_sms_send_to) + " " + phoneNumber);
    }

    public void verifyOtp() {
        setMessageToShow(getApplication().getResources().getString(R.string.verifying_otp));
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
                        setMessageToShow(response.optString("status", getApplication().getResources().getString(R.string.invalid_otp)));
                        return;
                    }

                    UserEntity userEntity = ParserUtil.getInstance().fromJson(response.optString(
                            "userdetail"), UserEntity.class);
                    if (userEntity == null){
                        showProgress.postValue(false);
                        showNextButton.postValue(true);
                        setMessageToShow(getApplication().getResources().getString(R.string.something_went_wrong));
                        return;
                    }



                    SharedPrefsUtils.setStringPreference(getApplication(), Constants.USER,
                            response.optString("userdetail"));

                    List<PlaylistEntity> playlistEntities = Arrays.asList(ParserUtil.getInstance()
                            .fromJson(response.optString("playlistdetail"),
                                    PlaylistEntity[].class));
                    playlistLocalDao.deleteAll();
                    playlistLocalDao.insertAll(playlistEntities);

                    List<PlaylistMediaEntity> playlistMediaEntities = Arrays.asList(
                            ParserUtil.getInstance().fromJson(
                                    response.optString("mediadetail"),
                                    PlaylistMediaEntity[].class));
                    playlistMediaLocalDao.deleteAll();
                    playlistMediaLocalDao.insertAll(playlistMediaEntities);

                    setMessageToShow(getApplication().getResources().getString(R.string.user_verified_successfully));
                    userVerifiedMutableLiveData.setValue(true);

                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                    showProgress.postValue(false);
                    showNextButton.postValue(true);
                    setMessageToShow(getApplication().getResources().getString(R.string.something_went_wrong));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                try {
                    showProgress.postValue(false);
                    showNextButton.postValue(true);
                    Logger.e(url, e.toString());
                    setMessageToShow(VolleyClient.getErrorMsg(e));
                } catch (Exception ex) {
                    Logger.e(TAG, ex.toString());
                }
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

    @Override
    protected void onCleared() {
        super.onCleared();
        if (stringRequest!=null) {
            stringRequest.cancel();
        }
    }

    public void requestOtp() {
        showProgress.postValue(true);
        showNextButton.postValue(false);
        setMessageToShow(getApplication().getResources().getString(R.string.waiting_to_automatically_detect_an_sms_send_to) + " " + phoneNumber);

        AppSignatureHelper appSignatureHashHelper = new AppSignatureHelper(getApplication());
        String hashKey = appSignatureHashHelper.getAppSignatures().get(0);
        Log.i(TAG, "HashKey: " + hashKey);

        final String url = Url.BASE_URL + Url.REQUEST_OTP;

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    showProgress.postValue(false);
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    JSONObject response = new JSONObject(stringResponse);
                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        showNextButton.postValue(true);
                        showSnackBar(getApplication().getResources().getString(R.string.failed_to_send_otp), Snackbar.LENGTH_SHORT);
                        return;
                    }
                    showSnackBar(getApplication().getResources().getString(R.string.otp_sent), Snackbar.LENGTH_SHORT);
                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                try {
                    showNextButton.postValue(true);
                    showProgress.postValue(false);
                    Logger.e(url, e.toString());
                    showSnackBar(VolleyClient.getErrorMsg(e), Snackbar.LENGTH_SHORT);
                } catch (Exception ex) {
                    Logger.e(TAG, ex.toString());
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(ApiConstant.USER_MOBILE, phoneNumber);
                params.put(ApiConstant.HASH_KEY, hashKey);
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public LiveData<String> getMessageToShow() {
        return messageMutableLiveData;
    }

    public void setMessageToShow(String message) {
        messageMutableLiveData.postValue(message);
    }
}
