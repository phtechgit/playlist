package com.pheuture.playlists.trending;

import com.pheuture.playlists.base.datasource.remote.ResponseModel;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface TrendingMediaRemoteDao {

    @FormUrlEncoded
    @POST("{url}")
    Call<ResponseModel> simpleApiCall(
            @Path("url") String url,
            @FieldMap Map<String, String> params);

    @Multipart
    @POST("{url}")
    Call<ResponseModel> multipartApiCall(
            @Path("url") String url,
            @PartMap() Map<String, RequestBody> partMap,
            @Part List<MultipartBody.Part> partFiles);
}
