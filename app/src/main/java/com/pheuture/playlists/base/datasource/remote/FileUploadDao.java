package com.pheuture.playlists.base.datasource.remote;

import com.pheuture.playlists.auth.UserEntity;

import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface FileUploadDao {

    @FormUrlEncoded
    @POST("{url}")
    Call<ResponseModel> simpleApiCall(
            @Path("url") String url,
            @Body UserEntity userEntity,
            @FieldMap Map<String, String> params);

    @Multipart
    @POST("{url}")
    Call<ResponseModel> multipartApiCall(
            @Path("url") String url,
            @PartMap() Map<String, RequestBody> partMap,
            @Part List<MultipartBody.Part> partFiles);


}
