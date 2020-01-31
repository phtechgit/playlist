package com.pheuture.playlists.base.interfaces;

import androidx.lifecycle.LiveData;

import com.bumptech.glide.load.engine.Resource;

import java.util.List;

public interface Repository<T> {
    void insert(T item);
    void update(T item);
    void delete(T item);

    void insert(List<T> item);
    void update(List<T> item);
    void delete(List<T> item);

    void deleteAll();
    LiveData<Resource<List<T>>> getLiveData();
}
