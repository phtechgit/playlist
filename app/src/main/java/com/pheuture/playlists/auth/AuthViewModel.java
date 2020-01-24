package com.pheuture.playlists.auth;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.interfaces.ApiConstant;
import com.pheuture.playlists.interfaces.ButtonClickListener;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.NetworkUtils;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthViewModel extends BaseAndroidViewModel {
    private static final String TAG = AuthViewModel.class.getSimpleName();
    private String phoneNumber;
    private MutableLiveData<Boolean> showNextButton;
    private ButtonClickListener buttonClickListener;
    private MutableLiveData<Boolean> moveToOtpVerifyPage;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        showNextButton = new MutableLiveData<>();
        moveToOtpVerifyPage = new MutableLiveData<>();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setShowNextButton(Boolean show) {
        showNextButton.postValue(show);
    }

    public LiveData<Boolean> getShowNextButton() {
        return showNextButton;
    }

    public void setOnButtonClickListener(ButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    public void setNextButtonClicked() {
        buttonClickListener.onButtonClick();
    }

    public void setMoveToOtpVerifyPage() {
        if (!NetworkUtils.online(getApplication())){
            Toast.makeText(getApplication(), "Please connect to Internet", Toast.LENGTH_SHORT).show();
        }
        requestOTP();
        moveToOtpVerifyPage.postValue(true);
    }

    public LiveData<Boolean> getMoveToOtpVerifyPage() {
        return moveToOtpVerifyPage;
    }

    public void requestOTP() {
        AppSignatureHelper appSignatureHashHelper = new AppSignatureHelper(getApplication());
        String hashKey = appSignatureHashHelper.getAppSignatures().get(0);
        Log.i(TAG, "HashKey: " + hashKey);

        final String url = Url.BASE_URL + Url.REQUEST_OTP;

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    JSONObject response = new JSONObject(stringResponse);

                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }
                    Toast.makeText(getApplication(), "OTP sent", Toast.LENGTH_SHORT).show();

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
}
