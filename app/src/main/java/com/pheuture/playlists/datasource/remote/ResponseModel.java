package com.pheuture.playlists.datasource.remote;

import org.json.JSONArray;

public class ResponseModel {
    private boolean message;
    private String status;
    private JSONArray data;

    public boolean getMessage() {
        return message;
    }

    public void setMessage(boolean message) {
        this.message = message;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "message=" + message +
                ", status='" + status + '\'' +
                ", data=" + data +
                '}';
    }
}
