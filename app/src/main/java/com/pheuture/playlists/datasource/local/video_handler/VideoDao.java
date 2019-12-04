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
public interface VideoDao {

    @Insert
    long insert(VideoEntity video);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VideoEntity> videoEntities);

    @Query("select * from VideoEntity")
    LiveData<List<VideoEntity>> getVideosLive();

    @Query("delete from VideoEntity")
    void deleteAll();
}
