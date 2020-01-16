package com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import com.pheuture.playlists.datasource.local.media_handler.queue.QueueMediaEntity;

import java.util.List;

/**
 * creator: Shashank
 * date: 08-Jan-19.
 */

@Dao
public interface PlaylistMediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PlaylistMediaEntity playlistMediaEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PlaylistMediaEntity> playlistMediaEntities);

    @Query("select * from PlaylistMediaEntity where playlistID=:playlistID")
    LiveData<List<PlaylistMediaEntity>> getPlaylistMediaLive(long playlistID);

    @Query("delete from PlaylistMediaEntity where playlistID=:playlistID and mediaID=:mediaID")
    void deleteMediaFromPlaylist(long playlistID, long mediaID);

    @Query("delete from PlaylistMediaEntity where playlistID=:playlistID")
    void deleteAllMediaFromPlaylist(long playlistID);

    @Query("delete from PlaylistMediaEntity")
    void deleteAll();

    @Query("select * from PlaylistMediaEntity where playlistID=:playlistID and mediaID=:mediaID")
    PlaylistMediaEntity getPlaylistMedia(long playlistID, long mediaID);

    @Query("select * from PlaylistMediaEntity where playlistID=:playlistID")
    List<PlaylistMediaEntity> getPlaylistMediaEntities(long playlistID);



    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select * from PlaylistMediaEntity where playlistID=:playlistID")
    List<PlaylistMediaEntity> getPlaylistMediaMediaEntities(long playlistID);




    @Query("select * from PlaylistMediaEntity where playlistID=:playlistID and mediaTitle like:mediaTitle limit:limit offset:offset")
    List<PlaylistMediaEntity> getPlaylistMediaEntities(long playlistID, String mediaTitle, int limit, int offset);

    @Query("select * from PlaylistMediaEntity where playlistID=:playlistID limit:limit offset:offset")
    List<PlaylistMediaEntity> getPlaylistMediaEntities(long playlistID, int limit, int offset);
}
