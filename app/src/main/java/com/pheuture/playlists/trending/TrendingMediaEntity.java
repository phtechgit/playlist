package com.pheuture.playlists.trending;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

import com.pheuture.playlists.media.MediaEntity;

public class TrendingMediaEntity extends MediaEntity implements Parcelable {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
    private int trendingMediaId;
    private int trendingPosition;

    protected TrendingMediaEntity(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrendingMediaEntity> CREATOR = new Creator<TrendingMediaEntity>() {
        @Override
        public TrendingMediaEntity createFromParcel(Parcel in) {
            return new TrendingMediaEntity(in);
        }

        @Override
        public TrendingMediaEntity[] newArray(int size) {
            return new TrendingMediaEntity[size];
        }
    };
}
