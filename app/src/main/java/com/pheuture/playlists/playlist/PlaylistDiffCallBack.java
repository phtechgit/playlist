package com.pheuture.playlists.playlist;

import androidx.recyclerview.widget.DiffUtil;

import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;

import java.util.List;

public class PlaylistDiffCallBack extends DiffUtil.Callback{
    private List<PlaylistEntity> oldList;
    private List<PlaylistEntity> newList;

    PlaylistDiffCallBack(List<PlaylistEntity> oldList, List<PlaylistEntity> newList) {
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
        return oldList.get(oldItemPosition).getPlaylistID() == newList.get(newItemPosition).getPlaylistID();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        PlaylistEntity oldModel = oldList.get(oldItemPosition);
        PlaylistEntity newModel = newList.get(newItemPosition);

        if (contentsDifferent(oldModel.getPlaylistName(), newModel.getPlaylistName())){
            return false;
        }
        if (contentsDifferent(String.valueOf(oldModel.getSongsCount()), String.valueOf(newModel.getSongsCount()))){
            return false;
        }
        if (contentsDifferent(String.valueOf(oldModel.getPlayDuration()), String.valueOf(newModel.getPlayDuration()))){
            return false;
        }
        if (contentsDifferent(String.valueOf(oldModel.getCreatedOn()), String.valueOf(newModel.getCreatedOn()))){
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
