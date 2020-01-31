package com.pheuture.playlists.upload;

import android.app.Application;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.util.Size;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.base.LocalRepository;
import com.pheuture.playlists.base.service.PendingFileUploadLocalDao;
import com.pheuture.playlists.base.service.PendingFileUploadEntity;
import com.pheuture.playlists.base.service.PendingFileUploadParamEntity;
import com.pheuture.playlists.auth.UserEntity;
import com.pheuture.playlists.media.MediaEntity;
import com.pheuture.playlists.base.service.PendingFileUploadService;
import com.pheuture.playlists.base.constants.ApiConstant;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.FileUtils;
import com.pheuture.playlists.base.utils.Logger;
import com.pheuture.playlists.base.utils.ParserUtil;
import com.pheuture.playlists.base.utils.SharedPrefsUtils;
import com.pheuture.playlists.base.constants.Url;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND;

public class UploadViewModel extends BaseAndroidViewModel implements MediaEntity.MediaColumns,
        PendingFileUploadParamEntity.MediaType {

    private static final String TAG = UploadViewModel.class.getSimpleName();
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer;
    private UserEntity user;
    private MutableLiveData<Uri> mediaUriLiveData = new MutableLiveData<>();
    private MutableLiveData<Uri> thumbnailUriLiveData = new MutableLiveData<>();
    private PendingFileUploadLocalDao pendingFileUploadLocalDao;

    public UploadViewModel(@NonNull Application application, Uri mediaUri) {
        super(application);
        pendingFileUploadLocalDao = LocalRepository.getInstance(application).pendingUploadLocalDao();

        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserEntity.class);

        dataSourceFactory = new DefaultDataSourceFactory(application,
                Util.getUserAgent(application, TAG));
        exoPlayer = ExoPlayerFactory.newSimpleInstance(application);

        mediaUriLiveData.setValue(mediaUri);
        createAndSetThumbnail();
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public DataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }

    protected void uploadMedia(String title, String description) {
        Calendar calendar = Calendar.getInstance();
        long timeStamp = calendar.getTimeInMillis();
        PendingFileUploadEntity pendingFileUploadEntity = new PendingFileUploadEntity();
        pendingFileUploadEntity.setTitle(title);
        pendingFileUploadEntity.setUrl(Url.MEDIA_UPLOAD);

        File thumbnailFile = new File(FileUtils.getRealPathFromURI(getApplication(),
                thumbnailUriLiveData.getValue()));
        long totalFileSize = new File(FileUtils.getRealPathFromURI(getApplication(),
                mediaUriLiveData.getValue())).length() + thumbnailFile.length();

        pendingFileUploadEntity.setSize(totalFileSize);
        Logger.e(TAG, "totalFileSize: " + totalFileSize);

        List<PendingFileUploadParamEntity> paramEntities = new ArrayList<>();
        paramEntities.add(new PendingFileUploadParamEntity(FILE, "videofile",
                FileUtils.getRealPathFromURI(getApplication(), mediaUriLiveData.getValue()), "video/*"));
        paramEntities.add(new PendingFileUploadParamEntity(FILE, "videoThumbnail",
                FileUtils.getRealPathFromURI(getApplication(), thumbnailUriLiveData.getValue()), "image/*"));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, MEDIA_TITLE, title, null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, MEDIA_DESCRIPTION, description, null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, PLAY_DURATION, getExoPlayer().getDuration(), null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, "createdOn", timeStamp, null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, "modifiedOn", timeStamp, null));
        paramEntities.add(new PendingFileUploadParamEntity(OTHER, ApiConstant.USER_ID, user.getUserID(), null));

        pendingFileUploadEntity.setParams(ParserUtil.getInstance().toJson(paramEntities));

        pendingFileUploadLocalDao.insert(pendingFileUploadEntity);

        PendingFileUploadService.startService(getApplication());
    }

    public MutableLiveData<Uri> getMediaUriLive() {
        return mediaUriLiveData;
    }

    public void createAndSetThumbnail() {
        Runnable runnable = () -> {
            Bitmap bitmap = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    File videoFile = null;
                    try {
                        videoFile = new File(FileUtils.getRealPathFromURI(getApplication(),
                                mediaUriLiveData.getValue()));
                    } catch (Exception e) {
                        Logger.e(TAG, e.toString());
                    }

                    if (videoFile == null){
                        return;
                    }

                    bitmap = ThumbnailUtils.createVideoThumbnail(videoFile,
                            new Size(640, 360), null);
                } else {
                    bitmap = ThumbnailUtils.createVideoThumbnail(
                            FileUtils.getRealPathFromURI(getApplication(),
                                    mediaUriLiveData.getValue()), FULL_SCREEN_KIND);
                }
            } catch (Exception e) {
                Logger.e(TAG, e.toString());
            }

            if (bitmap == null){
                return;
            }

            try {
                File thumbnailFile = File.createTempFile("thumbnail", ".png", getApplication().getCacheDir());
                if (!thumbnailFile.exists()){
                    if (!thumbnailFile.createNewFile()){
                        return;
                    }
                }
                FileOutputStream fos = new FileOutputStream(thumbnailFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();

                thumbnailUriLiveData.postValue(Uri.fromFile(thumbnailFile));

            } catch (Exception e) {
                Logger.e(TAG, e.toString());
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public MutableLiveData<Uri> getThumbnailUriLive() {
        return thumbnailUriLiveData;
    }

    public void setThumbnailUriLiveData(Uri data) {
        thumbnailUriLiveData.postValue(data);
    }
}
