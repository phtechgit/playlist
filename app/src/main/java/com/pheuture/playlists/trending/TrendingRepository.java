package com.pheuture.playlists.trending;

import androidx.lifecycle.LiveData;

import com.pheuture.playlists.base.Resource;
import com.pheuture.playlists.base.interfaces.Repository;
import com.pheuture.playlists.media.MediaEntity;
import com.pheuture.playlists.trending.TrendingMediaLocalDao;
import com.pheuture.playlists.trending.TrendingMediaRemoteDao;

import java.util.List;

public class TrendingRepository implements Repository<MediaEntity> {
    private TrendingMediaLocalDao trendingMediaLocalDao;
    private TrendingMediaRemoteDao trendingMediaRemoteDao;

    public TrendingRepository(TrendingMediaLocalDao trendingMediaLocalDao, TrendingMediaRemoteDao trendingMediaRemoteDao) {
        this.trendingMediaLocalDao = trendingMediaLocalDao;
        this.trendingMediaRemoteDao = trendingMediaRemoteDao;
    }

    @Override
    public void insert(MediaEntity item) {

    }

    @Override
    public void update(MediaEntity item) {

    }

    @Override
    public void delete(MediaEntity item) {

    }

    @Override
    public void insert(List<MediaEntity> item) {

    }

    @Override
    public void update(List<MediaEntity> item) {

    }

    @Override
    public void delete(List<MediaEntity> item) {

    }

    @Override
    public void deleteAll() {
        trendingMediaLocalDao.deleteAll();
    }

    @Override
    public LiveData<Resource.Success<List<MediaEntity>>> getLiveData() {
        return null;
    }
}
