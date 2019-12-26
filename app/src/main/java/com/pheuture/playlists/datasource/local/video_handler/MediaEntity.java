package com.pheuture.playlists.datasource.local.video_handler;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.bumptech.glide.Glide;
import com.google.gson.annotations.SerializedName;
import com.pheuture.playlists.utils.CalenderUtils;

@Entity
public class MediaEntity implements Parcelable {

	@NonNull
	@PrimaryKey
	@SerializedName("mediaID")
	private long mediaID;

	@SerializedName("mediaDescription")
	private String mediaDescription;

	@SerializedName("mediaUrl")
	private String mediaUrl;

	@SerializedName("mediaThumbnail")
	private String mediaThumbnail;

	@SerializedName("mediaName")
	private String mediaName;

	@SerializedName("mediaTitle")
	private String mediaTitle;

	@SerializedName("playDuration")
	private long playDuration;

	@SerializedName("postDate")
	private String postDate;

	@SerializedName("status")
	private String status;

	private MediaEntity(Parcel in) {
		mediaID = in.readLong();
		mediaDescription = in.readString();
		mediaUrl = in.readString();
		mediaThumbnail = in.readString();
		mediaName = in.readString();
		mediaTitle = in.readString();
		playDuration = in.readLong();
		postDate = in.readString();
		status = in.readString();
	}

	public MediaEntity() {
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mediaID);
		dest.writeString(mediaDescription);
		dest.writeString(mediaUrl);
		dest.writeString(mediaThumbnail);
		dest.writeString(mediaName);
		dest.writeString(mediaTitle);
		dest.writeLong(playDuration);
		dest.writeString(postDate);
		dest.writeString(status);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<MediaEntity> CREATOR = new Creator<MediaEntity>() {
		@Override
		public MediaEntity createFromParcel(Parcel in) {
			return new MediaEntity(in);
		}

		@Override
		public MediaEntity[] newArray(int size) {
			return new MediaEntity[size];
		}
	};

	public long getMediaID() {
		return mediaID;
	}

	public void setMediaID(long mediaID) {
		this.mediaID = mediaID;
	}

	public String getMediaDescription() {
		return mediaDescription;
	}

	public void setMediaDescription(String mediaDescription) {
		this.mediaDescription = mediaDescription;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getMediaThumbnail() {
		return mediaThumbnail;
	}

	public void setMediaThumbnail(String mediaThumbnail) {
		this.mediaThumbnail = mediaThumbnail;
	}

	public String getMediaName() {
		return mediaName;
	}

	public void setMediaName(String mediaName) {
		this.mediaName = mediaName;
	}

	public String getMediaTitle() {
		return mediaTitle;
	}

	public void setMediaTitle(String mediaTitle) {
		this.mediaTitle = mediaTitle;
	}

	public long getPlayDuration() {
		return playDuration;
	}

	public void setPlayDuration(long playDuration) {
		this.playDuration = playDuration;
	}

	public String getPostDate() {
		return postDate;
	}

	public void setPostDate(String postDate) {
		this.postDate = postDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@BindingAdapter({"imageUrl"})
	public static void loadImage(ImageView view, String imageUrl) {
		if (imageUrl==null || imageUrl.length()==0){
			return;
		}
		Glide.with(view.getContext())
				.load(imageUrl)
				.into(view);
	}

	public String getFormattedPlayDuration() {
		return CalenderUtils.getTimeDurationInFormat1(playDuration);
	}

}