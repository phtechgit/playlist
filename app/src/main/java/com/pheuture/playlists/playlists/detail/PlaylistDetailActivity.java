package com.pheuture.playlists.playlists.detail;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ActivityPlaylistDetailBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.SimpleDividerItemDecoration;
import com.pheuture.playlists.videos.VideosActivity;

import java.util.List;

public class PlaylistDetailActivity extends BaseActivity implements RecyclerViewInterface {
    private static final String TAG = PlaylistDetailActivity.class.getSimpleName();
    private ActivityPlaylistDetailBinding binding;
    private PlaylistDetailViewModel viewModel;
    private PlaylistVideosRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;
    private PlaylistEntity model;

    @Override
    public void initializations() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        model = getIntent().getParcelableExtra(ARG_PARAM1);

        viewModel = ViewModelProviders.of(this, new PlaylistDetailViewModelFactory(
                this.getApplication(),
                model)).get(PlaylistDetailViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_playlist_detail);
        binding.setModel(model);

        recyclerAdapter = new PlaylistVideosRecyclerAdapter(this);
        layoutManager = new LinearLayoutManager(this);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);
        binding.recyclerView.addItemDecoration(
                new SimpleDividerItemDecoration(getResources().getDrawable(R.drawable.line_divider),
                        0, 32));

        viewModel.getVideosLive().observe(this, new Observer<List<VideoEntity>>() {
            @Override
            public void onChanged(List<VideoEntity> videoEntities) {
                if (videoEntities.size()>0){
                    binding.imageButtonPlay.setVisibility(View.VISIBLE);
                    binding.imageButtonShuffle.setVisibility(View.VISIBLE);
                } else {
                    binding.imageButtonPlay.setVisibility(View.GONE);
                    binding.imageButtonShuffle.setVisibility(View.GONE);
                }

                recyclerAdapter.setData(videoEntities);
            }
        });

        viewModel.getProgressStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                if(show){
                    showProgress(binding.progressLayout.progressFullscreen, true);
                } else {
                    hideProgress(binding.progressLayout.progressFullscreen);
                }
            }
        });
    }

    @Override
    public void setListeners() {
        binding.imageButtonAddNewSong.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getFreshData();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.imageButtonAddNewSong)){
            Intent intent = new Intent(this, VideosActivity.class);
            intent.putExtra(ARG_PARAM1, model);
            startActivity(intent);
        }
    }

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {

    }

    @Override
    public void onRecyclerViewItemLongClick(Bundle bundle) {

    }
}
