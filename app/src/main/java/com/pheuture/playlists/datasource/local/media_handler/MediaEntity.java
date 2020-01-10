package com.pheuture.playlists.datasource.local.media_handler;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.pheuture.playlists.utils.CalenderUtils;

@Entity
public class MediaEntity implements Parcelable {

	protected MediaEntity(Parcel in) {
		mediaID = in.readLong();
		mediaDescription = in.readString();
		mediaUrl = in.readString();
		mediaThumbnail = in.readString();
		mediaName = in.readString();
		mediaTitle = in.readString();
		playDuration = in.readLong();
		postDate = in.readString();
		status = in.readString();
		createdOn = in.readLong();
		modifiedOn = in.readLong();
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
		dest.writeLong(createdOn);
		dest.writeLong(modifiedOn);
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

	public long getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(long modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	@Override
	public String toString() {
		return "MediaEntity{" +
				"mediaID=" + mediaID +
				", mediaDescription='" + mediaDescription + '\'' +
				", mediaUrl='" + mediaUrl + '\'' +
				", mediaThumbnail='" + mediaThumbnail + '\'' +
				", mediaName='" + mediaName + '\'' +
				", mediaTitle='" + mediaTitle + '\'' +
				", playDuration=" + playDuration +
				", postDate='" + postDate + '\'' +
				", status='" + status + '\'' +
				", createdOn=" + createdOn +
				", modifiedOn=" + modifiedOn +
				'}';
	}

	public interface MediaColumns{
		String MEDIA_DESCRIPTION = "mediaDescription";
		String MEDIA_URL = "mediaUrl";
		String MEDIA_THUMBNAIL = "mediaThumbnail";
		String MEDIA_NAME = "mediaName";
		String MEDIA_TITLE = "mediaTitle";
		String PLAY_DURATION = "playDuration";
		String POST_DATE = "postDate";
		String CREATED_ON = "createdOn";
		String STATUS = "status";
	}

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

	@SerializedName("createdOn")
	private long createdOn;

	@SerializedName("modifiedOn")
	private long modifiedOn;

	public MediaEntity() {
	}

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

	public String getFormattedPlayDuration() {
		return CalenderUtils.getTimeDurationInFormat1(playDuration);
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}
}