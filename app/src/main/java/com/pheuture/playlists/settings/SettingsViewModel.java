package com.pheuture.playlists.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pheuture.playlists.datasource.local.user_handler.UserModel;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;

public class SettingsViewModel extends AndroidViewModel {
    private MutableLiveData<String> mText;
    private UserModel user;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                getApplication(), Constants.USER, ""), UserModel.class);
        mText = new MutableLiveData<>();
        mText.postValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}