package com.pheuture.playlists.base.service;

import android.os.Parcel;
import android.os.Parcelable;

public class PendingFileUploadParamEntity implements Parcelable {
    private int mediaType;
    private String key;
    private String value;
    private String extra;

    public PendingFileUploadParamEntity(int mediaType, String key, Object value, String extra) {
        this.mediaType = mediaType;
        this.key = key;
        this.value = value.toString();
        this.extra = extra;
    }

    public interface MediaType{
        int OTHER = 1;
        int FILE = 2;
    }

    protected PendingFileUploadParamEntity(int other, String createdOn, long timeStamp, Parcel in) {
        mediaType = in.readInt();
        key = in.readString();
        value = in.readString();
        extra = in.readString();
    }

    protected PendingFileUploadParamEntity(Parcel in) {
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

    public static final Creator<PendingFileUploadParamEntity> CREATOR = new Creator<PendingFileUploadParamEntity>() {
        @Override
        public PendingFileUploadParamEntity createFromParcel(Parcel in) {
            return new PendingFileUploadParamEntity(in);
        }

        @Override
        public PendingFileUploadParamEntity[] newArray(int size) {
            return new PendingFileUploadParamEntity[size];
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
