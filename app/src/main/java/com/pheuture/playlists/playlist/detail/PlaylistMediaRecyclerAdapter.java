package com.pheuture.playlists.playlist.detail;

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
import com.pheuture.playlists.databinding.ItemMediaBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class PlaylistMediaRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = PlaylistMediaRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<PlaylistMediaEntity> oldList;
    private RecyclerViewClickListener recyclerViewClickListener;

    PlaylistMediaRecyclerAdapter(PlaylistDetailFragment context) {
        this.mContext = context.getContext();
        this.recyclerViewClickListener = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_media, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerHOlder, int position) {
        if (position == RecyclerView.NO_POSITION){
            return;
        }
        MyViewHolder holder = (MyViewHolder) recyclerHOlder;

        PlaylistMediaEntity model = oldList.get(position);
        holder.binding.setMediaTitle(model.getMediaTitle());
        holder.binding.setMediaDescription(model.getMediaDescription());
        holder.binding.setMediaThumbnail(model.getMediaThumbnail());
        holder.binding.setMediaDuration(model.getFormattedPlayDuration());
    }

    void setData(List<PlaylistMediaEntity> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(oldList, newList), true);
        oldList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void removeItem(int position) {
        oldList.remove(position);
        notifyItemRemoved(position);
    }

    class DiffCallBack extends DiffUtil.Callback{
        private List<PlaylistMediaEntity> oldList;
        private List<PlaylistMediaEntity> newList;

        public DiffCallBack(List<PlaylistMediaEntity> oldList, List<PlaylistMediaEntity> newList) {
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
            PlaylistMediaEntity oldModel = oldList.get(oldItemPosition);
            PlaylistMediaEntity newModel = newList.get(newItemPosition);

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
                    bundle.putInt(Constants.ARG_PARAM2, 1);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

                    recyclerViewClickListener.onRecyclerViewHolderClick(bundle);
                }
            });

            /*binding.imageViewThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION){
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, pos);
                    bundle.putInt(Constants.ARG_PARAM2, 1);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

                    recyclerViewInterface.onRecyclerViewHolderClick(bundle);
                }
            });*/

            binding.imageViewRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION){
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, pos);
                    bundle.putInt(Constants.ARG_PARAM2, 2);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));

                    recyclerViewClickListener.onRecyclerViewHolderClick(bundle);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return oldList == null ? 0 : oldList.size();
    }
}
