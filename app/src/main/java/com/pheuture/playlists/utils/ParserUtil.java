package com.pheuture.playlists.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ParserUtil {
    private static Gson gson;

    public static Gson getInstance(){
        return gson!=null ? gson : new GsonBuilder().create();
    }
}
