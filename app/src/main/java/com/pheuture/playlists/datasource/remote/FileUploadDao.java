package com.pheuture.playlists.datasource.remote;

import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.Constants;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface FileUploadDao extends MediaEntity.MediaColumns {
    @Multipart
    @POST("addVideo.php")
    Call<ResponseModel> uploadMediaFile(
            @Part(MEDIA_TITLE)RequestBody title,
            @Part(MEDIA_DESCRIPTION)RequestBody description,
            @Part(PLAY_DURATION)RequestBody playDuration,
            @Part("videoSingers")RequestBody singers,
            @Part("musicDirector")RequestBody musicDirector,
            @Part("movieName")RequestBody movieName,
            @Part("artists")RequestBody artists,
            @Part("movieDirector")RequestBody movieDirector,
            @Part(ApiConstant.USER_ID)RequestBody userID,
            @Part MultipartBody.Part... file);
}
