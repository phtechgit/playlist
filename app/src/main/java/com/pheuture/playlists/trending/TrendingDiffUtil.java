package com.pheuture.playlists.trending;

import androidx.recyclerview.widget.DiffUtil;
import com.pheuture.playlists.datasource.local.media_handler.MediaEntity;
import java.util.List;

public class TrendingDiffUtil extends DiffUtil.Callback{
    private List<MediaEntity> oldList;
    private List<MediaEntity> newList;

    public TrendingDiffUtil(List<MediaEntity> oldList, List<MediaEntity> newList) {
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
        if (contentsDifferent(oldModel.getMovieName(), newModel.getMediaDescription())){
            return false;
        }
        if (contentsDifferent(oldModel.getMediaThumbnail(), newModel.getMediaThumbnail())){
            return false;
        }
        if (contentsDifferent(String.valueOf(oldModel.getPlayDuration()), String.valueOf(newModel.getPlayDuration()))){
            return false;
        }
        return true;
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
}
