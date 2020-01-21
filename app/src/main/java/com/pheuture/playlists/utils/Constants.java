package com.pheuture.playlists.utils;

/**
 * creator: Shashank
 * date: 27-Dec-18.
 */
public final class Constants{

    private Constants() {
        throw new IllegalStateException("utility class should not be instantiated ");
    }

    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";
    public static final String ARG_PARAM4 = "param4";
    public static final String ARG_PARAM5 = "param5";

    public static final String DECORATOR_STARTING_VIEW = "decorator_starting_view";
    public static final String DECORATOR_ENDING_VIEW = "decorator_ending_view";

    public static final String USER = "user";
    public static final String OFFLINE_MEDIA_FOLDER = "offline_media";
    public static final String DOWNLOAD_PLAYLIST_MEDIA = "download_playlist_media";
    public static final String DOWNLOAD_USING_CELLULAR = "download_using_cellular";
    public static final String DOWNLOAD_WHILE_ROAMING = "download_while_roaming";
    public static final String CROSS_FADE_VALUE = "cross_fade_value";
    public static int CROSS_FADE_DEFAULT_VALUE = 1;

    public interface SnackBarActions {
        String SNACK_BAR_SHOW = "snack_bar_show";
        String SNACK_BAR_MESSAGE = "snack_bar_message";
        String SNACK_BAR_LENGTH = "snack_bar_length";
    }

}
