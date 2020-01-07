package com.pheuture.playlists.datasource.remote;

public class ResponseModel {
    private boolean message;
    private String status;


    public boolean getMessage() {
        return message;
    }

    public void setMessage(boolean message) {
        this.message = message;
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
                '}';
    }
}
