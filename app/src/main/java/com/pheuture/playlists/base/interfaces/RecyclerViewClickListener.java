package com.pheuture.playlists.base.interfaces;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.pheuture.playlists.databinding.ItemMediaBinding;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public interface RecyclerViewClickListener {
    void onRecyclerViewHolderClick(RecyclerView.ViewHolder viewHolder, Bundle bundle);

    void onRecyclerViewHolderLongClick(Bundle bundle);

    interface ClickType{
        int SELECT = 1;
        int ADD = 2;
        int REMOVE = 3;
        int DRAG = 4;
    }
}
