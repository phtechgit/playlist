package com.pheuture.playlists.datasource.local.video_handler.offline;

import com.google.gson.annotations.SerializedName;

public class Common {

    @SerializedName("mediaDescription")
    protected String mediaDescription;

    @SerializedName("mediaUrl")
    protected String mediaUrl;

    @SerializedName("mediaThumbnail")
    protected String mediaThumbnail;

    @SerializedName("mediaName")
    protected String mediaName;

    @SerializedName("mediaTitle")
    protected String mediaTitle;

    @SerializedName("playDuration")
    protected long playDuration;

    @SerializedName("postDate")
    protected String postDate;

    @SerializedName("status")
    protected String status;
}
