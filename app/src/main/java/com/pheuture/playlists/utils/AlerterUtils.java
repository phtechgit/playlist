package com.pheuture.playlists.utils;

import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

public class AlerterUtils {

    public static void showSnack(View view, String message){
        Snackbar mySnack = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        mySnack.show();
    }
}
