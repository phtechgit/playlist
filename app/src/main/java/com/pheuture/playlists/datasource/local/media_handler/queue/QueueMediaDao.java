package com.pheuture.playlists.datasource.local.media_handler.queue;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.pheuture.playlists.datasource.local.media_handler.MediaEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;

import java.util.List;

@Dao
public interface QueueMediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(QueueMediaEntity queueMediaEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QueueMediaEntity> queueMediaEntities);

    @Query("delete from QueueMediaEntity")
    void deleteAll();

    @Query("select * from QueueMediaEntity")
    LiveData<List<QueueMediaEntity>> getQueueMediaEntitiesLive();

    @Query("select * from QueueMediaEntity")
    List<QueueMediaEntity> getQueueMediaEntities();

    @Query("select * from QueueMediaEntity where state=:state")
    List<QueueMediaEntity> getQueueMediaEntities(int state);

    @Query("update QueueMediaEntity set state=:state")
    void changeStateOfAllMedia(int state);

    @Delete
    void delete(QueueMediaEntity queueMediaEntity);

    @Delete
    void delete(List<QueueMediaEntity> queueMediaEntities);


}
