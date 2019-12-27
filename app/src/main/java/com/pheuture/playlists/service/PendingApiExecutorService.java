package com.pheuture.playlists.service;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadDao;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadEntity;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.NetworkUtils;
import com.pheuture.playlists.utils.VolleyClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PendingApiExecutorService extends Service {
    private static final String TAG = PendingApiExecutorService.class.getSimpleName();
    private PendingUploadDao pendingUploadDao;
    private List<PendingUploadEntity> pendingUploadEntities;

    public PendingApiExecutorService() {
    }

    public synchronized static void startService(Application application) {
        if (NetworkUtils.online(application)) {
            Intent intent = new Intent(application, PendingApiExecutorService.class);
            application.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.e(TAG, "started");
        pendingUploadDao = LocalRepository.getInstance(this).pendingUploadDao();

        pendingUploadEntities = pendingUploadDao.getAllPendingUploadEntities();
        assert pendingUploadEntities != null;
        if (pendingUploadEntities.size()>0){
            startExecutor(pendingUploadEntities.get(0));
        } else {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startExecutor(PendingUploadEntity pendingUploadEntity) {
        String url = pendingUploadEntity.getUrl();

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
            @Override
            public void onResponse(String stringResponse) {
                        try {
                            Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                            JSONObject response = new JSONObject(stringResponse);
                                    if (response.optBoolean(ApiConstant.MESSAGE, false) == Boolean.FALSE) {
                                        stopSelf();
                                        return;
                                    }

                                    updateTaskStatus(pendingUploadEntity);

                        } catch (Exception e) {
                            Logger.e(TAG, e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Logger.e(url, error.toString());
                            stopSelf();
                        } catch (Exception e) {
                            Logger.e(TAG, e.toString());
                        }
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                try {
                    JSONObject jsonObject = new JSONObject(pendingUploadEntity.getParams());
                    Iterator<String> iterator = jsonObject.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        params.put(key, jsonObject.optString(key));
                    }
                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
                Logger.e(url + ApiConstant.PARAMS, params.toString());
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyClient.getRequestQueue(getApplication()).add(jsonObjectRequest);
    }

    private void updateTaskStatus(PendingUploadEntity pendingUploadEntity) {
        pendingUploadDao.delete(pendingUploadEntity);
        pendingUploadEntities.remove(0);

        if (pendingUploadEntities.size()>0){
            startExecutor(pendingUploadEntities.get(0));
        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        if (pendingUploadEntities.size()>0){
            scheduleRestart();
        }
        super.onDestroy();
        Logger.e(TAG, "stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void scheduleRestart() {
        //implement workManager

    }
}
