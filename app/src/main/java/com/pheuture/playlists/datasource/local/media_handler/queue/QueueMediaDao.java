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

    @Query("select count(*) from QueueMediaEntity")
    LiveData<Integer> getQueueMediaCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QueueMediaEntity> queueMediaEntities);

    @Query("select * from QueueMediaEntity")
    LiveData<List<QueueMediaEntity>> getAllMediaLive();

    @Query("delete from QueueMediaEntity")
    void deleteAll();

    @Query("select * from QueueMediaEntity order by modifiedOn desc")
    LiveData<List<QueueMediaEntity>> getTrendingMediaEntitiesLive();

    @Query("select * from QueueMediaEntity where mediaTitle like:searchQuery order by modifiedOn desc limit:limit offset:offset")
    List<QueueMediaEntity> getTrendingMediaEntities(String searchQuery, int limit, int offset);

    @Query("select * from QueueMediaEntity")
    LiveData<List<QueueMediaEntity>> getQueueMediaEntitiesLive();

    @Query("update QueueMediaEntity set state=:state")
    void changeStateOfAllMedia(int state);

    @Query("update QueueMediaEntity set state=:state where mediaID=:mediaId")
    void changeStateOfAllMedia(long mediaId, int state);

    @Query("select * from QueueMediaEntity limit 1")
    QueueMediaEntity getFirstQueueMedia();

    @Delete
    void delete(QueueMediaEntity queueMediaEntity);

    @Query("select rowid from QueueMediaEntity where mediaID=:mediaID")
    int getPositionOfQueueMedia(long mediaID);
}
