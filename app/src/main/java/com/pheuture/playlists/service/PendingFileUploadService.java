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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.pheuture.playlists.R;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadDao;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadEntity;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadParamEntity;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PendingFileUploadService extends Service implements PendingFileUploadParamEntity.MediaType, ProgressRequestBody.UploadCallbacks,
        NotificationActionReceiver.NotificationActions,
        NotificationActionReceiver.NotificationActionInterface {

    private static final String TAG = PendingFileUploadService.class.getSimpleName();
    private PendingFileUploadDao pendingFileUploadDao;
    private List<PendingFileUploadEntity> pendingUploadEntities;
    private PendingFileUploadEntity pendingFileUploadEntity;
    private Retrofit remoteRepository;
    private Call<ResponseModel> fileUploadClient;
    private FileUploadDao fileUploadDao;
    private NotificationManager notificationManager;
    private Notification progressNotification;
    private PendingIntent btPendingIntent;
    private NotificationActionReceiver notificationActionReceiver;
    private long mTotalToUploadInBytes;
    private long mUploadedInBytes;
    private int lastProgress;

    public synchronized static void startService(Context context) {
        try {
            if (NetworkUtils.online(context)) {
                Intent intent = new Intent(context, PendingFileUploadService.class);
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
        setupNotificationComponents();

        remoteRepository = RemoteRepository.getInstance(PendingFileUploadService.this);
        fileUploadDao = remoteRepository.create(FileUploadDao.class);
        pendingFileUploadDao = LocalRepository.getInstance(this).pendingUploadDao();

        pendingUploadEntities = pendingFileUploadDao.getAllPendingUploadEntities();
        if (pendingUploadEntities.size()==0){
            stopSelf();
        } else {
            pendingFileUploadEntity = pendingUploadEntities.get(0);
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
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(serviceChannel);
        }

        progressNotification = new NotificationCompat.Builder(this,
                NotificationChannelIDConstant.NOTIFICATION_CHANNEL_01)
                .setContentTitle("checking pending uploads")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_close_black, CANCEL, btPendingIntent)
                .setProgress(0, 0, true)
                .build();

        notificationManager.notify(NotificationIDConstant.FILE_UPLOAD_SERVICE_NOTIFICATION, progressNotification);
        startForeground(NotificationIDConstant.FILE_UPLOAD_SERVICE_NOTIFICATION, progressNotification);

        //Create an Intent for the BroadcastReceiver
        Intent buttonIntent = new Intent(this, NotificationActionReceiver.class);
        buttonIntent.putExtra(Constants.ARG_PARAM1, NotificationIDConstant.FILE_UPLOAD_SERVICE_NOTIFICATION);
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
        Logger.e(TAG, "startExecutor");

        mTotalToUploadInBytes = pendingFileUploadEntity.getSize();
        mUploadedInBytes = 0;
        lastProgress = 0;

        List<PendingFileUploadParamEntity> params = Arrays.asList(ParserUtil.getInstance().fromJson(pendingFileUploadEntity.getParams(), PendingFileUploadParamEntity[].class));

        final String url = pendingFileUploadEntity.getUrl();
        HashMap<String, RequestBody> partMap = new HashMap<>();
        List<MultipartBody.Part> partFiles = new ArrayList<>();

        for (int i=0; i<params.size(); i++) {
            PendingFileUploadParamEntity paramEntity = params.get(i);

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

                    showSuccessNotification();
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

    private void showSuccessNotification() {
        Notification successNotification = new NotificationCompat.Builder(this,
                NotificationChannelIDConstant.NOTIFICATION_CHANNEL_01)
                .setContentTitle(pendingFileUploadEntity.getTitle())
                .setSubText("uploaded successfully")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();

        notificationManager.notify(pendingFileUploadEntity.getId(), successNotification);
    }

    private void updateTaskStatus() {
        pendingUploadEntities.remove(0);
        pendingFileUploadDao.delete(pendingFileUploadEntity);

        if (pendingUploadEntities.size()==0){
            stopSelf();
        } else {
            pendingFileUploadEntity = pendingUploadEntities.get(0);
            startExecutor();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onProgressUpdate(long mfileLength, long mUploaded, int currentBufferSize) {
       /* Logger.e(TAG, "currentBufferSize: " + currentBufferSize);
        Logger.e(TAG, "totalToUploadInBytes: " + mTotalToUploadInBytes);*/

        mUploadedInBytes += currentBufferSize;
        /*Logger.e(TAG, "uploadedInBytes: " + mUploadedInBytes);*/

        int currentProgress = (int) ((mUploadedInBytes * 100) / mTotalToUploadInBytes);

        String subTitle =  "uploaded " + FileUtils.getReadableFileSize(mUploadedInBytes) + " of " + FileUtils.getReadableFileSize(mTotalToUploadInBytes);
        Logger.e(TAG, subTitle);

        if (lastProgress!=currentProgress){
            lastProgress = currentProgress;

            progressNotification = new NotificationCompat.Builder(PendingFileUploadService.this,
                    NotificationChannelIDConstant.NOTIFICATION_CHANNEL_01)
                    .setContentTitle(pendingFileUploadEntity.getTitle())
                    .setSubText(subTitle)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setOnlyAlertOnce(true)
                    .addAction(R.drawable.ic_close_black, CANCEL, btPendingIntent)
                    .setProgress(100, lastProgress, false)
                    .build();

            notificationManager.notify(NotificationIDConstant.FILE_UPLOAD_SERVICE_NOTIFICATION, progressNotification);
        }
    }

    @Override
    public void onNotificationCancelled() {
        Logger.e(TAG, "onNotificationCancelled");
        if (fileUploadClient!=null && !fileUploadClient.isCanceled()) {
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
