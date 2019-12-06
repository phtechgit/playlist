package com.pheuture.playlists.playlists.detail;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ActivityPlaylistDetailBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.Logger;
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
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;

    @Override
    public void initializations() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        model = getIntent().getParcelableExtra(ARG_PARAM1);

        viewModel = ViewModelProviders.of(this, new PlaylistDetailViewModelFactory(
                this.getApplication(),
                model)).get(PlaylistDetailViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_playlist_detail);
        binding.setModel(model);

        exoPlayer = viewModel.getExoPlayer();
        playerView = viewModel.getPlayerView();

        exoPlayer.addListener(playerListener);

        layoutManager = new LinearLayoutManager(this);
        recyclerAdapter = new PlaylistVideosRecyclerAdapter(this, layoutManager, exoPlayer, playerView);

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
        binding.imageButtonPlay.setOnClickListener(this);
        binding.imageButtonShuffle.setOnClickListener(this);
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

        } else if (v.equals(binding.imageButtonPlay)) {
          if (exoPlayer.getPlaybackState() == Player.STATE_READY && exoPlayer.getPlayWhenReady()){
              recyclerAdapter.setPlayerState(false);
          } else {
              recyclerAdapter.setPlayerState(true);
          }
        }
    }

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {

    }

    @Override
    public void onRecyclerViewItemLongClick(Bundle bundle) {

    }

    private Player.EventListener playerListener = new Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
            //0=Player.TIMELINE_CHANGE_REASON_PREPARED;
            Logger.e(TAG, "onTimelineChanged: " + " " + reason);
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Logger.e(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Logger.e(TAG, "onLoadingChanged: " + " " + isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playWhenReady && playbackState == Player.STATE_READY) {
                // media actually playing
                binding.imageButtonPlay.setImageResource(R.drawable.ic_pause_circular_light);
            } else {
                // player paused in any state
                binding.imageButtonPlay.setImageResource(R.drawable.ic_play_circular_white);
            }
            /*switch (playbackState) {
                case Player.STATE_BUFFERING:
                    Logger.e(TAG, "onPlayerStateChanged: buffering");
                    int percentageBuffered = exoPlayer.getBufferedPercentage();
                    Logger.e(TAG, percentageBuffered + "");
                    break;
                case Player.STATE_ENDED:
                    Logger.e(TAG, "onPlayerStateChanged: ended");
                    break;
                case Player.STATE_IDLE:
                    Logger.e(TAG, "onPlayerStateChanged: idle");
                    break;
                case Player.STATE_READY:
                    Logger.e(TAG, "onPlayerStateChanged: ready");
                    break;
                default:
                    break;
            }*/
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Logger.e(TAG, "onPlayerError: " + error.getMessage());
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            //1=Player.DISCONTINUITY_REASON_SEEK;
            Logger.e(TAG, "onPositionDiscontinuity: " + reason);
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {
            Logger.e(TAG, "onSeekProcessed");
        }
    };
}
