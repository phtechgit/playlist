package com.pheuture.playlists.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.pheuture.playlists.R;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadDao;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadEntity;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadParamEntity;
import com.pheuture.playlists.datasource.remote.FileUploadDao;
import com.pheuture.playlists.datasource.remote.ProgressRequestBody;
import com.pheuture.playlists.datasource.remote.RemoteRepository;
import com.pheuture.playlists.datasource.remote.ResponseModel;
import com.pheuture.playlists.receiver.NotificationActionReceiver;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.FileUtils;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.NetworkUtils;
import com.pheuture.playlists.utils.NotificationChannelIDConstant;
import com.pheuture.playlists.utils.NotificationIDConstant;
import com.pheuture.playlists.utils.ParserUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PendingApiExecutorService extends Service implements PendingUploadEntity.UploadType,
        PendingUploadParamEntity.MediaType, ProgressRequestBody.UploadCallbacks,
        NotificationActionReceiver.NotificationActions,
        NotificationActionReceiver.NotificationActionInterface {

    private static final String TAG = PendingApiExecutorService.class.getSimpleName();
    private PendingUploadDao pendingUploadDao;
    private List<PendingUploadEntity> pendingUploadEntities;
    private PendingUploadEntity pendingUploadEntity;
    private Retrofit remoteRepository;
    private Call<ResponseModel> fileUploadClient;
    private FileUploadDao fileUploadDao;
    private NotificationManager notificationManager;
    private Notification progressNotification;
    private boolean serviceInForeground = false;
    private PendingIntent btPendingIntent;
    private NotificationActionReceiver notificationActionReceiver;
    private long mUploadedInBytes = 0;

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
        pendingUploadDao = LocalRepository.getInstance(this).pendingUploadDao();

        pendingUploadEntities = pendingUploadDao.getAllPendingUploadEntities();
        if (pendingUploadEntities.size()==0){
            stopSelf();
        } else {
            pendingUploadEntity = pendingUploadEntities.get(0);
            setupNotificationComponents();
            startExecutor();
        }
    }

    private void setupNotificationComponents() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //create channel for notification
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel  =  new NotificationChannel(
                    NotificationChannelIDConstant.NOTIFICATION_CHANNEL_01,
                    NotificationChannelIDConstant.NOTIFICATION_CHANNEL_01,
                    NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(serviceChannel);
        }

        //Create an Intent for the BroadcastReceiver
        Intent buttonIntent = new Intent(this, NotificationActionReceiver.class);
        buttonIntent.putExtra(Constants.ARG_PARAM1, pendingUploadEntity.getId());
        buttonIntent.setAction(CANCEL);

        //Create the PendingIntent
        btPendingIntent = PendingIntent.getBroadcast(this, 0,
                buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        setUpBroadcastReceiver();
    }

    private void setUpBroadcastReceiver() {
        notificationActionReceiver = new NotificationActionReceiver(this);
        registerReceiver(notificationActionReceiver, new IntentFilter(CANCEL));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startExecutor() {
        if (pendingUploadEntity.getType() == SIMPLE || pendingUploadEntity.getType() == NOT_DEFINED){
            startSimpleApiCall();

        } else if (pendingUploadEntity.getType() == MULTI_PART){
            startMultipartApiCall();
        }
    }

    private void startSimpleApiCall() {
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

        final String url = pendingUploadEntity.getUrl();

        FileUploadDao fileUploadDao = remoteRepository.create(FileUploadDao.class);
        fileUploadClient = fileUploadDao.simpleApiCall(url, params);
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

                    updateTaskStatus();

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

    private void startMultipartApiCall() {
        List<PendingUploadParamEntity> params = Arrays.asList(ParserUtil.getInstance().fromJson(pendingUploadEntity.getParams(), PendingUploadParamEntity[].class));

        final String url = pendingUploadEntity.getUrl();
        HashMap<String, RequestBody> partMap = new HashMap<>();
        List<MultipartBody.Part> partFiles = new ArrayList<>();

        for (int i=0; i<params.size(); i++) {
            PendingUploadParamEntity paramEntity = params.get(i);

            if (paramEntity.getMediaType() == OTHER) {
                partMap.put(paramEntity.getKey(), RequestBody.create(paramEntity.getValue(), MultipartBody.FORM));

            } else if (paramEntity.getMediaType() == FILE) {
                File mediaFile = new File(paramEntity.getValue());
                partFiles.add(MultipartBody.Part.createFormData(paramEntity.getKey(), paramEntity.getValue().substring(paramEntity.getValue().lastIndexOf("/")),
                        new ProgressRequestBody(mediaFile, MediaType.parse(paramEntity.getExtra()), this)));
            }
        }

        fileUploadClient = fileUploadDao.multipartApiCall(url, partMap, partFiles);
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

                    updateTaskStatus();

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

    private void updateTaskStatus() {
        pendingUploadEntities.remove(0);
        pendingUploadDao.delete(pendingUploadEntity);

        stopForeground(false);
        stopSelf();
        updateProgressStatus();

        if (pendingUploadEntities.size()>0) {
            PendingApiExecutorService.startService(this);
        }
    }

    private void updateProgressStatus() {
        progressNotification = new NotificationCompat.Builder(this,
                NotificationChannelIDConstant.NOTIFICATION_CHANNEL_01)
                .setContentTitle(pendingUploadEntity.getTitle())
                .setSubText("uploaded successfully")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        notificationManager.notify(pendingUploadEntity.getId(), progressNotification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onProgressUpdate(long uploadedInBytes) {
        mUploadedInBytes = uploadedInBytes;
        int percentageCompleted = (int) (mUploadedInBytes*100/pendingUploadEntity.getSize());
        String subTitle =  "uploaded " + FileUtils.getFileSize(mUploadedInBytes) + " of " + FileUtils.getFileSize(pendingUploadEntity.getSize());

        progressNotification = new NotificationCompat.Builder(this,
                NotificationChannelIDConstant.NOTIFICATION_CHANNEL_01)
                .setContentTitle(pendingUploadEntity.getTitle())
                .setSubText(subTitle)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_close_black, CANCEL, btPendingIntent)
                .setProgress(100, percentageCompleted, false)
                .build();

        notificationManager.notify(pendingUploadEntity.getId(), progressNotification);
        if (!serviceInForeground){
            startForeground(pendingUploadEntity.getId(), progressNotification);
            serviceInForeground = true;
        }
    }

    @Override
    public void onNotificationCancelled() {
        if (!fileUploadClient.isCanceled()) {
            fileUploadClient.cancel();
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (notificationActionReceiver != null) {
                unregisterReceiver(notificationActionReceiver);
            }
        } catch (Exception ignored) {
        }

        if (pendingUploadEntities.size()>0){
            scheduleRestart();
        }
        Logger.e(TAG, "stopped");
        super.onDestroy();
    }

    private void scheduleRestart() {
        //implement workManager
    }

}
