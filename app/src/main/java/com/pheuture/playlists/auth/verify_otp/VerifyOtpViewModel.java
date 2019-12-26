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
import com.pheuture.playlists.datasource.local.user_handler.UserModel;
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
    private String phone;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<UserModel> userModelMutableLiveData = new MutableLiveData<>();

    public VerifyOtpViewModel(@NonNull Application application, String phone) {
        super(application);
        this.phone = phone;
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

                    UserModel userModel = ParserUtil.getInstance().fromJson(response.optString(ApiConstant.DATA), UserModel.class);
                    if (userModel==null){
                        return;
                    }

                    SharedPrefsUtils.setStringPreference(getApplication(), Constants.USER, response.optString(ApiConstant.DATA));

                    userModelMutableLiveData.setValue(userModel);

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
                params.put(ApiConstant.PHONE, phone);
                params.put(ApiConstant.OTP, otp);
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    public MutableLiveData<UserModel> getUserLive() {
        return userModelMutableLiveData;
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }
}
