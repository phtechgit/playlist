package com.pheuture.playlists.queue;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import com.pheuture.playlists.datasource.local.media_handler.queue.QueueMediaEntity;
import java.util.List;

public class MediaQueueDiffUtil extends DiffUtil.Callback {
    private List<QueueMediaEntity> oldList;
    private List<QueueMediaEntity> newList;

    public MediaQueueDiffUtil(List<QueueMediaEntity> oldList, List<QueueMediaEntity> newList) {
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
        return oldList.get(oldItemPosition).getPosition() == newList.get(newItemPosition).getPosition();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        QueueMediaEntity oldModel = oldList.get(oldItemPosition);
        QueueMediaEntity newModel = newList.get(newItemPosition);
        
        if (contentsDifferent(oldModel.getMediaTitle(), newModel.getMediaTitle())) {
            return false;
        }
        if (contentsDifferent(oldModel.getMovieName(), newModel.getMovieName())) {
            return false;
        }
        if (contentsDifferent(oldModel.getMediaThumbnail(), newModel.getMediaThumbnail())) {
            return false;
        }
        if (contentsDifferent(String.valueOf(oldModel.getFormattedPlayDuration()), String.valueOf(newModel.getFormattedPlayDuration()))) {
            return false;
        }
        if (contentsDifferent(String.valueOf(oldModel.getState()), String.valueOf(newModel.getState()))) {
            return false;
        }
        if (contentsDifferent(String.valueOf(oldModel.getProgress()), String.valueOf(newModel.getProgress()))) {
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        QueueMediaEntity oldModel = oldList.get(oldItemPosition);
        QueueMediaEntity newModel = newList.get(newItemPosition);

        Bundle bundle = new Bundle();
        if (contentsDifferent(oldModel.getMediaTitle(), newModel.getMediaTitle())) {
            bundle.putString(QueueMediaEntity.MediaColumns.MEDIA_TITLE, newModel.getMediaTitle());
        }
        if (contentsDifferent(oldModel.getMovieName(), newModel.getMovieName())) {
            bundle.putString(QueueMediaEntity.MediaColumns.MEDIA_MOVIE_NAME, newModel.getMovieName());
        }
        if (contentsDifferent(oldModel.getMediaThumbnail(), newModel.getMediaThumbnail())) {
            bundle.putString(QueueMediaEntity.MediaColumns.MEDIA_THUMBNAIL, newModel.getMediaThumbnail());
        }
        if (contentsDifferent(String.valueOf(oldModel.getFormattedPlayDuration()), String.valueOf(newModel.getFormattedPlayDuration()))) {
            bundle.putString(QueueMediaEntity.MediaColumns.PLAY_DURATION, newModel.getFormattedPlayDuration());
        }
        if (contentsDifferent(String.valueOf(oldModel.getState()), String.valueOf(newModel.getState()))) {
            bundle.putInt(QueueMediaEntity.QueueMediaColumns.STATE, newModel.getState());
        }
        if (contentsDifferent(String.valueOf(oldModel.getState()), String.valueOf(newModel.getState()))) {
            bundle.putInt(QueueMediaEntity.QueueMediaColumns.PROGRESS, newModel.getProgress());
        }
        return bundle;
    }

    private boolean contentsDifferent(String oldData, String newData) {
        if (oldData == null && newData != null) {
            return false;
        }
        if (oldData != null && newData == null) {
            return false;
        }
        if (oldData == null) {
            return false;
        }
        return !oldData.equals(newData);
    }
}

