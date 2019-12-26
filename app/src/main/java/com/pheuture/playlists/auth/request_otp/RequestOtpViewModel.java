package com.pheuture.playlists.auth.request_otp;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.auth.AppSignatureHelper;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RequestOtpViewModel extends AndroidViewModel {
    private static final String TAG = RequestOtpViewModel.class.getSimpleName();

    public RequestOtpViewModel(@NonNull Application application) {
        super(application);
    }

    public void requestOTP(String phone) {
        AppSignatureHelper appSignatureHashHelper = new AppSignatureHelper(getApplication());
        String hashKey = appSignatureHashHelper.getAppSignatures().get(0);
        Log.i(TAG, "HashKey: " + hashKey);

        final String url = Url.REQUEST_OTP;

        JSONObject params = new JSONObject();
        try {
            params.put(ApiConstant.PHONE, phone);
            params.put(ApiConstant.HASHTAG, hashKey);
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
        Logger.e(url + ApiConstant.PARAMS, params.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, response.toString());
                    if (!response.optBoolean(ApiConstant.MESSAGE, false)) {
                        return;
                    }

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
        });
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).cancelAll(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }
}
