package com.pheuture.playlists.interfaces;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface RecyclerViewClickListener {
    void onRecyclerViewHolderClick(Bundle bundle);
    void onRecyclerViewHolderLongClick(Bundle bundle);
}
