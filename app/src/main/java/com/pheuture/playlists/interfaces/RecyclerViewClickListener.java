package com.pheuture.playlists.interfaces;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

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
