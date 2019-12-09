package com.pheuture.playlists.playlists.detail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
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

import java.util.ArrayList;
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
    private boolean isPlaying;
    private int playerPosition;
    private ConcatenatingMediaSource concatenatedSource;
    private List<MediaSource> mediaSources;

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
        recyclerAdapter = new PlaylistVideosRecyclerAdapter(this, layoutManager, playerView);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);
        binding.recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                Logger.e(TAG, "onChildViewAttachedToWindow: " + binding.recyclerView.getChildAdapterPosition(view));
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                Logger.e(TAG, "onChildViewDetachedFromWindow: " + binding.recyclerView.getChildAdapterPosition(view));
            }
        });
        binding.recyclerView.addOnScrollListener(scrollListener);


        viewModel.getVideosLive().observe(this, new Observer<List<VideoEntity>>() {
            @Override
            public void onChanged(List<VideoEntity> videoEntities) {
                //create single instance of media source/playlist
                concatenatedSource = new ConcatenatingMediaSource(true);
                mediaSources = new ArrayList<>();
                for (int i=0; i<videoEntities.size() ; i++){
                    VideoEntity model = videoEntities.get(i);

                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(viewModel.getDataSourceFactory())
                            .createMediaSource(Uri.parse(model.getVideoUrl()));
                    mediaSources.add(mediaSource);
                }

                concatenatedSource.addMediaSources(mediaSources);

                exoPlayer.prepare(concatenatedSource);
                recyclerAdapter.setData(videoEntities);

                //show/hide play pause button
                if (videoEntities.size()>0){
                    binding.imageButtonPlay.setVisibility(View.VISIBLE);
                    if (videoEntities.size()>2) {
                        binding.imageButtonShuffle.setVisibility(View.VISIBLE);
                    } else {
                        binding.imageButtonShuffle.setVisibility(View.GONE);
                    }
                } else {
                    binding.imageButtonPlay.setVisibility(View.GONE);
                    binding.imageButtonShuffle.setVisibility(View.GONE);
                }
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

        viewModel.isPlayling().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                isPlaying = value;
                exoPlayer.setPlayWhenReady(isPlaying);
                if (isPlaying){
                    binding.imageButtonPlay.setImageResource(R.drawable.ic_pause_circular_light);
                } else {
                    binding.imageButtonPlay.setImageResource(R.drawable.ic_play_circular_white);
                }

                if (isPlaying && playerPosition== RecyclerView.NO_POSITION){
                    viewModel.setPlayerPosition(0);
                }
            }
        });

        viewModel.getPlayerPosition().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer value) {
                playerPosition = value;
                recyclerAdapter.setPlayerPosition(playerPosition);
            }
        });
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Logger.e(TAG, "onScrollStateChanged: " + newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int totalItemCount = layoutManager.getItemCount();
            int visibleItemCount = layoutManager.getChildCount();
            int currentPosition = layoutManager.findLastVisibleItemPosition();
            int remainingItems = totalItemCount - currentPosition;
            if (dy > 0 && remainingItems < visibleItemCount) {
                /*viewModel.getMoreData();*/
            }
        }
    };

    @Override
    public void setListeners() {
        binding.imageButtonPlay.setOnClickListener(this);
        binding.imageButtonShuffle.setOnClickListener(this);
        binding.imageButtonAddNewSong.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        playerView.onResume();
        viewModel.getFreshData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playerView.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.imageButtonAddNewSong)){
            Intent intent = new Intent(this, VideosActivity.class);
            intent.putExtra(ARG_PARAM1, model);
            startActivity(intent);

        } else if (v.equals(binding.imageButtonPlay)) {
            viewModel.setIsPlaying(!isPlaying);
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
            Logger.e(TAG, "onTimelineChanged: " + " " + reason);
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Logger.e(TAG, "onTracksChanged: " + trackSelections.length);
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Logger.e(TAG, "onLoadingChanged: " + " " + isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Logger.e(TAG, "onPlayerStateChanged: " + playWhenReady + ", " + playbackState);
            /*if (playWhenReady && playbackState == Player.STATE_READY) {
                // media actually playing
                viewModel.setIsPlaying(true);
            } else {
                // player paused in any state
                viewModel.setIsPlaying(false);
            }*/
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    Logger.e(TAG, "onPlayerStateChanged: buffering");
                    int percentageBuffered = exoPlayer.getBufferedPercentage();
                    Logger.e(TAG, percentageBuffered + "");
                    break;
                case Player.STATE_ENDED:
                    Logger.e(TAG, "onPlayerStateChanged: ended");
                    if (isPlaying){
                        viewModel.setIsPlaying(false);
                    }
                    break;
                case Player.STATE_IDLE:
                    Logger.e(TAG, "onPlayerStateChanged: idle");
                    break;
                case Player.STATE_READY:
                    Logger.e(TAG, "onPlayerStateChanged: ready");
                    break;
                default:
                    break;
            }
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
            int latestWindowIndex = exoPlayer.getCurrentWindowIndex();
            if (latestWindowIndex != playerPosition) {
                // item selected in playlist has changed, handle here
                /*viewModel.setPlayerPosition(latestWindowIndex);*/
                viewModel.setPlayerPosition(latestWindowIndex);
                Logger.e(TAG, "onPositionDiscontinuity: " + latestWindowIndex);
                // ...
            }
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
