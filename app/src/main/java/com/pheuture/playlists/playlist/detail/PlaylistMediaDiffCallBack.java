package com.pheuture.playlists.playlist.detail;

import androidx.recyclerview.widget.DiffUtil;

import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;

import java.util.List;

public class PlaylistMediaDiffCallBack extends DiffUtil.Callback {
    private List<PlaylistMediaEntity> oldList;
    private List<PlaylistMediaEntity> newList;

    public PlaylistMediaDiffCallBack(List<PlaylistMediaEntity> oldList, List<PlaylistMediaEntity> newList) {
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

        if (contentsDifferent(oldModel.getMediaTitle(), newModel.getMediaTitle())) {
            return false;
        }
        if (contentsDifferent(oldModel.getMovieName(), newModel.getMovieName())) {
            return false;
        }
        if (contentsDifferent(oldModel.getMediaThumbnail(), newModel.getMediaThumbnail())) {
            return false;
        }
        if (contentsDifferent(String.valueOf(oldModel.getPlayDuration()), String.valueOf(newModel.getPlayDuration()))) {
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
