package com.pheuture.playlists.datasource.local.media_handler.queue;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.pheuture.playlists.datasource.local.media_handler.MediaEntity;

@Entity
public class QueueMediaEntity extends MediaEntity implements Parcelable {
    private int position;
    private int state;
    private int progress;

    public interface QueueMediaColumns {
        String POSITION = "position";
        String STATE = "state";
        String PROGRESS = "progress";
    }

    protected QueueMediaEntity(Parcel in) {
        super(in);
        position = in.readInt();
        state = in.readInt();
        progress = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(position);
        dest.writeInt(state);
        dest.writeInt(progress);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QueueMediaEntity> CREATOR = new Creator<QueueMediaEntity>() {
        @Override
        public QueueMediaEntity createFromParcel(Parcel in) {
            return new QueueMediaEntity(in);
        }

        @Override
        public QueueMediaEntity[] newArray(int size) {
            return new QueueMediaEntity[size];
        }
    };

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public interface QueueMediaState{
        int PLAYED = 2;
        int PLAYING = 1;
        int IN_QUEUE = 0;
    }

    public QueueMediaEntity() {
    }

}
