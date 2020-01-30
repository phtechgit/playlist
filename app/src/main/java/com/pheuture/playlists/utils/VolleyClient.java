package com.pheuture.playlists.utils;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

public final class VolleyClient {
    private static final String TAG = VolleyClient.class.getSimpleName();
    private static RequestQueue mRequestQueue;

    public static synchronized RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context.getApplicationContext());
        }
        return mRequestQueue;
    }

    public static String getErrorMsg(VolleyError volleyError){
        String message = null;

        if (volleyError instanceof NetworkError) {
            message = "Cannot connect to Internet... Please check your connection!";
        } else if (volleyError instanceof ServerError) {
            message = "The server could not be found. Please try again after some time!!";
        } else if (volleyError instanceof AuthFailureError) {
            message = "Cannot connect to Internet... Please check your connection!";
        } else if (volleyError instanceof ParseError) {
            message = "Parsing error! Please try again after some time!!";
        } else if (volleyError instanceof TimeoutError) {
            message = "Connection TimeOut! Please check your internet connection.";
        }
        return message;
    }
}
