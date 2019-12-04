package com.pheuture.playlists.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pheuture.playlists.R;
import com.pheuture.playlists.utils.BaseFragment;

public class SettingsFragment extends BaseFragment {
    private SettingsViewModel settingsViewModel;

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void initializations() {

    }

    @Override
    public void handleListeners() {

    }

    @Override
    public void onClick(View v) {

    }
}