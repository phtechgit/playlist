package com.pheuture.playlists;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.pheuture.playlists.databinding.ActivityMainBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.Logger;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private NavController navController;
    private MainViewModel viewModel;
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;
    private PlaylistEntity playlist;
    private List<VideoEntity> videos;
    private ConcatenatingMediaSource concatenatedSource;
    private List<MediaSource> mediaSources;
    private LinearLayout linearLayoutBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_playlists, R.id.navigation_trending, R.id.navigation_settings)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public void initializations() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        linearLayoutBottomSheet = binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer;
        playerView = binding.layoutBottomSheet.playerView;

         bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBottomSheet);
         bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        exoPlayer = viewModel.getExoPlayer();
        playerView.setPlayer(exoPlayer);

        viewModel.getPlaylist().observe(this, new Observer<PlaylistEntity>() {
            @Override
            public void onChanged(PlaylistEntity playlistEntity) {
                playlist = playlistEntity;
            }
        });

        viewModel.getVideos().observe(this, new Observer<List<VideoEntity>>() {
            @Override
            public void onChanged(List<VideoEntity> videoEntities) {
                videos = videoEntities;

                if (videos.size()>0){
                    //create single instance of media source/playlist
                    concatenatedSource = new ConcatenatingMediaSource(true);
                    mediaSources = new ArrayList<>();
                    for (int i=0; i<videos.size() ; i++){
                        VideoEntity model = videoEntities.get(i);

                        MediaSource mediaSource = new ProgressiveMediaSource.Factory(viewModel.getDataSourceFactory())
                                .createMediaSource(Uri.parse(model.getVideoUrl()));
                        mediaSources.add(mediaSource);
                    }

                    concatenatedSource.addMediaSources(mediaSources);

                    exoPlayer.prepare(concatenatedSource);
                    exoPlayer.setPlayWhenReady(true);

                    binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void setListeners() {
        exoPlayer.addListener(playerListener);
    }

    private Player.EventListener playerListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
            /*Logger.e(TAG, "onTimelineChanged: " + reason);*/
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            /*Logger.e(TAG, "onTracksChanged: " + trackSelections.length);*/
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            /*Logger.e(TAG, "onLoadingChanged: " + isLoading);*/
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            /*Logger.e(TAG, "onPlayerStateChanged: " + playWhenReady + ", " + playbackState);*/

            /*if (playWhenReady && playbackState == Player.STATE_READY) {
                // media actually playing
                viewModel.setIsPlaying(true);
            } else {
                // player paused in any state
                viewModel.setIsPlaying(false);
            }*/

            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    /*Logger.e(TAG, "onPlayerStateChanged: buffering");
                    int percentageBuffered = exoPlayer.getBufferedPercentage();
                    Logger.e(TAG, percentageBuffered + "");*/
                    break;
                case Player.STATE_ENDED:
                    /*Logger.e(TAG, "onPlayerStateChanged: ended");*/
                    /*if (isPlaying){
                        viewModel.setIsPlaying(false);
                    }*/
                    break;
                case Player.STATE_IDLE:
                    /*Logger.e(TAG, "onPlayerStateChanged: idle")*/;
                    break;
                case Player.STATE_READY:
                    /*Logger.e(TAG, "onPlayerStateChanged: ready");*/
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
            /*Logger.e(TAG, "onPlayerError: " + error.getMessage());*/
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            /*int latestWindowIndex = exoPlayer.getCurrentWindowIndex();
            if (latestWindowIndex != playerPosition) {
                // item selected in playlist has changed, handle here
                *//*viewModel.setPlayerPosition(latestWindowIndex);*//*
                viewModel.setPlayerPosition(latestWindowIndex);
                Logger.e(TAG, "onPositionDiscontinuity: " + latestWindowIndex);
                // ...
            }*/
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {
            Logger.e(TAG, "onSeekProcessed");
        }
    };

    @Override
    public void onClick(View v) {

    }

    public void setMedia(PlaylistEntity playlist, List<VideoEntity> videoEntityList){
        viewModel.setPlaylist(playlist);
        viewModel.setVideos( videoEntityList);
    }

    @Override
    public void onBackPressed() {
        if (!navController.popBackStack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer!=null){
            exoPlayer.release();
        }
    }

    BottomSheetCallback  bottomSheetCallback = new BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Logger.e(TAG, "onStateChanged: " + newState);
            switch (newState) {
                case BottomSheetBehavior.STATE_HIDDEN:
                    binding.navView.setVisibility(View.VISIBLE);
                    exoPlayer.setPlayWhenReady(false);
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    binding.navView.setVisibility(View.GONE);
                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    binding.navView.setVisibility(View.VISIBLE);
                    break;
                case BottomSheetBehavior.STATE_DRAGGING:
                    break;
                case BottomSheetBehavior.STATE_SETTLING:
                    /*bottomSheetBehavior.setHideable(false);*/
                    break;
                case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    binding.navView.setVisibility(View.VISIBLE);
                    bottomSheetBehavior.setHideable(true);
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View view, float v) {

        }
    };
}
