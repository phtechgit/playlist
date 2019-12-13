package com.pheuture.playlists.datasource.local.playlist_handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;

import java.util.List;

/**
 * creator: Shashank
 * date: 08-Jan-19.
 */

@Dao
public interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PlaylistEntity playlistEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PlaylistEntity> playlistEntities);

    @Query("select * from PlaylistEntity order by createdDate desc")
    LiveData<List<PlaylistEntity>> getPlaylistsLive();

    @Query("select * from PlaylistEntity where id=:playlistID")
    LiveData<PlaylistEntity> getPlaylist(long playlistID);

    @Query("delete from PlaylistEntity")
    void deleteAll();
}
