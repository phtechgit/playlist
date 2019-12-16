package com.pheuture.playlists.datasource.local.video_handler;

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
}
