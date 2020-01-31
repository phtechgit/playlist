package com.pheuture.playlists.media;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.pheuture.playlists.base.utils.CalenderUtils;

@Entity
public class MediaEntity implements Parcelable {

	@NonNull
	@PrimaryKey
	@SerializedName("mediaID")
	private long mediaID;

	@SerializedName("status")
	private String status;

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

	@SerializedName("movieName")
	private String movieName;

	@SerializedName("singers")
	private String singers;

	@SerializedName("musicDirector")
	private String musicDirector;

	@SerializedName("artists")
	private String artists;

	@SerializedName("movieDirector")
	private String movieDirector;

	@SerializedName("playDuration")
	private long playDuration;

	@SerializedName("postDate")
	private String postDate;

	@SerializedName("createdOn")
	private long createdOn;

	@SerializedName("modifiedOn")
	private long modifiedOn;

	public MediaEntity() {
	}

	protected MediaEntity(Parcel in) {
		mediaID = in.readLong();
		status = in.readString();
		mediaDescription = in.readString();
		mediaUrl = in.readString();
		mediaThumbnail = in.readString();
		mediaName = in.readString();
		mediaTitle = in.readString();
		movieName = in.readString();
		singers = in.readString();
		musicDirector = in.readString();
		artists = in.readString();
		movieDirector = in.readString();
		playDuration = in.readLong();
		postDate = in.readString();
		createdOn = in.readLong();
		modifiedOn = in.readLong();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mediaID);
		dest.writeString(status);
		dest.writeString(mediaDescription);
		dest.writeString(mediaUrl);
		dest.writeString(mediaThumbnail);
		dest.writeString(mediaName);
		dest.writeString(mediaTitle);
		dest.writeString(movieName);
		dest.writeString(singers);
		dest.writeString(musicDirector);
		dest.writeString(artists);
		dest.writeString(movieDirector);
		dest.writeLong(playDuration);
		dest.writeString(postDate);
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMovieName() {
		return movieName;
	}

	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	public String getSingers() {
		return singers;
	}

	public void setSingers(String singers) {
		this.singers = singers;
	}

	public String getMusicDirector() {
		return musicDirector;
	}

	public void setMusicDirector(String musicDirector) {
		this.musicDirector = musicDirector;
	}

	public String getArtists() {
		return artists;
	}

	public void setArtists(String artists) {
		this.artists = artists;
	}

	public String getMovieDirector() {
		return movieDirector;
	}

	public void setMovieDirector(String movieDirector) {
		this.movieDirector = movieDirector;
	}

	public interface MediaColumns{
		String MEDIA_DESCRIPTION = "mediaDescription";
		String MEDIA_MOVIE_NAME = "mediaMovieName";
		String MEDIA_URL = "mediaUrl";
		String MEDIA_THUMBNAIL = "mediaThumbnail";
		String MEDIA_NAME = "mediaName";
		String MEDIA_TITLE = "mediaTitle";
		String PLAY_DURATION = "playDuration";
		String POST_DATE = "postDate";
		String CREATED_ON = "createdOn";
		String STATUS = "status";
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