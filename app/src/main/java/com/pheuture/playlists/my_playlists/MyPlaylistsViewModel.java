package com.pheuture.playlists.my_playlists;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyPlaylistsViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public MyPlaylistsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void createPlaylist(String playlistName) {
    }
}