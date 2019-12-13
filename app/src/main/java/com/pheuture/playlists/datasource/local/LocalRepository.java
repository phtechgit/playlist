package com.pheuture.playlists.datasource.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineVideoDao;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineVideoEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoDao;
import com.pheuture.playlists.utils.Converters;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;

/**
 * creator: Shashank
 * date: 07-Jan-19.
 */

@Database(version = 1,
        entities = {
                PlaylistEntity.class,
                VideoEntity.class,
                OfflineVideoEntity.class
        }, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class LocalRepository extends RoomDatabase {
    private static LocalRepository mLocalRepository;

    public abstract PlaylistDao playlistDao();
    public abstract OfflineVideoDao offlineVideoDao();
    public abstract VideoDao videoDao();

    public static LocalRepository getInstance(Context context) {
        if (mLocalRepository == null) {
            mLocalRepository = Room.databaseBuilder(context.getApplicationContext(),
                    LocalRepository.class, context.getPackageName() + "_db")
                    .allowMainThreadQueries()
                    /*.addMigrations(MIGRATION_1_2)*/
                    .build();
        }
        return mLocalRepository;
    }
}
