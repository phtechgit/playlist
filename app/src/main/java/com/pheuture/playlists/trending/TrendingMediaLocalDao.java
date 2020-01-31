package com.pheuture.playlists.trending;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pheuture.playlists.media.MediaEntity;
import java.util.List;

@Dao
public interface TrendingMediaLocalDao {

    @Insert
    long insert(MediaEntity video);

    @Update
    long update(MediaEntity video);

    @Delete
    void delete(MediaEntity mediaEntity);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<MediaEntity> mediaEntities);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<MediaEntity> mediaEntities);

    @Delete
    void delete(List<MediaEntity> mediaEntity);


    @Query("delete from MediaEntity")
    void deleteAll();


    @Query("select * from MediaEntity order by modifiedOn desc")
    LiveData<List<MediaEntity>> getTrendingMediaEntitiesLive();
}
