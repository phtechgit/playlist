package com.pheuture.playlists.base;

import java.util.ArrayList;
import java.util.List;

public class Resource<T> {
    private List<T> data = new ArrayList<>();
    private int status;
    private int responseCode = -1;
    private String error = "";

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }


    public interface Status{
        int LOADING = 1;
        int SUCCESS = 2;
        int FAILED = 3;
    }

}
