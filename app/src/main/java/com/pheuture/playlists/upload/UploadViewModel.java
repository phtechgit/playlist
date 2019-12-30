package com.pheuture.playlists.upload;

import android.app.Application;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.common.api.Api;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.datasource.remote.FileUploadDao;
import com.pheuture.playlists.datasource.remote.ProgressRequestBody;
import com.pheuture.playlists.datasource.remote.RemoteRepository;
import com.pheuture.playlists.datasource.remote.ResponseModel;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.RealPathUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.StringUtils;
import com.pheuture.playlists.utils.Url;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND;
import static com.pheuture.playlists.datasource.local.video_handler.MediaEntity.MediaColumns.MEDIA_DESCRIPTION;
import static com.pheuture.playlists.datasource.local.video_handler.MediaEntity.MediaColumns.MEDIA_TITLE;
import static com.pheuture.playlists.datasource.local.video_handler.MediaEntity.MediaColumns.PLAY_DURATION;

public class UploadViewModel extends AndroidViewModel {
    private static final String TAG = UploadViewModel.class.getSimpleName();
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer;
    private MutableLiveData<Boolean> showProgress = new MutableLiveData<>();
    private MutableLiveData<Boolean> uploaded = new MutableLiveData<>();
    private MutableLiveData<Integer>  progressPercentage = new MutableLiveData<>();
    private UserEntity user;
    private MutableLiveData<Uri> mediaUri;
    private MutableLiveData<Uri> thumbnailUri = new MutableLiveData<>();
    private AsyncHttpClient fileUploadClient;
    private Retrofit remoteRepository;

    public UploadViewModel(@NonNull Application application, Uri newMediaUri) {
        super(application);
        remoteRepository = RemoteRepository.getInstance(getApplication());
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
        uploaded.setValue(null);

        String mediaFilePath = RealPathUtil.getRealPath(getApplication(), mediaUri.getValue());
        String thumbnailFilePath = RealPathUtil.getRealPath(getApplication(), thumbnailUri.getValue());

        if (StringUtils.isEmpty(mediaFilePath)){
            Logger.e(TAG, "mediaFilePath si empty");
            uploaded.setValue(false);
        }

        if (StringUtils.isEmpty(thumbnailFilePath)){
            Logger.e(TAG, "thumbnailFilePath si empty");
            uploaded.setValue(false);
        }

        File mediaFile = new File(mediaFilePath);
        File thumbnailFile = new File(thumbnailFilePath);

        showProgress.setValue(true);

        final String url = Url.MEDIA_UPLOAD;

        FileUploadDao fileUploadDao = remoteRepository.create(FileUploadDao.class);
        Call<ResponseModel> fileUploadClient = fileUploadDao.uploadMediaFile(
                RequestBody.create(title, MultipartBody.FORM),
                RequestBody.create(description, MultipartBody.FORM),
                RequestBody.create(String.valueOf(exoPlayer.getDuration()), MultipartBody.FORM),
                RequestBody.create("", MultipartBody.FORM),
                RequestBody.create("", MultipartBody.FORM),
                RequestBody.create("", MultipartBody.FORM),
                RequestBody.create("", MultipartBody.FORM),
                RequestBody.create("", MultipartBody.FORM),
                RequestBody.create(String.valueOf(user.getUserID()), MultipartBody.FORM),
                MultipartBody.Part.createFormData("videofile", title,
                        ProgressRequestBody.Companion.create(mediaFile, MultipartBody.FORM)),
                MultipartBody.Part.createFormData("videoThumbnail", title,
                        new ProgressRequestBody(thumbnailFile, MediaType.parse("video/*"), new ProgressRequestBody.UploadCallbacks() {
                            @Override
                            public void onProgressUpdate(int percentage) {

                            }

                            @Override
                            public void onError() {

                            }

                            @Override
                            public void onFinish() {

                            }
                        })));

        fileUploadClient.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(@NotNull Call<ResponseModel> fileUploadClient, @NotNull retrofit2.Response<ResponseModel> response) {
                try {
                    showProgress.setValue(false);
                    Logger.e(url + ApiConstant.RESPONSE, response.body().toString());

                    if (response.body().getMessage() == Boolean.FALSE) {
                        return;
                    }

                    uploaded.setValue(true);

                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseModel> call, @NotNull Throwable t) {
                try {
                    showProgress.setValue(false);
                    String stringResponse = t.getMessage();
                    Logger.e(url + ApiConstant.RESPONSE, stringResponse);

                    uploaded.setValue(false);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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

    public void cancelUpload() {
        if (fileUploadClient!=null){
            fileUploadClient.cancelRequestsByTAG(TAG, true);
        }
    }
}
