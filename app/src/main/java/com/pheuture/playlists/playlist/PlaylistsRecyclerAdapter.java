package com.pheuture.playlists.playlist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ItemPlaylistBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.constants.Constants;
import java.util.ArrayList;
import java.util.List;

public class PlaylistsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements RecyclerViewClickListener.ClickType{
    private static final String TAG = PlaylistsRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<PlaylistEntity> oldList;
    private RecyclerViewClickListener recyclerViewClickListener;

    PlaylistsRecyclerAdapter(PlaylistFragment context) {
        this.mContext = context.getContext();
        this.recyclerViewClickListener = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerHOlder, int position) {
        MyViewHolder holder = (MyViewHolder) recyclerHOlder;

        PlaylistEntity model = oldList.get(position);
        holder.binding.setModel(model);
        holder.binding.setPosition(position);
        if (model.getPlaylistID() == RecyclerView.NO_ID){
            holder.binding.imageViewThumbnail.setImageResource(R.drawable.ic_plus_light);
            holder.binding.imageViewRemove.setVisibility(View.GONE);
        } else {
            holder.binding.imageViewThumbnail.setImageResource(R.drawable.ic_music);
            holder.binding.imageViewRemove.setVisibility(View.VISIBLE);
        }

    }

    void setData(List<PlaylistEntity> newData) {
        List<PlaylistEntity> newList = new ArrayList<>(newData);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PlaylistDiffCallBack(oldList, newList), true);
        oldList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemPlaylistBinding binding;

        MyViewHolder(@NonNull ItemPlaylistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, adapterPosition);
                    bundle.putInt(Constants.ARG_PARAM2, SELECT);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(adapterPosition));
                    recyclerViewClickListener.onRecyclerViewHolderClick(this, bundle);
                }
            });

            binding.imageViewRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, adapterPosition);
                    bundle.putInt(Constants.ARG_PARAM2, REMOVE);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(adapterPosition));
                    recyclerViewClickListener.onRecyclerViewHolderClick(this, bundle);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return oldList == null ? 0 : oldList.size();
    }
}
