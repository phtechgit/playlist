package com.pheuture.playlists.base.utils;

import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

public final class AlerterUtils {

    public static void showSnack(View view, String message){
        Snackbar mySnack = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        mySnack.show();
    }
}
