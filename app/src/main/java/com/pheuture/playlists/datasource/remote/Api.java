package com.pheuture.playlists.datasource.remote;



import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {

    @Multipart
    @POST("upload-media.php")
    Call<ResponseModel> uploadFile(
//            @Part("user_id") RequestBody userId,
//            @Part("user_name") RequestBody userName,
//            @Part("friend_name") RequestBody friendName,
//            @Part("post_type") RequestBody postType,
//            @Part("friend_mobnum") RequestBody friendMobNum,
//            @Part("post_url") RequestBody postUrl,
//            @Part("post_text") RequestBody postText,
//            @Part("post_content_type") RequestBody postContentType,
            @Part MultipartBody.Part photo
    );

}
