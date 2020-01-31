package com.pheuture.playlists.playlist;

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
public interface PlaylistLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PlaylistEntity playlistEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PlaylistEntity> playlistEntities);

    @Query("select * from PlaylistEntity where playlistID=:playlistID")
    LiveData<PlaylistEntity> getPlaylistLive(long playlistID);

    @Query("delete from PlaylistEntity")
    void deleteAll();

    @Query("delete from PlaylistEntity where playlistID=:playlistID")
    void deletePlaylist(long playlistID);

    @Query("select * from PlaylistEntity where playlistName =:playlistName")
    List<PlaylistEntity> getPlaylistEntities(String playlistName);

    @Query("select * from PlaylistEntity where playlistName like:playlistName order by modifiedOn desc limit:limit offset:offset")
    List<PlaylistEntity> getPlaylistEntities(String playlistName, int limit, int offset);

    @Query("select * from PlaylistEntity order by modifiedOn desc limit:limit offset:offset")
    List<PlaylistEntity> getPlaylistEntities(int limit, int offset);
}
