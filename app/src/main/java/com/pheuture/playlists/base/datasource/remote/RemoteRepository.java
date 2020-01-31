package com.pheuture.playlists.base.datasource.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pheuture.playlists.base.constants.Url;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * creator: Shashank
 * date: 07-Jan-19.
 */

public abstract class RemoteRepository  {
    private static Retrofit mLocalRepository;

    public static Retrofit getInstance() {
        if (mLocalRepository == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            httpClient
                    .readTimeout(120, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .connectTimeout(120, TimeUnit.SECONDS);

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            mLocalRepository = new Retrofit.Builder()
                    .baseUrl(Url.BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return mLocalRepository;
    }
}
