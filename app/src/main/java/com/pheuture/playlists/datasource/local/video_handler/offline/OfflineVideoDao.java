package com.pheuture.playlists.datasource.local.video_handler.offline;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * creator: Shashank
 * date: 08-Jan-19.
 */

@Dao
public interface OfflineVideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OfflineVideoEntity offlineVideoEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OfflineVideoEntity> offlineVideoEntities);

    @Query("select * from OfflineVideoEntity")
    LiveData<List<OfflineVideoEntity>> getOfflineVideos();

    @Query("delete from OfflineVideoEntity")
    void deleteAll();

    @Query("select * from OfflineVideoEntity where id=:id")
    OfflineVideoEntity getOfflineMedia(long id);

    @Query("update OfflineVideoEntity set downloadStatus=:downloadStatus where downloadID =:downloadId")
    void updateOfflineVideoStatus(long downloadId, int downloadStatus);

    @Query("update OfflineVideoEntity set downloadedFilePath=:downloadedFilePath, downloadStatus=:downloadStatus where downloadID =:downloadId")
    void updateOfflineVideoStatus(long downloadId, String downloadedFilePath, int downloadStatus);
}
