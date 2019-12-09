package com.pheuture.playlists.playlists.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ItemVideoBinding;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;

import java.util.List;

public class PlaylistVideosRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = PlaylistVideosRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<VideoEntity> oldList;
    private LinearLayoutManager layoutManager;
    private RecyclerViewInterface recyclerViewInterface;
    private PlayerView playerView;
    private int playerPosition = RecyclerView.NO_POSITION;

    PlaylistVideosRecyclerAdapter(Context context, LinearLayoutManager layoutManager, PlayerView playerView) {
        this.mContext = context;
        this.layoutManager = layoutManager;
        this.recyclerViewInterface = (RecyclerViewInterface) context;
        this.playerView = playerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerHOlder, int position) {
        Logger.e(TAG, "onBindViewHolder: " + position);
        if (position == RecyclerView.NO_POSITION){
            return;
        }

        MyViewHolder holder = (MyViewHolder) recyclerHOlder;

        VideoEntity model = oldList.get(position);
        holder.binding.setModel(model);

        if (playerPosition == position){
            //add player to the frameLayout of current position
            setPlayer(holder.binding);

        } else {
            //remove player from the frameLayout of current position
            removePlayer(holder.binding);
        }
    }

    private void removePlayer(ItemVideoBinding binding) {
        /*binding.frameLayout.removeAllViews();*/
        ViewGroup parent = (ViewGroup) playerView.getParent();
        if (parent != null) {
            int index = parent.indexOfChild(playerView);
            if (index >= 0) {
                parent.removeViewAt(index);
            }
        }
        binding.imageViewThumbnail.setVisibility(View.VISIBLE);
    }

    private void setPlayer(ItemVideoBinding binding) {
        playerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ViewGroup parent = (ViewGroup) playerView.getParent();
        if (parent != null) {
            int index = parent.indexOfChild(playerView);
            if (index >= 0) {
                parent.removeViewAt(index);
            }
        }

        binding.frameLayout.addView(playerView);
        binding.imageViewThumbnail.setVisibility(View.GONE);
    }

    void setData(List<VideoEntity> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(oldList, newList));
        diffResult.dispatchUpdatesTo(this);
        oldList = newList;
    }

    /*public void setPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playWhenReady) {
         if (playerPosition == RecyclerView.NO_POSITION){
             playerPosition = 0;
         }

         notifyItemChanged(playerPosition);
         layoutManager.scrollToPosition(playerPosition);
        }
    }*/

    public void setPlayerPosition(int newPlayerPosition) {
        int oldPlayerPosition = this.playerPosition;
        this.playerPosition = newPlayerPosition;

        notifyItemChanged(oldPlayerPosition);
        notifyItemChanged(newPlayerPosition);

        layoutManager.scrollToPosition(newPlayerPosition);
    }

    class DiffCallBack extends DiffUtil.Callback{
        private List<VideoEntity> oldList;
        private List<VideoEntity> newList;

        public DiffCallBack(List<VideoEntity> oldList, List<VideoEntity> newList) {
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
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            VideoEntity oldModel = oldList.get(oldItemPosition);
            VideoEntity newModel = newList.get(newItemPosition);

            if (contentsDifferent(oldModel.getVideoUrl(), newModel.getVideoUrl())){
                return false;
            }
            if (contentsDifferent(oldModel.getVideoName(), newModel.getVideoName())){
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
        return oldData == null || oldData.equals(newData);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemVideoBinding binding;

        MyViewHolder(@NonNull ItemVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.imageViewAdd.setVisibility(View.GONE);

            /*binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, adapterPosition);
                    bundle.putParcelable(Constants.ARG_PARAM2, oldList.get(adapterPosition));

                    recyclerViewInterface.onRecyclerViewItemClick(bundle);
                }
            });*/

            binding.imageViewThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION){
                        return;
                    }

                    int tempPosition = playerPosition;
                    playerPosition = pos;
                    notifyItemChanged(tempPosition);
                    notifyItemChanged(playerPosition);
                }
            });

            binding.imageViewAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION){
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, pos);
                    bundle.putParcelable(Constants.ARG_PARAM2, oldList.get(pos));

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
