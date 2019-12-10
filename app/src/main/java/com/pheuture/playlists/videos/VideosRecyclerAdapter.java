package com.pheuture.playlists.videos;

import android.content.Context;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ItemVideoBinding;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VideosRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = VideosRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<VideoEntity> oldList;
    private RecyclerViewInterface recyclerViewInterface;

    VideosRecyclerAdapter(VideosFragment context) {
        this.mContext = context.getContext();
        this.recyclerViewInterface = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerHOlder, int position) {
        MyViewHolder holder = (MyViewHolder) recyclerHOlder;

        VideoEntity model = oldList.get(position);
        holder.binding.setModel(model);
    }

    void setData(List<VideoEntity> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(oldList, newList),
                true);
        diffResult.dispatchUpdatesTo(this);
        oldList = newList;
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
        return oldData == null || oldData.equals(newData);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemVideoBinding binding;

        MyViewHolder(@NonNull ItemVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

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
                    bundle.putInt(Constants.ARG_PARAM1, 1);
                    bundle.putInt(Constants.ARG_PARAM2, pos);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));
                    recyclerViewInterface.onRecyclerViewItemClick(bundle);
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
                    bundle.putInt(Constants.ARG_PARAM1, 2);
                    bundle.putInt(Constants.ARG_PARAM2, pos);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

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
