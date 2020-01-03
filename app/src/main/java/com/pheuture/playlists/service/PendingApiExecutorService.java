package com.pheuture.playlists.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiDao;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiEntity;
import com.pheuture.playlists.datasource.remote.FileUploadDao;
import com.pheuture.playlists.datasource.remote.RemoteRepository;
import com.pheuture.playlists.datasource.remote.ResponseModel;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.NetworkUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PendingApiExecutorService extends Service {
    private static final String TAG = PendingApiExecutorService.class.getSimpleName();
    private PendingApiDao pendingApiDao;
    private List<PendingApiEntity> pendingApiEntities;
    private PendingApiEntity pendingApiEntity;
    private Retrofit remoteRepository;
    private FileUploadDao fileUploadDao;

    public synchronized static void startService(Context context) {
        try {
            if (NetworkUtils.online(context)) {
                Intent intent = new Intent(context, PendingApiExecutorService.class);
                context.startService(intent);
            }
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.e(TAG, "started");

        remoteRepository = RemoteRepository.getInstance(PendingApiExecutorService.this);
        fileUploadDao = remoteRepository.create(FileUploadDao.class);
        pendingApiDao = LocalRepository.getInstance(this).pendingApiDao();

        pendingApiEntities = pendingApiDao.getAllPendingApiEntities();
        if (pendingApiEntities.size()==0){
            stopSelf();
        } else {
            pendingApiEntity = pendingApiEntities.get(0);
            startExecutor();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startExecutor() {
        Map<String, String> params = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(pendingApiEntity.getParams());
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                params.put(key, jsonObject.optString(key));
            }
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }

        final String url = pendingApiEntity.getUrl();

        fileUploadDao = remoteRepository.create(FileUploadDao.class);
        Call<ResponseModel> fileUploadClient = fileUploadDao.simpleApiCall(url, params);
        fileUploadClient.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(@NotNull Call<ResponseModel> fileUploadClient, @NotNull retrofit2.Response<ResponseModel> response) {
                try {
                    if (response.body()==null){
                        stopSelf();
                        return;
                    }

                    Logger.e(url + ApiConstant.RESPONSE, response.body().toString());
                    if (response.body().getMessage() == Boolean.FALSE) {
                        stopSelf();
                        return;
                    }

                    pendingApiEntities.remove(0);
                    pendingApiDao.delete(pendingApiEntity);

                    if (pendingApiEntities.size()==0){
                        stopSelf();
                    } else {
                        pendingApiEntity = pendingApiEntities.get(0);
                        startExecutor();
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
                stopSelf();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (pendingApiEntities.size()>0){
            scheduleRestart();
        }
        Logger.e(TAG, "stopped");
        super.onDestroy();
    }

    private void scheduleRestart() {
        //implement workManager
    }

}
