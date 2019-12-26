package com.pheuture.playlists.datasource.local.playlist_handler;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.pheuture.playlists.utils.CalenderUtils;

@Entity
public class PlaylistEntity implements Parcelable, Cloneable {

	@NonNull
	@PrimaryKey
	@SerializedName("playlistID")
	private long playlistID;

	@SerializedName("createdOn")
	private long createdOn;

	@SerializedName("createdByUserID")
	private long createdByUserID;

	@SerializedName("createdByUserName")
	private String createdByUserName;

	@SerializedName("playDuration")
	private long playDuration;

	@SerializedName("playlistName")
	private String playlistName;

	@SerializedName("songsCount")
	private long songsCount;

	public PlaylistEntity() {
	}

	protected PlaylistEntity(Parcel in) {
		playlistID = in.readLong();
		createdOn = in.readLong();
		createdByUserID = in.readLong();
		createdByUserName = in.readString();
		playDuration = in.readLong();
		playlistName = in.readString();
		songsCount = in.readLong();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(playlistID);
		dest.writeLong(createdOn);
		dest.writeLong(createdByUserID);
		dest.writeString(createdByUserName);
		dest.writeLong(playDuration);
		dest.writeString(playlistName);
		dest.writeLong(songsCount);
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

	@NonNull
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public long getPlaylistID() {
		return playlistID;
	}

	public void setPlaylistID(long playlistID) {
		this.playlistID = playlistID;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
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

	public long getSongsCount() {
		return songsCount;
	}

	public void setSongsCount(long songsCount) {
		this.songsCount = songsCount;
	}

	@BindingAdapter({"showOrHide"})
	public static void showOrHide(View view, int position) {
		if (position == 0){
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}

	public String getSongsCountWithFormattedTotalPlaybackTime(){
		if (songsCount>1) {
			return songsCount + " songs \u2022 " + CalenderUtils.getTimeDurationFormat2(playDuration);
		} else {
			return songsCount + " song \u2022 " + CalenderUtils.getTimeDurationFormat2(playDuration);
		}
	}

	public String getCreatedByFormatted(){
		return "by " + createdByUserName;
	}

	public String getCreatedByUserName() {
		return createdByUserName;
	}

	public void setCreatedByUserName(String createdByUserName) {
		this.createdByUserName = createdByUserName;
	}

	public long getCreatedByUserID() {
		return createdByUserID;
	}

	public void setCreatedByUserID(long createdByUserID) {
		this.createdByUserID = createdByUserID;
	}
}