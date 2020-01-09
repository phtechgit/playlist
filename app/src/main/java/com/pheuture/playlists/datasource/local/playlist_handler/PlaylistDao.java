package com.pheuture.playlists.datasource.local.playlist_handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.Collection;
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

    @Query("select * from PlaylistEntity order by createdOn desc")
    LiveData<List<PlaylistEntity>> getPlaylistsLive();

    @Query("select * from PlaylistEntity where playlistID=:playlistID")
    LiveData<PlaylistEntity> getPlaylistLive(long playlistID);

    @Query("delete from PlaylistEntity")
    void deleteAll();

    @Query("delete from PlaylistEntity where playlistID=:playlistID")
    void deletePlaylist(long playlistID);

    @Query("select * from PlaylistEntity where playlistName like:playlistName")
    List<PlaylistEntity> getPlaylist(String playlistName);

    @Query("select * from PlaylistEntity where playlistName like:searchQuery LIMIT :limits OFFSET :offsets")
    List<PlaylistEntity> getPlaylistList(String searchQuery, int limits, int offsets);
}
