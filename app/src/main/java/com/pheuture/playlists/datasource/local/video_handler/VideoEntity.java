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

@Entity
public class VideoEntity implements Parcelable {

	@NonNull
	@PrimaryKey
	private long id;

	@SerializedName("videoDescription")
	private String videoDescription;

	@SerializedName("videoUrl")
	private String videoUrl;

	@SerializedName("videoThumbail")
	private String videoThumbnail;

	@SerializedName("videoName")
	private String videoName;

	@SerializedName("postDate")
	private String postDate;

	@SerializedName("status")
	private String status;

	public VideoEntity() {
	}

	protected VideoEntity(Parcel in) {
		id = in.readLong();
		videoDescription = in.readString();
		videoUrl = in.readString();
		videoThumbnail = in.readString();
		videoName = in.readString();
		postDate = in.readString();
		status = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(videoDescription);
		dest.writeString(videoUrl);
		dest.writeString(videoThumbnail);
		dest.writeString(videoName);
		dest.writeString(postDate);
		dest.writeString(status);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<VideoEntity> CREATOR = new Creator<VideoEntity>() {
		@Override
		public VideoEntity createFromParcel(Parcel in) {
			return new VideoEntity(in);
		}

		@Override
		public VideoEntity[] newArray(int size) {
			return new VideoEntity[size];
		}
	};

	@BindingAdapter({"bind:imageUrl"})
	public static void loadImage(ImageView view, String imageUrl) {
		Glide.with(view.getContext())
				.load(imageUrl)
				.into(view);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
}