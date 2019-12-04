package com.pheuture.playlists.datasource.remote;

import android.os.Parcel;
import android.os.Parcelable;

public class ResponseModel implements Parcelable {
    private String message;
    private String url;

    public ResponseModel(String url){
        this.url = url;

    }

    protected ResponseModel(Parcel in) {
        url = in.readString();
    }

    public static final Creator<ResponseModel> CREATOR = new Creator<ResponseModel>() {
        @Override
        public ResponseModel createFromParcel(Parcel in) {
            return new ResponseModel(in);
        }

        @Override
        public ResponseModel[] newArray(int size) {
            return new ResponseModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
