package com.pheuture.playlists.trending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pheuture.playlists.R;
import com.pheuture.playlists.utils.BaseFragment;

public class TrendingFragment extends BaseFragment {
    private trendingViewModel trendingViewModel;

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trendings, container, false);
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