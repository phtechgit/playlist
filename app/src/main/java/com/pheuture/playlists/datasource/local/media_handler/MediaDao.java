package com.pheuture.playlists.datasource.local.media_handler;

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
public interface MediaDao {

    @Insert
    long insert(MediaEntity video);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MediaEntity> mediaEntities);

    @Query("select * from MediaEntity")
    LiveData<List<MediaEntity>> getAllMediaLive();

    @Query("delete from MediaEntity")
    void deleteAll();

    @Query("select * from MediaEntity order by modifiedOn desc")
    LiveData<List<MediaEntity>> getTrendingMediaEntitiesLive();

    @Query("select * from MediaEntity where mediaTitle like:searchQuery order by modifiedOn desc limit:limit offset:offset")
    List<MediaEntity> getTrendingMediaEntities(String searchQuery, int limit, int offset);

    @Query("select * from MediaEntity order by modifiedOn desc limit:limit offset:offset")
    LiveData<List<MediaEntity>> getTrendingMediaEntitiesLive(int limit, int offset);
}
