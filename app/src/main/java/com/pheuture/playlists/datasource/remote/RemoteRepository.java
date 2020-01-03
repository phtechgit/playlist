package com.pheuture.playlists.datasource.remote;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pheuture.playlists.datasource.local.video_handler.MediaDao;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
import com.pheuture.playlists.utils.Converters;
import com.pheuture.playlists.utils.Url;

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

    public static Retrofit getInstance(Context context) {
        if (mLocalRepository == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            httpClient.readTimeout(120, TimeUnit.SECONDS)
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
