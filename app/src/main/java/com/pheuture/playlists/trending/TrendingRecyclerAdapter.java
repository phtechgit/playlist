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
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TrendingRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final String TAG = TrendingRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private List<MediaEntity> masterList;
    private List<MediaEntity> filteredList;
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

        MediaEntity model = masterList.get(position);
        holder.binding.setMediaTitle(model.getMediaTitle());
        holder.binding.setMediaDescription(model.getMediaDescription());
        holder.binding.setMediaThumbnail(model.getMediaThumbnail());
        holder.binding.setMediaDuration(model.getFormattedPlayDuration());
    }

    void setData(List<MediaEntity> newList) {
        masterList = new ArrayList<>(newList);
        /*filteredList = new ArrayList<>(masterList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(filteredList, newList),
                false);
        masterList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);*/
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

            if (contentsDifferent(oldModel.getMediaName(), newModel.getMediaName())){
                return false;
            }
            if (contentsDifferent(oldModel.getMediaDescription(), newModel.getMediaDescription())){
                return false;
            }
            if (contentsDifferent(oldModel.getMediaThumbnail(), newModel.getMediaThumbnail())){
                return false;
            }
            if (contentsDifferent(oldModel.getMediaUrl(), newModel.getMediaUrl())){
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

                    recyclerViewInterface.onRecyclerViewItemClick(bundle);
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
                    bundle.putParcelable(Constants.ARG_PARAM2, masterList.get(pos));

                    recyclerViewInterface.onRecyclerViewItemClick(bundle);
                }
            });*/
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
                String charString = charSequence.toString();

                List<MediaEntity> newFilteredList = new ArrayList<>();

                if (charString.isEmpty()) {
                    newFilteredList.addAll(masterList);

                } else {
                    for (int i = 0; i < masterList.size(); i++) {
                        MediaEntity row = masterList.get(i);

                        if (!StringUtils.isEmpty(row.getMediaTitle()) && row.getMediaTitle().toLowerCase().contains(charString.toLowerCase())) {
                            newFilteredList.add(row);
                        } else if (!StringUtils.isEmpty(row.getMediaDescription()) && row.getMediaDescription().toLowerCase().contains(charString.toLowerCase())) {
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

                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(filteredList, newList),
                        false);
                filteredList = new ArrayList<>(newList);
                diffResult.dispatchUpdatesTo(TrendingRecyclerAdapter.this);
            }
        };
    }
}
