package com.pheuture.playlists.auth.request_otp;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.AppSignatureHelper;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.constants.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.constants.Url;
import com.pheuture.playlists.utils.VolleyClient;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RequestOtpViewModel extends BaseAndroidViewModel {
    private static final String TAG = RequestOtpViewModel.class.getSimpleName();
    private MutableLiveData<Boolean> showNextButton;
    private MutableLiveData<Boolean> showProgress;
    private OtpListener otpListener;
    private String phoneNumber;

    public RequestOtpViewModel(@NonNull Application application) {
        super(application);
        showNextButton = new MutableLiveData<>(false);
        showProgress = new MutableLiveData<>(false);
    }

    public LiveData<Boolean> getShowNextButton() {
        return showNextButton;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        if (phoneNumber.length()==10){
            setShowNext(true);
        } else {
            setShowNext(false);
        }
    }

    public LiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public void setShowNext(boolean show) {
        showNextButton.postValue(show);
    }

    public void requestOTP() {
        setShowNext(false);
        showProgress.postValue(true);
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
                        setShowNext(true);
                        Toast.makeText(getApplication(), getApplication().getString(R.string.failed_to_send_otp), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.otp_sent), Toast.LENGTH_SHORT).show();
                    otpListener.onOtpSent();
                } catch (Exception e) {
                    setShowNext(true);
                    Logger.e(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                try {
                    setShowNext(true);
                    showProgress.postValue(false);
                    Logger.e(url, e.toString());
                    Toast.makeText(getApplication(),VolleyClient.getErrorMsg(e) , Toast.LENGTH_SHORT).show();
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

    public void setOtpSentListener(OtpListener otpListener) {
        this.otpListener = otpListener;
    }
}
