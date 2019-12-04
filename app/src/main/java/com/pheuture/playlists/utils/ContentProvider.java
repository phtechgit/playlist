package com.pheuture.playlists.utils;

import android.content.ContentResolver;
import android.content.Context;

public class ContentProvider {

    private static ContentResolver contentResolver;

    public static synchronized ContentResolver getContentResolver(Context context) {
        if (contentResolver == null) {
            contentResolver = context.getContentResolver();
        }
        return contentResolver;
    }
}
