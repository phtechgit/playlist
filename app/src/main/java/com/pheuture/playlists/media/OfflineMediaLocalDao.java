package com.pheuture.playlists.media;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * creator: Shashank
 * date: 08-Jan-19.
 */

@Dao
public interface OfflineMediaLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OfflineMediaEntity offlineVideoEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OfflineMediaEntity> offlineVideoEntities);

    @Query("select * from OfflineMediaEntity")
    LiveData<List<OfflineMediaEntity>> getOfflineVideos();

    @Query("delete from OfflineMediaEntity")
    void deleteAll();

    @Query("select * from OfflineMediaEntity where mediaID=:id")
    OfflineMediaEntity getOfflineMedia(long id);

    @Query("update OfflineMediaEntity set downloadStatus=:downloadStatus where downloadID =:downloadId")
    void updateOfflineVideoStatus(long downloadId, int downloadStatus);

    @Query("update OfflineMediaEntity set downloadedFilePath=:downloadedFilePath, downloadStatus=:downloadStatus where downloadID =:downloadId")
    void updateOfflineVideoStatus(long downloadId, String downloadedFilePath, int downloadStatus);

    @Query("delete from OfflineMediaEntity where mediaName=:fileName")
    void delete(String fileName);
}
