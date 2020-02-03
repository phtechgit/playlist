package com.pheuture.playlists.playist_detail;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

import com.google.gson.annotations.SerializedName;
import com.pheuture.playlists.media.MediaEntity;

@Entity
public class PlaylistMediaEntity extends MediaEntity implements Parcelable {

    @NonNull
    @PrimaryKey(autoGenerate = true)
	@SerializedName("playlistMediaID")
    private long playlistMediaID;

	@SerializedName("playlistID")
	private long playlistID;

	public PlaylistMediaEntity() {
	}

	protected PlaylistMediaEntity(Parcel in) {
		super(in);
		playlistMediaID = in.readLong();
		playlistID = in.readLong();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(playlistMediaID);
		dest.writeLong(playlistID);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<PlaylistMediaEntity> CREATOR = new Creator<PlaylistMediaEntity>() {
		@Override
		public PlaylistMediaEntity createFromParcel(Parcel in) {
			return new PlaylistMediaEntity(in);
		}

		@Override
		public PlaylistMediaEntity[] newArray(int size) {
			return new PlaylistMediaEntity[size];
		}
	};

	public long getPlaylistMediaID() {
		return playlistMediaID;
	}

	public void setPlaylistMediaID(long playlistMediaID) {
		this.playlistMediaID = playlistMediaID;
	}

	public long getPlaylistID() {
		return playlistID;
	}

	public void setPlaylistID(long playlistID) {
		this.playlistID = playlistID;
	}
}