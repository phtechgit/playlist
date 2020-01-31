package com.pheuture.playlists.base;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.pheuture.playlists.base.service.PendingFileUploadLocalDao;
import com.pheuture.playlists.media.OfflineMediaLocalDao;
import com.pheuture.playlists.media.OfflineMediaEntity;
import com.pheuture.playlists.playist_detail.PlaylistMediaLocalDao;
import com.pheuture.playlists.playlist.PlaylistLocalDao;
import com.pheuture.playlists.queue.QueueMediaDao;
import com.pheuture.playlists.queue.QueueMediaEntity;
import com.pheuture.playlists.base.service.PendingApiLocalDao;
import com.pheuture.playlists.base.service.PendingApiEntity;
import com.pheuture.playlists.playlist.PlaylistEntity;
import com.pheuture.playlists.playist_detail.PlaylistMediaEntity;
import com.pheuture.playlists.trending.TrendingMediaLocalDao;
import com.pheuture.playlists.base.service.PendingFileUploadEntity;
import com.pheuture.playlists.base.utils.Converters;
import com.pheuture.playlists.media.MediaEntity;

/**
 * creator: Shashank
 * date: 07-Jan-19.
 */

@Database(version = 1, entities = {PendingApiEntity.class,
                PendingFileUploadEntity.class,
                PlaylistEntity.class,
                PlaylistMediaEntity.class,
                MediaEntity.class,
                OfflineMediaEntity.class,
                QueueMediaEntity.class
        }, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class LocalRepository extends RoomDatabase {
    public abstract PendingApiLocalDao pendingApiLocalDao();
    public abstract PendingFileUploadLocalDao pendingUploadLocalDao();
    public abstract PlaylistLocalDao playlistLocalDao();
    public abstract PlaylistMediaLocalDao playlistMediaLocalDao();
    public abstract OfflineMediaLocalDao offlineMediaLocalDao();
    public abstract TrendingMediaLocalDao mediaLocalDao();
    public abstract QueueMediaDao queueMediaLocalDao();

    private static LocalRepository mLocalRepository;

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
