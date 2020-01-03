package com.pheuture.playlists.datasource.local.user_handler;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.pheuture.playlists.utils.StringUtils;

@Entity
public class UserEntity implements Parcelable {

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

	public UserEntity() {

    }

	protected UserEntity(Parcel in) {
		userID = in.readInt();
		userMobile = in.readString();
		userFirstName = in.readString();
		userLastName = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(userID);
		dest.writeString(userMobile);
		dest.writeString(userFirstName);
		dest.writeString(userLastName);
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
}