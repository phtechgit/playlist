package com.pheuture.playlists.queue;

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
import com.pheuture.playlists.databinding.ItemQueueNotPlayingMediaBinding;
import com.pheuture.playlists.databinding.ItemQueuePlayingMediaBinding;
import com.pheuture.playlists.datasource.local.media_handler.queue.QueueMediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MediaQueueRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = MediaQueueRecyclerAdapter.class.getSimpleName();
    private int PLAYING_MEDIA = 1;
    private int NOT_PLAYING_MEDIA = 2;
    private Context mContext;
    private List<QueueMediaEntity> oldList;
    private RecyclerViewClickListener recyclerViewClickListener;

    public interface ClickType {
        int SELECT = 1;
        int REMOVE = 2;
    }

    public MediaQueueRecyclerAdapter(Context context, RecyclerViewClickListener listener) {
        this.mContext = context;
        this.recyclerViewClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (oldList.get(position).getState() == QueueMediaEntity.QueueMediaState.PLAYING){
            return PLAYING_MEDIA;
        } else {
            return NOT_PLAYING_MEDIA;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PLAYING_MEDIA) {
            return new PlayingMediaViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    R.layout.item_queue_playing_media, parent, false));
        } else {
            return new NotPlayingMediaViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    R.layout.item_queue_not_playing_media, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerHolder, int position) {
        if (position == RecyclerView.NO_POSITION){
            return;
        }
        if (getItemViewType(position) == PLAYING_MEDIA){
            ((PlayingMediaViewHolder) recyclerHolder).setData(oldList.get(position));
        } else {
            ((NotPlayingMediaViewHolder) recyclerHolder).setData(oldList.get(position));
        }
    }

    public void setData(List<QueueMediaEntity> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(oldList, newList), true);
        oldList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public List<QueueMediaEntity> getDataList() {
        return oldList;
    }

    class DiffCallBack extends DiffUtil.Callback{
        private List<QueueMediaEntity> oldList;
        private List<QueueMediaEntity> newList;

        public DiffCallBack(List<QueueMediaEntity> oldList, List<QueueMediaEntity> newList) {
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
            QueueMediaEntity oldModel = oldList.get(oldItemPosition);
            QueueMediaEntity newModel = newList.get(newItemPosition);

            if (contentsDifferent(oldModel.getMediaUrl(), newModel.getMediaUrl())){
                return false;
            }
            if (contentsDifferent(oldModel.getMediaName(), newModel.getMediaName())){
                return false;
            }
            if (contentsDifferent(oldModel.getMediaDescription(), newModel.getMediaDescription())){
                return false;
            }
            if (contentsDifferent(oldModel.getMediaThumbnail(), newModel.getMediaThumbnail())){
                return false;
            }
            if (contentsDifferent(String.valueOf(oldModel.getState()), String.valueOf(newModel.getState()))){
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

    public class PlayingMediaViewHolder extends RecyclerView.ViewHolder {
        private ItemQueuePlayingMediaBinding binding;

        PlayingMediaViewHolder(@NonNull ItemQueuePlayingMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void setData(QueueMediaEntity model) {
            binding.setMediaTitle(model.getMediaTitle());
            binding.setMediaDescription(model.getMediaDescription());
            binding.setMediaThumbnail(model.getMediaThumbnail());
            binding.setMediaDuration(model.getFormattedPlayDuration());
        }
    }

    public class NotPlayingMediaViewHolder extends RecyclerView.ViewHolder {
        private ItemQueueNotPlayingMediaBinding binding;

        NotPlayingMediaViewHolder(@NonNull ItemQueueNotPlayingMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.imageViewRemove.setVisibility(View.VISIBLE);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION){
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, pos);
                    bundle.putInt(Constants.ARG_PARAM2, ClickType.SELECT);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

                    recyclerViewClickListener.onRecyclerViewItemClick(bundle);
                }
            });

            binding.imageViewRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION){
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, pos);
                    bundle.putInt(Constants.ARG_PARAM2, ClickType.REMOVE);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

                    recyclerViewClickListener.onRecyclerViewItemClick(bundle);
                }
            });
        }

        public void setData(QueueMediaEntity model) {
            binding.setMediaTitle(model.getMediaTitle());
            binding.setMediaDescription(model.getMediaDescription());
            binding.setMediaThumbnail(model.getMediaThumbnail());
            binding.setMediaDuration(model.getFormattedPlayDuration());
        }
    }

    @Override
    public int getItemCount() {
        return oldList == null ? 0 : oldList.size();
    }
}
