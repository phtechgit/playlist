package com.pheuture.playlists.auth.request_otp;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
        final String url = Url.REQUEST_OTP;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, response);
                    JSONObject responseJsonObject = new JSONObject(response);
                    if (!responseJsonObject.optBoolean(ApiConstant.MESSAGE, false)) {
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
                    Logger.e(TAG, e.toString());
                } catch (Exception ex) {
                    Logger.e(TAG, ex.toString());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                AppSignatureHelper appSignatureHashHelper = new AppSignatureHelper(getApplication());
                String hashKey = appSignatureHashHelper.getAppSignatures().get(0);
                Log.i(TAG, "HashKey: " + hashKey);

                Map<String, String> params = new HashMap<>();
                try {
                    params.put(ApiConstant.PHONE, phone);
                    params.put(ApiConstant.HASHTAG, hashKey);
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
}
