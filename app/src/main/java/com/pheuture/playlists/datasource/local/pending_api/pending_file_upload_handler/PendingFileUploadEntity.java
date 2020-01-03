package com.pheuture.playlists.datasource.local.pending_api.pending_file_upload_handler;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class PendingFileUploadEntity implements Parcelable {

	@NonNull
	@PrimaryKey(autoGenerate = true)
	@SerializedName("id")
	private int id;

	@SerializedName("title")
	private String title;

	@SerializedName("size")
	private long size;

	@SerializedName("params")
	private String params;

	@SerializedName("url")
	private String url;

	public PendingFileUploadEntity() {

	}

	protected PendingFileUploadEntity(Parcel in) {
		id = in.readInt();
		title = in.readString();
		size = in.readLong();
		params = in.readString();
		url = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(title);
		dest.writeLong(size);
		dest.writeString(params);
		dest.writeString(url);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<PendingFileUploadEntity> CREATOR = new Creator<PendingFileUploadEntity>() {
		@Override
		public PendingFileUploadEntity createFromParcel(Parcel in) {
			return new PendingFileUploadEntity(in);
		}

		@Override
		public PendingFileUploadEntity[] newArray(int size) {
			return new PendingFileUploadEntity[size];
		}
	};

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
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