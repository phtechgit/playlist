package com.pheuture.playlists.datasource.remote;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.pheuture.playlists.datasource.local.video_handler.VideoDao;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.utils.Converters;

/**
 * creator: Shashank
 * date: 07-Jan-19.
 */

@Database(version = 1,
        entities = {
                VideoEntity.class,
        }, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class RemoteRepository extends RoomDatabase {
    private static RemoteRepository mLocalRepository;

    public abstract VideoDao videoDao();

    public static RemoteRepository getInstance(Context context) {
        if (mLocalRepository == null) {
            mLocalRepository = Room.databaseBuilder(context.getApplicationContext(),
                    RemoteRepository.class, context.getPackageName() + "_db")
                    .allowMainThreadQueries()
                    /*.addMigrations(MIGRATION_1_2)*/
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return mLocalRepository;
    }
}
