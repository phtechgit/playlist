package com.pheuture.playlists.utils;

public final class Url {
    private Url() {
        throw new IllegalStateException("utility class should not be instantiated ");
    }

    public static final String BASE_URL = "http://111.118.180.237/testing_d/praveen/playlist/";
    public static final String REQUEST_OTP = BASE_URL + "sendOtp.php";
    public static final String VERIFY_OTP = BASE_URL + "varify_Otp.php";
    public static final String UPDATE_USER_DETAIL = BASE_URL + "create_user.php";

    public static final String PLAYLIST_LIST = BASE_URL + "search_playlist.php";
    public static final String PLAYLIST_CREATE = "create_playlist.php";
    public static final String PLAYLIST_DELETE = "delete_playlist.php";
    public static final String PLAYLIST_VIDEOS = BASE_URL + "playlist_videos.php";
    public static final String PLAYLIST_MEDIA_ADD = "addVideo_playlist.php";
    public static final String PLAYLIST_MEDIA_REMOVE = "delete_playlistvideo.php";
    public static final String MEDIA_TRENDING = BASE_URL + "list.php";
    public static final String MEDIA_UPLOAD = "addVideo.php";

}
