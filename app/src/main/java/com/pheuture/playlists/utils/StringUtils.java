package com.pheuture.playlists.utils;

import android.text.TextUtils;

/**
 * creator: Shashank
 * date: 27-Dec-18.
 */
public final class StringUtils {

    private StringUtils() {
        throw new IllegalStateException("utility class should not be instantiated ");
    }

    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0 || TextUtils.getTrimmedLength(text) == 0;
    }

    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0 || TextUtils.getTrimmedLength(text) == 0;
    }

    public static String format (String text) {
        return text + "            "; // 12 spaces
    }
}
