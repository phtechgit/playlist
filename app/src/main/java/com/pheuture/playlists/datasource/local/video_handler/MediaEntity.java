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
	@SerializedName("id")
	private long mediaID;

	@SerializedName("videoDescription")
	private String videoDescription;

	@SerializedName("videoUrl")
	private String videoUrl;

	@SerializedName("videoThumbail")
	private String videoThumbnail;

	@SerializedName("videoName")
	private String videoName;

	@SerializedName("videoTitle")
	private String videoTitle;

	@SerializedName("video_duration")
	private long playDuration;

	@SerializedName("postDate")
	private String postDate;

	@SerializedName("status")
	private String status;

	public MediaEntity() {
	}

	protected MediaEntity(Parcel in) {
		mediaID = in.readLong();
		videoDescription = in.readString();
		videoUrl = in.readString();
		videoThumbnail = in.readString();
		videoName = in.readString();
		videoTitle = in.readString();
		playDuration = in.readLong();
		postDate = in.readString();
		status = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mediaID);
		dest.writeString(videoDescription);
		dest.writeString(videoUrl);
		dest.writeString(videoThumbnail);
		dest.writeString(videoName);
		dest.writeString(videoTitle);
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

	@BindingAdapter({"imageUrl"})
	public static void loadImage(ImageView view, String imageUrl) {
		if (imageUrl==null || imageUrl.length()==0){
			return;
		}
		Glide.with(view.getContext())
				.load(imageUrl)
				.into(view);
	}

	public long getMediaID() {
		return mediaID;
	}

	public void setMediaID(long mediaID) {
		this.mediaID = mediaID;
	}

	public String getVideoDescription() {
		return videoDescription;
	}

	public void setVideoDescription(String videoDescription) {
		this.videoDescription = videoDescription;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getVideoThumbnail() {
		return videoThumbnail;
	}

	public void setVideoThumbnail(String videoThumbnail) {
		this.videoThumbnail = videoThumbnail;
	}

	public String getVideoName() {
		return videoName;
	}

	public void setVideoName(String videoName) {
		this.videoName = videoName;
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

	public long getPlayDuration() {
		return playDuration;
	}

	public void setPlayDuration(long playDuration) {
		this.playDuration = playDuration;
	}

    public String getFormattedPlayDuration() {
		return CalenderUtils.getTimeDurationInFormat1(playDuration);
    }

	public String getVideoTitle() {
		return videoTitle;
	}

	public void setVideoTitle(String videoTitle) {
		this.videoTitle = videoTitle;
	}
}