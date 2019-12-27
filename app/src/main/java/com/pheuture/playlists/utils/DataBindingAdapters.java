package com.pheuture.playlists.utils;

import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;

public final class DataBindingAdapters {

    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        if (imageUrl==null || imageUrl.length()==0){
            return;
        }
        Glide.with(view.getContext())
                .load(imageUrl)
                .into(view);
    }
}
