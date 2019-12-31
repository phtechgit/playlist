package com.pheuture.playlists.datasource.local.pending_upload_handler;

import android.os.Parcel;
import android.os.Parcelable;

public class PendingUploadParamEntity implements Parcelable {

    public PendingUploadParamEntity(int mediaType, String key, String value, String extra) {
        this.mediaType = mediaType;
        this.key = key;
        this.value = value;
        this.extra = extra;
    }

    public interface MediaType{
        int FILE = 1;
        int OTHER = 2;
    }

    private int mediaType;
    private String key;
    private String value;
    private String extra;

    public PendingUploadParamEntity() {
    }

    protected PendingUploadParamEntity(Parcel in) {
        mediaType = in.readInt();
        key = in.readString();
        value = in.readString();
        extra = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mediaType);
        dest.writeString(key);
        dest.writeString(value);
        dest.writeString(extra);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PendingUploadParamEntity> CREATOR = new Creator<PendingUploadParamEntity>() {
        @Override
        public PendingUploadParamEntity createFromParcel(Parcel in) {
            return new PendingUploadParamEntity(in);
        }

        @Override
        public PendingUploadParamEntity[] newArray(int size) {
            return new PendingUploadParamEntity[size];
        }
    };

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

}
