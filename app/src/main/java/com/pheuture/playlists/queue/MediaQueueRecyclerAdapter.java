package com.pheuture.playlists.queue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ItemQueueNotPlayingMediaBinding;
import com.pheuture.playlists.databinding.ItemQueuePlayingMediaBinding;
import com.pheuture.playlists.base.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.Logger;
import com.pheuture.playlists.base.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MediaQueueRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements RecyclerViewClickListener.ClickType {
    private static final String TAG = MediaQueueRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private int PLAYING_MEDIA = 1;
    private int NOT_PLAYING_MEDIA = 2;
    private List<QueueMediaEntity> oldList;
    private RecyclerViewClickListener recyclerViewClickListener;

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
            ((PlayingMediaViewHolder) recyclerHolder).setData(oldList.get(position), null);
        } else {
            ((NotPlayingMediaViewHolder) recyclerHolder).setData(oldList.get(position), null);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerHolder, int position, @NotNull List<Object> payloads) {
        if (position == RecyclerView.NO_POSITION){
            return;
        }
        if (getItemViewType(position) == PLAYING_MEDIA){
            ((PlayingMediaViewHolder) recyclerHolder).setData(oldList.get(position), payloads);
        } else {
            ((NotPlayingMediaViewHolder) recyclerHolder).setData(oldList.get(position), payloads);
        }
    }

    public void updateData(final List<QueueMediaEntity> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MediaQueueDiffUtil(oldList, newList),
                true);
        oldList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public class PlayingMediaViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
        private ItemQueuePlayingMediaBinding binding;

        @SuppressLint("ClickableViewAccessibility")
        PlayingMediaViewHolder(@NonNull ItemQueuePlayingMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.imageViewDragHandle.setOnTouchListener(this);
            this.binding.imageViewRemove.setVisibility(View.GONE);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION){
                return true;
            }
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.ARG_PARAM1, pos);
            bundle.putInt(Constants.ARG_PARAM2, DRAG);
            bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

            recyclerViewClickListener.onRecyclerViewHolderClick(this, bundle);
            return false;
        }

        public void setData(QueueMediaEntity model, List<Object> payloads) {
            if (payloads == null || payloads.isEmpty()) {
                binding.setMediaTitle(model.getMediaTitle());
                binding.setMediaDescription(model.getMovieName());
                binding.setMediaThumbnail(model.getMediaThumbnail());
                binding.setMediaDuration(model.getFormattedPlayDuration());
            } else {
                try {
                    Bundle bundle = (Bundle) payloads.get(0);
                    String mediaTitle = bundle.getString(QueueMediaEntity.MediaColumns.MEDIA_TITLE);
                    String mediaMovieName = bundle.getString(QueueMediaEntity.MediaColumns.MEDIA_MOVIE_NAME);
                    String mediaThumbnail = bundle.getString(QueueMediaEntity.MediaColumns.MEDIA_THUMBNAIL);
                    String mediaPlayDuration = bundle.getString(QueueMediaEntity.MediaColumns.PLAY_DURATION);
                    int mediaState = bundle.getInt(QueueMediaEntity.QueueMediaColumns.STATE);
                    int mediaProgress = bundle.getInt(QueueMediaEntity.QueueMediaColumns.PROGRESS);

                    if (!StringUtils.isEmpty(mediaTitle)) {
                        binding.setMediaTitle(model.getMediaTitle());
                    }
                    if (!StringUtils.isEmpty(mediaMovieName)) {
                        binding.setMediaDescription(model.getMovieName());
                    }
                    if (!StringUtils.isEmpty(mediaThumbnail)) {
                        binding.setMediaThumbnail(model.getMediaThumbnail());
                    }
                    if (!StringUtils.isEmpty(mediaPlayDuration)) {
                        binding.setMediaDuration(model.getFormattedPlayDuration());
                    }
                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }
        }
    }

    public class NotPlayingMediaViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
        private ItemQueueNotPlayingMediaBinding binding;

        @SuppressLint("ClickableViewAccessibility")
        NotPlayingMediaViewHolder(@NonNull ItemQueueNotPlayingMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.imageViewDragHandle.setOnTouchListener(this);
            this.binding.imageViewRemove.setVisibility(View.VISIBLE);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION){
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, pos);
                    bundle.putInt(Constants.ARG_PARAM2, SELECT);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

                    recyclerViewClickListener.onRecyclerViewHolderClick(NotPlayingMediaViewHolder.this, bundle);
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
                    bundle.putInt(Constants.ARG_PARAM2, REMOVE);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

                    recyclerViewClickListener.onRecyclerViewHolderClick(NotPlayingMediaViewHolder.this, bundle);
                }
            });
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION){
                return true;
            }
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.ARG_PARAM1, pos);
            bundle.putInt(Constants.ARG_PARAM2, DRAG);
            bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

            recyclerViewClickListener.onRecyclerViewHolderClick(this, bundle);
            return false;
        }

        public void setData(QueueMediaEntity model, List<Object> payloads) {
            if (payloads == null || payloads.isEmpty()) {
                binding.setMediaTitle(model.getMediaTitle());
                binding.setMediaDescription(model.getMovieName());
                binding.setMediaThumbnail(model.getMediaThumbnail());
                binding.setMediaDuration(model.getFormattedPlayDuration());

            } else {
                try {
                    Bundle bundle = (Bundle) payloads.get(0);
                    String mediaTitle = bundle.getString(QueueMediaEntity.MediaColumns.MEDIA_TITLE);
                    String mediaMovieName = bundle.getString(QueueMediaEntity.MediaColumns.MEDIA_MOVIE_NAME);
                    String mediaThumbnail = bundle.getString(QueueMediaEntity.MediaColumns.MEDIA_THUMBNAIL);
                    String mediaPlayDuration = bundle.getString(QueueMediaEntity.MediaColumns.PLAY_DURATION);
                    int mediaState = bundle.getInt(QueueMediaEntity.QueueMediaColumns.STATE);
                    int mediaProgress = bundle.getInt(QueueMediaEntity.QueueMediaColumns.PROGRESS);

                    if (!StringUtils.isEmpty(mediaTitle)) {
                        binding.setMediaTitle(model.getMediaTitle());
                    }
                    if (!StringUtils.isEmpty(mediaMovieName)) {
                        binding.setMediaDescription(model.getMovieName());
                    }
                    if (!StringUtils.isEmpty(mediaThumbnail)) {
                        binding.setMediaThumbnail(model.getMediaThumbnail());
                    }
                    if (!StringUtils.isEmpty(mediaPlayDuration)) {
                        binding.setMediaDuration(model.getFormattedPlayDuration());
                    }
                } catch (Exception e) {
                    Logger.e(TAG, e.toString());
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return oldList == null ? 0 : oldList.size();
    }
}
