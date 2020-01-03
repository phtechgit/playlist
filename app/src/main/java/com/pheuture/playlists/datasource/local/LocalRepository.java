package com.pheuture.playlists.datasource.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiDao;
import com.pheuture.playlists.datasource.local.pending_api.PendingApiEntity;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadDao;
import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistDao;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineMediaDao;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineMediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.MediaDao;
import com.pheuture.playlists.utils.Converters;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;

/**
 * creator: Shashank
 * date: 07-Jan-19.
 */

@Database(version = 1, entities = {PendingApiEntity.class,
                PendingFileUploadEntity.class,
                PlaylistEntity.class,
                PlaylistMediaEntity.class,
                MediaEntity.class,
                OfflineMediaEntity.class
        }, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class LocalRepository extends RoomDatabase {
    private static LocalRepository mLocalRepository;

    public abstract PendingApiDao pendingApiDao();
    public abstract PendingFileUploadDao pendingUploadDao();
    public abstract PlaylistDao playlistDao();
    public abstract PlaylistMediaDao playlistMediaDao();
    public abstract OfflineMediaDao offlineVideoDao();
    public abstract MediaDao videoDao();

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
