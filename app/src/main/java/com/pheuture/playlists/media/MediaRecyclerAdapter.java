package com.pheuture.playlists.media;

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
import com.pheuture.playlists.datasource.local.media_handler.MediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.constants.Constants;

import java.util.ArrayList;
import java.util.List;

public class MediaRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = MediaRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<MediaEntity> oldList;
    private RecyclerViewClickListener recyclerViewClickListener;

    MediaRecyclerAdapter(MediaFragment context) {
        this.mContext = context.getContext();
        this.recyclerViewClickListener = context;
    }

    @Override
    public long getItemId(int position) {
        return oldList.get(position).getMediaID();
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
        holder.binding.setMediaTitle(model.getMediaTitle());
        holder.binding.setMediaDescription(model.getMovieName());
        holder.binding.setMediaThumbnail(model.getMediaThumbnail());
        holder.binding.setMediaDuration(model.getFormattedPlayDuration());
    }

    void setData(List<MediaEntity> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MediaDiffCallBack(oldList, newList),
                true);
        oldList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemMediaBinding binding;

        MyViewHolder(@NonNull ItemMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.imageViewAdd.setVisibility(View.VISIBLE);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
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
                    bundle.putInt(Constants.ARG_PARAM1, 1);
                    bundle.putInt(Constants.ARG_PARAM2, pos);
                    bundle.putParcelable(Constants.ARG_PARAM3, oldList.get(pos));
                    recyclerViewInterface.onRecyclerViewHolderClick(bundle);
                }
            });*/

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
