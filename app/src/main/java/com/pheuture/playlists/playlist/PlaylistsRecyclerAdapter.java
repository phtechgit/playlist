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
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = PlaylistsRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<PlaylistEntity> oldList;
    private RecyclerViewInterface recyclerViewInterface;

    PlaylistsRecyclerAdapter(PlaylistFragment context) {
        this.mContext = context.getContext();
        this.recyclerViewInterface = context;
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
        if (position == 0){
            holder.binding.imageViewThumbnail.setImageResource(R.drawable.ic_plus_light);
            holder.binding.imageViewRemove.setVisibility(View.GONE);
        } else {
            holder.binding.imageViewThumbnail.setImageResource(R.drawable.ic_music);
            holder.binding.imageViewRemove.setVisibility(View.VISIBLE);
        }

    }

    void setData(List<PlaylistEntity> newData) {
        List<PlaylistEntity> newList = new ArrayList<>(newData);
        PlaylistEntity addNewPlaylistModel = new PlaylistEntity();
        addNewPlaylistModel.setPlaylistID(RecyclerView.NO_ID);
        addNewPlaylistModel.setPlaylistName("Create playlist");

        newList.add(0, addNewPlaylistModel);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(oldList, newList));
        oldList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    class DiffCallBack extends DiffUtil.Callback{
        private List<PlaylistEntity> oldList;
        private List<PlaylistEntity> newList;

        public DiffCallBack(List<PlaylistEntity> oldList, List<PlaylistEntity> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList == null ? 0 : oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList == null ? 0 : newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getPlaylistID() == newList.get(newItemPosition).getPlaylistID();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            PlaylistEntity oldModel = oldList.get(oldItemPosition);
            PlaylistEntity newModel = newList.get(newItemPosition);

            if (contentsDifferent(oldModel.getPlaylistName(), newModel.getPlaylistName())){
                return false;
            }
            if (contentsDifferent(String.valueOf(oldModel.getSongsCount()), String.valueOf(newModel.getSongsCount()))){
                return false;
            }
            if (contentsDifferent(String.valueOf(oldModel.getPlayDuration()), String.valueOf(newModel.getPlayDuration()))){
                return false;
            }
            if (contentsDifferent(String.valueOf(oldModel.getCreatedOn()), String.valueOf(newModel.getCreatedOn()))){
                return false;
            }
            return true;
        }
    }

    private boolean contentsDifferent(String oldData, String newData) {
        if (oldData == null && newData != null){
            return false;
        }
        if (oldData != null && newData == null){
            return false;
        }
        if (oldData == null){
            return false;
        }
        return !oldData.equals(newData);
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
                    bundle.putInt(Constants.ARG_PARAM2, 1);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(adapterPosition));
                    recyclerViewInterface.onRecyclerViewItemClick(bundle);
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
                    bundle.putInt(Constants.ARG_PARAM2, 2);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(adapterPosition));
                    recyclerViewInterface.onRecyclerViewItemClick(bundle);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return oldList == null ? 0 : oldList.size();
    }
}