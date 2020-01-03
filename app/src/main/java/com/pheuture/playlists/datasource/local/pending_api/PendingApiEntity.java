package com.pheuture.playlists.datasource.local.pending_api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class PendingApiEntity implements Parcelable {

	@NonNull
	@PrimaryKey(autoGenerate = true)
	@SerializedName("id")
	private int id;

	@SerializedName("params")
	private String params;

	@SerializedName("url")
	private String url;


	public PendingApiEntity() {

	}

	protected PendingApiEntity(Parcel in) {
		id = in.readInt();
		params = in.readString();
		url = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(params);
		dest.writeString(url);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<PendingApiEntity> CREATOR = new Creator<PendingApiEntity>() {
		@Override
		public PendingApiEntity createFromParcel(Parcel in) {
			return new PendingApiEntity(in);
		}

		@Override
		public PendingApiEntity[] newArray(int size) {
			return new PendingApiEntity[size];
		}
	};

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}