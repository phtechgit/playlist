package com.pheuture.playlists.datasource.local.pending_upload_handler;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;

import java.util.List;

import retrofit2.http.DELETE;

/**
 * creator: Shashank
 * date: 08-Jan-19.
 */

@Dao
public interface PendingUploadDao {

    @Insert
    long insert(PendingUploadEntity pendingUploadEntity);

    @Insert()
    void insertAll(List<PendingUploadEntity> pendingUploadEntities);

    @Query("select * from PendingUploadEntity")
    LiveData<List<PendingUploadEntity>> getAllPendingUploadEntitiesLive();

    @Query("select * from PendingUploadEntity")
    List<PendingUploadEntity> getAllPendingUploadEntities();

    @Query("select * from PendingUploadEntity limit 1")
    PendingUploadEntity getSinglePendingUploadEntities();

    @Query("delete from PendingUploadEntity")
    void deleteAll();

    @Delete()
    int delete(PendingUploadEntity pendingUploadEntity);
}
