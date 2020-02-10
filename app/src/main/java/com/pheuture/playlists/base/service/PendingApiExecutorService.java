package com.pheuture.playlists.base.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.pheuture.playlists.base.datasource.local.LocalRepository;
import com.pheuture.playlists.base.datasource.remote.FileUploadDao;
import com.pheuture.playlists.base.datasource.remote.RemoteRepository;
import com.pheuture.playlists.base.datasource.remote.ResponseModel;
import com.pheuture.playlists.base.constants.ApiConstant;
import com.pheuture.playlists.base.utils.Logger;
import com.pheuture.playlists.base.utils.NetworkUtils;
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
    private PendingApiLocalDao pendingApiLocalDao;
    private List<PendingApiEntity> pendingApiEntities;
    private PendingApiEntity pendingApiEntity;
    private Retrofit remoteRepository;
    private FileUploadDao fileUploadDao;

    public synchronized static void startService(Context context) {
        try {
            if (NetworkUtils.isConnected(context)) {
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

        remoteRepository = RemoteRepository.getInstance();
        fileUploadDao = remoteRepository.create(FileUploadDao.class);
        pendingApiLocalDao = LocalRepository.getInstance(this).pendingApiLocalDao();

        pendingApiEntities = pendingApiLocalDao.getAllPendingApiEntities();
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

        final String url = pendingApiEntity.getUrl();

        Map<String, String> params = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(pendingApiEntity.getParams());
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                params.put(key, jsonObject.optString(key));
            }
            Logger.e(url + ApiConstant.PARAMS, jsonObject.toString());
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }

        fileUploadDao = remoteRepository.create(FileUploadDao.class);
        Call<ResponseModel> fileUploadClient = fileUploadDao.simpleApiCall(url, params);
        fileUploadClient.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(@NotNull Call<ResponseModel> fileUploadClient, @NotNull retrofit2.Response<ResponseModel> response) {
                try {
                    if (response.body()==null){
                        Logger.e(url + ApiConstant.RESPONSE, response.message());
                        stopSelf();
                        return;
                    }

                    Logger.e(url + ApiConstant.RESPONSE, response.body().toString());
                    if (response.body().getMessage() == Boolean.FALSE) {
                        stopSelf();
                        return;
                    }

                    pendingApiEntities.remove(0);
                    pendingApiLocalDao.delete(pendingApiEntity);

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
