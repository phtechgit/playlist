package com.pheuture.playlists.auth;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

import com.google.gson.annotations.SerializedName;
import com.pheuture.playlists.base.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

@Entity
public class UserEntity implements Parcelable {

	@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
	@NonNull
	@PrimaryKey
	@SerializedName("userID")
	protected int userID;

	@SerializedName("userMobile")
	protected String userMobile;

	@SerializedName("userFirstName")
	protected String userFirstName;

	@SerializedName("userLastName")
	protected String userLastName;

	@SerializedName("createdOn")
	protected long createdOn;

	@SerializedName("modifiedOn")
	protected long modifiedOn;

	protected UserEntity() {

    }

	protected UserEntity(Parcel in) {
		userID = in.readInt();
		userMobile = in.readString();
		userFirstName = in.readString();
		userLastName = in.readString();
		createdOn = in.readLong();
		modifiedOn = in.readLong();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(userID);
		dest.writeString(userMobile);
		dest.writeString(userFirstName);
		dest.writeString(userLastName);
		dest.writeLong(createdOn);
		dest.writeLong(modifiedOn);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<UserEntity> CREATOR = new Creator<UserEntity>() {
		@Override
		public UserEntity createFromParcel(Parcel in) {
			return new UserEntity(in);
		}

		@Override
		public UserEntity[] newArray(int size) {
			return new UserEntity[size];
		}
	};

	public String getUserMobile() {
		return userMobile;
	}

	public void setUserMobile(String userMobile) {
		this.userMobile = userMobile;
	}

	public String getUserFirstName() {
		return userFirstName;
	}

	public void setUserFirstName(String userFirstName) {
		this.userFirstName = userFirstName;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getUserLastName() {
		return userLastName;
	}

	public void setUserLastName(String userLastName) {
		this.userLastName = userLastName;
	}

	public String getUserFullName(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(userFirstName);
		if (!StringUtils.isEmpty(userLastName)){
			stringBuilder.append(" ");
			stringBuilder.append(userLastName);
		}
		return stringBuilder.toString();
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public long getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(long modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	@NotNull
	@Override
	public String toString() {
		return "UserEntity{" +
				"userID=" + userID +
				", userMobile='" + userMobile + '\'' +
				", userFirstName='" + userFirstName + '\'' +
				", userLastName='" + userLastName + '\'' +
				", createdOn=" + createdOn +
				", modifiedOn=" + modifiedOn +
				'}';
	}
}