package com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.bumptech.glide.Glide;
import com.google.gson.annotations.SerializedName;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;

@Entity
public class PlaylistMediaEntity extends MediaEntity implements Parcelable {
	@NonNull
	@PrimaryKey(autoGenerate = true)
	@SerializedName("ignore")
	private long id;

	@SerializedName("playlistId")
	private long playlistID;

	protected PlaylistMediaEntity(Parcel in) {
		id = in.readLong();
		playlistID = in.readLong();
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

	public PlaylistMediaEntity() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(playlistID);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPlaylistID() {
		return playlistID;
	}

	public void setPlaylistID(long playlistID) {
		this.playlistID = playlistID;
	}
}