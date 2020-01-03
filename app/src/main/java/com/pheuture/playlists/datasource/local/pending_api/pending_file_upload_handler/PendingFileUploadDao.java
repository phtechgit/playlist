package com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * creator: Shashank
 * date: 08-Jan-19.
 */

@Dao
public interface PendingFileUploadDao {

    @Insert
    long insert(PendingFileUploadEntity pendingFileUploadEntity);

    @Insert()
    void insertAll(List<PendingFileUploadEntity> pendingUploadEntities);

    @Query("select * from PendingFileUploadEntity")
    LiveData<List<PendingFileUploadEntity>> getAllPendingUploadEntitiesLive();

    @Query("select * from PendingFileUploadEntity")
    List<PendingFileUploadEntity> getAllPendingUploadEntities();

    @Query("select * from PendingFileUploadEntity limit 1")
    PendingFileUploadEntity getSinglePendingUploadEntities();

    @Query("delete from PendingFileUploadEntity")
    void deleteAll();

    @Delete()
    int delete(PendingFileUploadEntity pendingFileUploadEntity);
}
