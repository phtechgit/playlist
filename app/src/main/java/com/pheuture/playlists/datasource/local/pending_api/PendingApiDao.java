package com.pheuture.playlists.datasource.local.pending_api;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler.PendingFileUploadEntity;

import java.util.List;

/**
 * creator: Shashank
 * date: 08-Jan-19.
 */

@Dao
public interface PendingApiDao {

    @Insert
    long insert(PendingApiEntity pendingApiEntity);

    @Insert()
    void insertAll(List<PendingApiEntity> pendingApiEntities);

    @Query("select * from PendingApiEntity")
    LiveData<List<PendingApiEntity>> getAllPendingApiEntitiesLive();

    @Query("select * from PendingApiEntity")
    List<PendingApiEntity> getAllPendingApiEntities();

    @Query("select * from PendingApiEntity limit 1")
    PendingApiEntity getSinglePendingApiEntity();

    @Query("delete from PendingApiEntity")
    void deleteAll();

    @Delete()
    int delete(PendingApiEntity pendingApiEntity);
}
