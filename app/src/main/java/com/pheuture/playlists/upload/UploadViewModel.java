package com.pheuture.playlists.upload;

import android.app.Application;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.room.util.FileUtil;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.loopj.android.http.AsyncHttpClient;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadDao;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadEntity;
import com.pheuture.playlists.datasource.local.pending_upload_handler.PendingUploadParamEntity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
import com.pheuture.playlists.datasource.remote.FileUploadDao;
import com.pheuture.playlists.datasource.remote.ProgressRequestBody;
import com.pheuture.playlists.datasource.remote.RemoteRepository;
import com.pheuture.playlists.datasource.remote.ResponseModel;
import com.pheuture.playlists.service.PendingApiExecutorService;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.FileUtils;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.RealPathUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.StringUtils;
import com.pheuture.playlists.utils.Url;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.http.Part;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND;

public class UploadViewModel extends AndroidViewModel implements MediaEntity.MediaColumns,
        PendingUploadEntity.UploadType, PendingUploadParamEntity.MediaType {

    private static final String TAG = UploadViewModel.class.getSimpleName();
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<Boolean> uploaded = new MutableLiveData<>();
    private MutableLiveData<Integer>  progressPercentage = new MutableLiveData<>();
    private UserEntity user;
    private MutableLiveData<Uri> mediaUri;
    private MutableLiveData<Uri> thumbnailUri = new MutableLiveData<>();
    private PendingUploadDao pendingUploadDao;

    public UploadViewModel(@NonNull Application application, Uri newMediaUri) {
        super(application);
        pendingUploadDao = LocalRepository.getInstance(application).pendingUploadDao();

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
        PendingUploadEntity pendingUploadEntity = new PendingUploadEntity();
        pendingUploadEntity.setUrl(Url.MEDIA_UPLOAD);
        pendingUploadEntity.setType(MULTI_PART);

        List<PendingUploadParamEntity> paramEntities = new ArrayList<>();
        paramEntities.add(new PendingUploadParamEntity(FILE, "videofile",
                RealPathUtil.getRealPath(getApplication(), mediaUri.getValue()), "video/*"));
        paramEntities.add(new PendingUploadParamEntity(FILE, "videoThumbnail",
                RealPathUtil.getRealPath(getApplication(), thumbnailUri.getValue()), "image/*"));
        paramEntities.add(new PendingUploadParamEntity(OTHER, MEDIA_TITLE, title, null));
        paramEntities.add(new PendingUploadParamEntity(OTHER, MEDIA_DESCRIPTION, description, null));
        paramEntities.add(new PendingUploadParamEntity(OTHER, PLAY_DURATION, String.valueOf(getExoPlayer().getDuration()), null));
        paramEntities.add(new PendingUploadParamEntity(OTHER, "videoSingers", "dummy", null));
        paramEntities.add(new PendingUploadParamEntity(OTHER, "musicDirector", "dummy", null));
        paramEntities.add(new PendingUploadParamEntity(OTHER, "movieName", "dummy", null));
        paramEntities.add(new PendingUploadParamEntity(OTHER, "artists", "dummy", null));
        paramEntities.add(new PendingUploadParamEntity(OTHER, "movieDirector", "dummy", null));
        paramEntities.add(new PendingUploadParamEntity(OTHER, ApiConstant.USER_ID, String.valueOf(user.getUserID()), null));

        pendingUploadEntity.setParams(ParserUtil.getInstance().toJson(paramEntities));

        pendingUploadDao.insert(pendingUploadEntity);

        PendingApiExecutorService.startService(getApplication());
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public MutableLiveData<Integer> getProgressPercentage() {
        return progressPercentage;
    }

    public MutableLiveData<Boolean> getUploadedStatus() {
        return uploaded;
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
