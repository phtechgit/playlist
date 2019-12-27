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
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class VerifyOtpViewModel extends AndroidViewModel {
    private static final String TAG = VerifyOtpViewModel.class.getSimpleName();
    private String userMobile;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<UserEntity> userModelMutableLiveData = new MutableLiveData<>();

    public VerifyOtpViewModel(@NonNull Application application, String userMobile) {
        super(application);
        this.userMobile = userMobile;
    }

    public void verifyOtp(String otp) {
        showProgress.postValue(true);

        final String url = Url.VERIFY_OTP;

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    showProgress.postValue(false);

                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    JSONObject response = new JSONObject(stringResponse);

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

                    UserEntity userEntity = ParserUtil.getInstance().fromJson(response.optString(ApiConstant.DATA), UserEntity.class);
                    if (userEntity ==null){
                        return;
                    }

                    SharedPrefsUtils.setStringPreference(getApplication(), Constants.USER, response.optString(ApiConstant.DATA));

                    userModelMutableLiveData.setValue(userEntity);

                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                try {
                    showProgress.postValue(false);
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
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public MutableLiveData<UserEntity> getUserLive() {
        return userModelMutableLiveData;
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }
}
