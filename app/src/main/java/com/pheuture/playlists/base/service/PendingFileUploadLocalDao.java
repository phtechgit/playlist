package com.pheuture.playlists.base.service;

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
public interface PendingFileUploadLocalDao {

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
