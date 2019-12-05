package com.pheuture.playlists.datasource.local.playlist_handler;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Entity
public class PlaylistEntity implements Parcelable {

	@NonNull
	@PrimaryKey
	@SerializedName("id")
	private long id;

	@SerializedName("created_time")
	private long createdDate;

	@SerializedName("playDuration")
	private long playDuration;

	@SerializedName("playlistName")
	private String playlistName;

	@SerializedName("songsCount")
	private int songsCount;

	public PlaylistEntity() {
	}

	protected PlaylistEntity(Parcel in) {
		id = in.readLong();
		createdDate = in.readLong();
		playDuration = in.readLong();
		playlistName = in.readString();
		songsCount = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(createdDate);
		dest.writeLong(playDuration);
		dest.writeString(playlistName);
		dest.writeInt(songsCount);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<PlaylistEntity> CREATOR = new Creator<PlaylistEntity>() {
		@Override
		public PlaylistEntity createFromParcel(Parcel in) {
			return new PlaylistEntity(in);
		}

		@Override
		public PlaylistEntity[] newArray(int size) {
			return new PlaylistEntity[size];
		}
	};

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}

	public long getPlayDuration() {
		return playDuration;
	}

	public void setPlayDuration(long playDuration) {
		this.playDuration = playDuration;
	}

	public String getPlaylistName() {
		return playlistName;
	}

	public void setPlaylistName(String playlistName) {
		this.playlistName = playlistName;
	}

	public int getSongsCount() {
		return songsCount;
	}

	public void setSongsCount(int songsCount) {
		this.songsCount = songsCount;
	}

	@BindingAdapter({"bind:showOrHide"})
	public static void showOrHide(View view, int position) {
		if (position == 0){
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}
}