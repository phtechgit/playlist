package com.pheuture.playlists.base.utils;

import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.images.Size;

public final class DataBindingAdapters {

    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        if (imageUrl==null || imageUrl.length()==0){
            return;
        }

        Glide.with(view.getContext())
                .load(imageUrl)
                .override(160,90)
                .into(view);
    }
}
