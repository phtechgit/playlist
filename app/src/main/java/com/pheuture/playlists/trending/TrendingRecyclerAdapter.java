package com.pheuture.playlists.trending;

import android.content.Context;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ItemMediaBinding;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class TrendingRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = TrendingRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<MediaEntity> oldList;
    private RecyclerViewInterface recyclerViewInterface;
    private int playerPosition = RecyclerView.NO_POSITION;

    TrendingRecyclerAdapter(TrendingFragment context) {
        this.mContext = context.getContext();
        this.recyclerViewInterface = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_media, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerHOlder, int position) {
        MyViewHolder holder = (MyViewHolder) recyclerHOlder;

        MediaEntity model = oldList.get(position);
        holder.binding.setMediaTitle(model.getVideoTitle());
        holder.binding.setMediaDescription(model.getVideoDescription());
        holder.binding.setMediaThumbnail(model.getVideoThumbnail());
        holder.binding.setMediaDuration(model.getFormattedPlayDuration());
    }

    void setData(List<MediaEntity> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(oldList, newList),
                false);
        oldList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    class DiffCallBack extends DiffUtil.Callback{
        private List<MediaEntity> oldList;
        private List<MediaEntity> newList;

        public DiffCallBack(List<MediaEntity> oldList, List<MediaEntity> newList) {
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
            return oldList.get(oldItemPosition).getMediaID() == newList.get(newItemPosition).getMediaID();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            MediaEntity oldModel = oldList.get(oldItemPosition);
            MediaEntity newModel = newList.get(newItemPosition);

            if (contentsDifferent(oldModel.getVideoName(), newModel.getVideoName())){
                return false;
            }
            if (contentsDifferent(oldModel.getVideoDescription(), newModel.getVideoDescription())){
                return false;
            }
            if (contentsDifferent(oldModel.getVideoThumbnail(), newModel.getVideoThumbnail())){
                return false;
            }
            if (contentsDifferent(oldModel.getVideoUrl(), newModel.getVideoUrl())){
                return false;
            }
            if (contentsDifferent(String.valueOf(oldModel.getPostDate()), String.valueOf(newModel.getPostDate()))){
                return false;
            }
            if (contentsDifferent(String.valueOf(oldModel.getStatus()), String.valueOf(newModel.getStatus()))){
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
        private ItemMediaBinding binding;

        MyViewHolder(@NonNull ItemMediaBinding binding) {
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
