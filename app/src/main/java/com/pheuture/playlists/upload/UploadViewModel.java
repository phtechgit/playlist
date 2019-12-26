package com.pheuture.playlists.upload;

import android.app.Application;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pheuture.playlists.datasource.local.user_handler.UserModel;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.RealPathUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.Url;

import cz.msebera.android.httpclient.Header;
import java.io.File;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

public class UploadViewModel extends AndroidViewModel {
    private static final String TAG = UploadViewModel.class.getSimpleName();
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer;
    private MutableLiveData<Boolean> showProgress;
    private MutableLiveData<Integer> progressPercentage;
    private UserModel user;

    public UploadViewModel(@NonNull Application application) {
        super(application);
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserModel.class);

        dataSourceFactory = new DefaultDataSourceFactory(application,
                Util.getUserAgent(application, TAG));
        exoPlayer = ExoPlayerFactory.newSimpleInstance(application);
        showProgress = new MutableLiveData<>();
        progressPercentage = new MutableLiveData<>();
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public DataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }

    public void submitMedia(Uri mediaUri, Uri thumbnailUri, String title, String description) {
        File media = new File(RealPathUtil.getRealPath(getApplication(), mediaUri));

        showProgress.setValue(true);

        final String url = Url.MEDIA_UPLOAD;

        RequestParams params = new RequestParams();
        try {
            params.put("videofile", media);
            if (thumbnailUri == null){
                params.put("videoThumbnail", ThumbnailUtils.createVideoThumbnail(
                        RealPathUtil.getRealPath(getApplication(), mediaUri), MINI_KIND));

            } else {
                File thumbnail = new File(RealPathUtil.getRealPath(getApplication(), thumbnailUri));
                params.put("videoThumbnail", thumbnail);
            }
            params.put("videoTitle", title);
            params.put("videoDescription", description);
            params.put("videoDuration", getExoPlayer().getDuration());
            params.put("videoSingers", "dummy");
            params.put("musicDirector", "dummy");
            params.put("movieName", "dummy");
            params.put("artists", "dummy");
            params.put("movieDirector", "dummy");
            params.put(ApiConstant.USER_ID, String.valueOf(user.getUserId()));
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    showProgress.setValue(false);
                    String response = new String(responseBody, "UTF-8");
                    Logger.e(url + ApiConstant.RESPONSE, response);

                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    showProgress.setValue(false);
                    String response = new String(responseBody, "UTF-8");
                    Logger.e(url, statusCode + ": " + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int progress = (int) (100*bytesWritten/totalSize);
                progressPercentage.setValue(progress);
            }
        });
    }

    public MutableLiveData<Boolean> getProgressStatus() {
        return showProgress;
    }

    public MutableLiveData<Integer> getProgressPercentage() {
        return progressPercentage;
    }

}
