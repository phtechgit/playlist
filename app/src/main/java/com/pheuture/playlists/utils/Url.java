package com.pheuture.playlists.utils;

public final class Url {
    private Url() {
        throw new IllegalStateException("utility class should not be instantiated ");
    }

    public static final String PLAYLIST_LIST = "http://111.118.180.237/testing_d/praveen/playlist/search_playlist.php";
    public static final String PLAYLIST_CREATE = "http://111.118.180.237/testing_d/praveen/playlist/create_playlist.php";
    public static final String PLAYLIST_VIDEOS = "http://111.118.180.237/testing_d/praveen/playlist/playlist_videos.php";
    public static final String PLAYLIST_VIDEO_ADD = "http://111.118.180.237/testing_d/praveen/playlist/addVideo_playlist.php";
    public static final String VIDEOS_TRENDING = "http://111.118.180.237/testing_d/praveen/playlist/list.php";

}
