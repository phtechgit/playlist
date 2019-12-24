package com.pheuture.playlists.utils;

public final class Url {
    private Url() {
        throw new IllegalStateException("utility class should not be instantiated ");
    }

    public static final String REQUEST_OTP = "http://111.118.180.237/sendSms/sendSms.php";
    public static final String VERIFY_OTP = "http://111.118.180.237/testing_d/praveen/playlist/varify_Otp.php";
    public static final String PLAYLIST_LIST = "http://111.118.180.237/testing_d/praveen/playlist/search_playlist.php";
    public static final String PLAYLIST_CREATE = "http://111.118.180.237/testing_d/praveen/playlist/create_playlist.php";
    public static final String PLAYLIST_DELETE = "http://111.118.180.237/testing_d/praveen/playlist/delete_playlist.php";
    public static final String PLAYLIST_VIDEOS = "http://111.118.180.237/testing_d/praveen/playlist/playlist_videos.php";
    public static final String PLAYLIST_MEDIA_ADD = "http://111.118.180.237/testing_d/praveen/playlist/addVideo_playlist.php";
    public static final String PLAYLIST_MEDIA_REMOVE = "http://111.118.180.237/testing_d/praveen/playlist/delete_playlistvideo.php";
    public static final String MEDIA_TRENDING = "http://111.118.180.237/testing_d/praveen/playlist/list.php";
    public static final String MEDIA_UPLOAD = "http://111.118.180.237/testing_d/praveen/playlist/addVideo.php";

}
