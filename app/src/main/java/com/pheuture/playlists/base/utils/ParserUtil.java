package com.pheuture.playlists.base.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class ParserUtil {
    private static Gson gson;

    public static Gson getInstance(){
        return gson!=null ? gson : new GsonBuilder().create();
    }
}
