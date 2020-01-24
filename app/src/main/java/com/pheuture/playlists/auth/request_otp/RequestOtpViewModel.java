package com.pheuture.playlists.auth.request_otp;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.auth.AppSignatureHelper;
import com.pheuture.playlists.interfaces.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RequestOtpViewModel extends AndroidViewModel {
    private static final String TAG = RequestOtpViewModel.class.getSimpleName();
    private MutableLiveData<Boolean> showNextButton;

    public RequestOtpViewModel(@NonNull Application application) {
        super(application);
        showNextButton = new MutableLiveData<>();
    }

    public LiveData<Boolean> getShowNextButton() {
        return showNextButton;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber.length()==10){
            showNextButton.postValue(true);
        } else {
            showNextButton.postValue(false);
        }
    }
}
