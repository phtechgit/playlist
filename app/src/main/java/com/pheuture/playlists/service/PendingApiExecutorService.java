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
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadParamEntity;
import com.pheuture.playlists.datasource.remote.FileUploadDao;
import com.pheuture.playlists.datasource.remote.ProgressRequestBody;
import com.pheuture.playlists.datasource.remote.RemoteRepository;
import com.pheuture.playlists.datasource.remote.ResponseModel;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.NetworkUtils;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.RealPathUtil;
import com.pheuture.playlists.utils.StringUtils;
import com.pheuture.playlists.utils.Url;
import com.pheuture.playlists.utils.VolleyClient;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PendingApiExecutorService extends Service implements PendingUploadParamEntity.MediaType{
    private static final String TAG = PendingApiExecutorService.class.getSimpleName();
    private PendingUploadDao pendingUploadDao;
    private List<PendingUploadEntity> pendingUploadEntities;
    private Retrofit remoteRepository;
    private Call<ResponseModel> fileUploadClient;

    public PendingApiExecutorService() {
        remoteRepository = RemoteRepository.getInstance(PendingApiExecutorService.this);
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
        if (pendingUploadEntity.getType() == 1){
            startSimpleApiCall(pendingUploadEntity);
        } else {
            startMultipartApiCall(pendingUploadEntity);
        }
    }

    private void startSimpleApiCall(PendingUploadEntity pendingUploadEntity) {
        JSONObject jsonParams = null;
        try {
            jsonParams = new JSONObject(pendingUploadEntity.getParams());
        } catch (JSONException e) {
            Logger.e(TAG, e.toString());
        }

        final String url = pendingUploadEntity.getUrl();

        FileUploadDao fileUploadDao = remoteRepository.create(FileUploadDao.class);
        fileUploadClient = fileUploadDao.simpleApiCall(url, jsonParams);
        fileUploadClient.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(@NotNull Call<ResponseModel> fileUploadClient, @NotNull retrofit2.Response<ResponseModel> response) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, response.body().toString());

                    if (response.body().getMessage() == Boolean.FALSE) {
                        return;
                    }


                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseModel> call, @NotNull Throwable t) {
                try {
                    String stringResponse = t.getMessage();
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startMultipartApiCall(PendingUploadEntity pendingUploadEntity) {
        List<PendingUploadParamEntity> params = Arrays.asList(ParserUtil.getInstance().fromJson(pendingUploadEntity.getParams(), PendingUploadParamEntity[].class));

        final String url = pendingUploadEntity.getUrl();
        Map<String, RequestBody> partMap = new HashMap<>();
        List<MultipartBody.Part> partFiles = new ArrayList<>();

        for (int i=0; i<params.size(); i++) {
            PendingUploadParamEntity paramEntity = params.get(i);

            if (paramEntity.getMediaType() == FILE) {
                File mediaFile = new File(paramEntity.getValue());
                partFiles.add(MultipartBody.Part.createFormData(paramEntity.getKey(), paramEntity.getExtra(),
                        ProgressRequestBody.Companion.create(mediaFile, MultipartBody.FORM)));
            } else {
                partMap.put(paramEntity.getKey(), RequestBody.create(paramEntity.getValue(), MultipartBody.FORM));
            }
        }

        FileUploadDao fileUploadDao = remoteRepository.create(FileUploadDao.class);
        fileUploadClient = fileUploadDao.multipartApiCall(url, partMap, partFiles);
        fileUploadClient.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(@NotNull Call<ResponseModel> fileUploadClient, @NotNull retrofit2.Response<ResponseModel> response) {
                try {
                    Logger.e(url + ApiConstant.RESPONSE, response.body().toString());

                    if (response.body().getMessage() == Boolean.FALSE) {
                        return;
                    }

                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseModel> call, @NotNull Throwable t) {
                try {
                    String stringResponse = t.getMessage();
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

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
