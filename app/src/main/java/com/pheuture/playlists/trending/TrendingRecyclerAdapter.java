package com.pheuture.playlists.trending;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ItemMediaBinding;
import com.pheuture.playlists.datasource.local.media_handler.MediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.constants.Constants;
import com.pheuture.playlists.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TrendingRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final String TAG = TrendingRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<MediaEntity> masterList;
    private List<MediaEntity> filteredList;
    private RecyclerViewClickListener recyclerViewClickListener;

    TrendingRecyclerAdapter(TrendingFragment context) {
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
        MyViewHolder holder = (MyViewHolder) recyclerHOlder;

        MediaEntity model = filteredList.get(position);
        holder.binding.setMediaTitle(model.getMediaTitle());
        holder.binding.setMediaDescription(model.getMovieName());
        holder.binding.setMediaThumbnail(model.getMediaThumbnail());
        holder.binding.setMediaDuration(model.getFormattedPlayDuration());
    }

    void setData(List<MediaEntity> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TrendingDiffUtil(filteredList, newList), true);
        masterList = new ArrayList<>(newList);
        filteredList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemMediaBinding binding;

        MyViewHolder(@NonNull ItemMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.imageViewAdd.setVisibility(View.GONE);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION){
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.ARG_PARAM1, pos);
                    bundle.putParcelable(Constants.ARG_PARAM2, masterList.get(pos));

                    recyclerViewClickListener.onRecyclerViewHolderClick(bundle);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredList == null ? 0 : filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchString = charSequence.toString().toLowerCase();

                List<MediaEntity> newFilteredList = new ArrayList<>();

                if (searchString.isEmpty()) {
                    newFilteredList.addAll(masterList);

                } else {
                    for (int i = 0; i < masterList.size(); i++) {
                        MediaEntity row = masterList.get(i);

                        if (!StringUtils.isEmpty(row.getMediaTitle()) && row.getMediaTitle().toLowerCase().contains(searchString)) {
                            newFilteredList.add(row);
                        } else if (!StringUtils.isEmpty(row.getMovieName()) && row.getMovieName().toLowerCase().contains(searchString)) {
                            newFilteredList.add(row);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = newFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                List<MediaEntity> newList = (ArrayList<MediaEntity>) filterResults.values;
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TrendingDiffUtil(filteredList, newList), true);
                filteredList = new ArrayList<>(newList);
                diffResult.dispatchUpdatesTo(TrendingRecyclerAdapter.this);
            }
        };
    }
}
