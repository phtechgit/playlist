package com.pheuture.playlists.upload;

import android.app.Application;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.room.util.FileUtil;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadDao;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadEntity;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadParamEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
import com.pheuture.playlists.service.PendingApiExecutorService;
import com.pheuture.playlists.service.PendingFileUploadService;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.FileUtils;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.RealPathUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND;

public class UploadViewModel extends AndroidViewModel implements MediaEntity.MediaColumns,
        PendingFileUploadParamEntity.MediaType {

    private static final String TAG = UploadViewModel.class.getSimpleName();
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<Integer>  progressPercentage = new MutableLiveData<>();
    private UserEntity user;
    private MutableLiveData<Uri> mediaUri;
    private MutableLiveData<Uri> thumbnailUri = new MutableLiveData<>();
    private PendingFileUploadDao pendingFileUploadDao;

    public UploadViewModel(@NonNull Application application, Uri newMediaUri) {
        super(application);
        pendingFileUploadDao = LocalRepository.getInstance(application).pendingUploadDao();

        mediaUri = new MutableLiveData<>(newMediaUri);

        createAndSetThumbnail();

        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        dataSourceFactory = new DefaultDataSourceFactory(application,
                Util.getUserAgent(application, TAG));
        exoPlayer = ExoPlayerFactory.newSimpleInstance(application);
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public DataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }

    protected void uploadMedia(String title, String description) {
        PendingFileUploadEntity pendingFileUploadEntity = new PendingFileUploadEntity();
        pendingFileUploadEntity.setTitle(title);
        pendingFileUploadEntity.setUrl(Url.MEDIA_UPLOAD);

        File thumbnailFile = new File(FileUtils.getPath(getApplication(), thumbnailUri.getValue()));
        long totalFileSize = FileUtils.getSize(getApplication(), mediaUri.getValue()) + thumbnailFile.length();

        pendingFileUploadEntity.setSize(totalFileSize);
        Logger.e(TAG, "totalFileSize: " + totalFileSize);

        List<PendingFileUploadParamEntity> paramEntities = new ArrayList<>();
        paramEntities.add(new PendingFileUploadParamEntity(FILE, "videofile",
                RealPathUtil.getRealPath(getApplication(), mediaUri.getValue()), "video/*"));
        paramEntities.add(new PendingFileUploadParamEntity(FILE, "videoThumbnail",
                RealPathUtil.getRealPath(getApplication(), thumbnailUri.getValue()), "image/*"));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, MEDIA_TITLE, title, null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, MEDIA_DESCRIPTION, description, null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, PLAY_DURATION, String.valueOf(getExoPlayer().getDuration()), null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, "videoSingers", "dummy", null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, "musicDirector", "dummy", null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, "movieName", "dummy", null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, "artists", "dummy", null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, "movieDirector", "dummy", null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, ApiConstant.USER_ID, String.valueOf(user.getUserID()), null));

        pendingFileUploadEntity.setParams(ParserUtil.getInstance().toJson(paramEntities));

        pendingFileUploadDao.insert(pendingFileUploadEntity);

        PendingFileUploadService.startService(getApplication());
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public MutableLiveData<Integer> getProgressPercentage() {
        return progressPercentage;
    }

    public MutableLiveData<Uri> getMediaUri() {
        return mediaUri;
    }

    public void createAndSetThumbnail() {
        Runnable runnable = () -> {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(
                    RealPathUtil.getRealPath(getApplication(), mediaUri.getValue()), FULL_SCREEN_KIND);

            try {
                File thumbnailFile = File.createTempFile("thumbnail", ".png", getApplication().getCacheDir());
                if (!thumbnailFile.exists()){
                    if (!thumbnailFile.createNewFile()){
                        return;
                    }
                }
                FileOutputStream fos = new FileOutputStream(thumbnailFile);
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();

                thumbnailUri.postValue(Uri.fromFile(thumbnailFile));

            } catch (Exception e) {
                Logger.e(TAG, e.toString());
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public MutableLiveData<Uri> getThumbnailLive() {
        return thumbnailUri;
    }

    public void setThumbnailUri(Uri data) {
        thumbnailUri.postValue(data);
    }
}
