package com.pheuture.playlists.datasource.local.video_handler.offline;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;

import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;

@Entity
public class OfflineVideoEntity extends VideoEntity implements Parcelable {
    private long downloadID;
    private int downloadStatus;
    private String downloadedFilePath;

    public OfflineVideoEntity() {
    }

    protected OfflineVideoEntity(Parcel in) {
        super(in);
        downloadID = in.readLong();
        downloadStatus = in.readInt();
        downloadedFilePath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(downloadID);
        dest.writeInt(downloadStatus);
        dest.writeString(downloadedFilePath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OfflineVideoEntity> CREATOR = new Creator<OfflineVideoEntity>() {
        @Override
        public OfflineVideoEntity createFromParcel(Parcel in) {
            return new OfflineVideoEntity(in);
        }

        @Override
        public OfflineVideoEntity[] newArray(int size) {
            return new OfflineVideoEntity[size];
        }
    };

    public long getDownloadID() {
        return downloadID;
    }

    public void setDownloadID(long downloadID) {
        this.downloadID = downloadID;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getDownloadedFilePath() {
        return downloadedFilePath;
    }

    public void setDownloadedFilePath(String downloadedFilePath) {
        this.downloadedFilePath = downloadedFilePath;
    }
}