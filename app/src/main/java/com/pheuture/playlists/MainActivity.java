package com.pheuture.playlists;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
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
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.StringUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.security.RunAs;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private NavController navController;
    private MainViewModel viewModel;
    private SimpleExoPlayer exoPlayer1;
    private SimpleExoPlayer exoPlayer2;
    private PlayerView playerView;
    private PlaylistEntity playlist;
    private List<VideoEntity> videos;
    private ConcatenatingMediaSource concatenatedSource;
    private List<MediaSource> mediaSources;
    private ConstraintLayout constraintLayoutBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private static int currentPlayer = RecyclerView.NO_POSITION;
    private static int currentVideoPosition;
    private Handler handler = new Handler();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BottomNavigationView navView = findViewById(R.id.nav_view);

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

        constraintLayoutBottomSheet = binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer;
        playerView = binding.layoutBottomSheet.playerView;

        bottomSheetBehavior = BottomSheetBehavior.from(constraintLayoutBottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        exoPlayer1 = viewModel.getExoPlayer1();
        exoPlayer2 = viewModel.getExoPlayer2();

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
                    resetAllPlayers();

                    //reset current video position to play as -1
                    currentVideoPosition = RecyclerView.NO_POSITION;

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

                    exoPlayer1.prepare(mediaSources.get(++currentVideoPosition));
                    exoPlayer1.setPlayWhenReady(true);
                    playerView.setPlayer(exoPlayer1);
                    currentPlayer = 1;

                    binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer.setVisibility(View.VISIBLE);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    if (playlist != null && !StringUtils.isEmpty(playlist.getPlaylistName())) {
                        binding.layoutBottomSheet.textViewTitle.setText(playlist.getPlaylistName());
                        binding.layoutBottomSheet.textViewCreator.setText(ApiConstant.DUMMY_USER);
                    } else {
                        binding.layoutBottomSheet.textViewTitle.setText(videos.get(0).getVideoName());
                        binding.layoutBottomSheet.textViewCreator.setText(videos.get(0).getVideoDescription());
                    }

                    if (mediaSources.size()>1) {
                        handler.removeCallbacks(runnable);
                        handler.postDelayed(runnable, 2000);
                    }
                }
            }
        });
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                //no media available to play
                if ((mediaSources.size() - 1) == currentVideoPosition){
                    handler.removeCallbacks(runnable);
                    return;
                }

                if (currentPlayer == 1) {
                    long totalDuration = exoPlayer1.getDuration();
                    long currentDuration = exoPlayer1.getCurrentPosition();

                    if(totalDuration <= 0 || currentDuration <= 0 || (totalDuration - currentDuration) > 2000){
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                float oldVolume = exoPlayer1.getVolume();
                                float newVolume = (oldVolume>0) ? oldVolume/2 : 0;

                                exoPlayer2.setVolume(newVolume);
                                exoPlayer2.prepare(mediaSources.get(++currentVideoPosition));
                                exoPlayer2.setPlayWhenReady(true);
                                playerView.setPlayer(exoPlayer2);
                                currentPlayer = 2;

                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        exoPlayer1.setVolume(newVolume);
                                        exoPlayer2.setVolume(oldVolume);
                                    }
                                };
                                new Handler().postDelayed(runnable, 1000);
                            } catch (Exception e) {
                                Logger.e(TAG, e.toString());
                            }
                        }
                    });
                } else {
                    long totalDuration = exoPlayer2.getDuration();
                    long currentDuration = exoPlayer2.getCurrentPosition();

                    if(totalDuration <= 0 || currentDuration <= 0 || (totalDuration - currentDuration) > 2000){
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                float oldVolume = exoPlayer2.getVolume();
                                float newVolume = (oldVolume>0) ? oldVolume/2 : 0;

                                exoPlayer1.setVolume(newVolume);
                                exoPlayer1.prepare(mediaSources.get(++currentVideoPosition));
                                exoPlayer1.setPlayWhenReady(true);
                                playerView.setPlayer(exoPlayer1);
                                currentPlayer = 1;

                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        exoPlayer2.setVolume(newVolume);
                                        exoPlayer1.setVolume(oldVolume);
                                    }
                                };
                                new Handler().postDelayed(runnable, 1000);
                            } catch (Exception e) {
                                Logger.e(TAG, e.toString());
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Logger.e(TAG, e.toString());
            }
            handler.postDelayed(runnable, 2000);
        }
    };

    private void resetAllPlayers(){
        try {
            exoPlayer1.setPlayWhenReady(false);
            exoPlayer2.setPlayWhenReady(false);
            handler.removeCallbacks(runnable);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void setListeners() {
        exoPlayer1.addListener(playerListener1);
        exoPlayer2.addListener(playerListener2);
        binding.layoutBottomSheet.imageVIewClose.setOnClickListener(this);
    }

    private Player.EventListener playerListener1 = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
            Logger.e(TAG, "onTimelineChanged: " + reason);
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Logger.e(TAG, "onTracksChanged: " + trackSelections.length);
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
                    int percentageBuffered = exoPlayer1.getBufferedPercentage();
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
            String data = shuffleModeEnabled?"Enabled":"Disabled";
            Toast.makeText(MainActivity.this, "Shuffle " + data , Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            /*Logger.e(TAG, "onPlayerError: " + error.getMessage());*/
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            int latestWindowIndex = exoPlayer1.getCurrentWindowIndex();
            Logger.e(TAG, "onPositionDiscontinuity: " + latestWindowIndex);
            if (playlist != null && !StringUtils.isEmpty(playlist.getPlaylistName())) {
                binding.layoutBottomSheet.textViewTitle.setText(playlist.getPlaylistName());
                binding.layoutBottomSheet.textViewCreator.setText(ApiConstant.DUMMY_USER);
            } else {
                binding.layoutBottomSheet.textViewTitle.setText(videos.get(latestWindowIndex).getVideoName());
                binding.layoutBottomSheet.textViewCreator.setText(videos.get(latestWindowIndex).getVideoDescription());
            }

            /*if (latestWindowIndex != playerPosition) {
                // item selected in playlist has changed, handle here
                viewModel.setPlayerPosition(latestWindowIndex);
                viewModel.setPlayerPosition(latestWindowIndex);
                Logger.e(TAG, "onPositionDiscontinuity: " + latestWindowIndex);
                // ...
            }*/
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Logger.e(TAG, "onPlaybackParametersChanged");
        }

        @Override
        public void onSeekProcessed() {
            Logger.e(TAG, "onSeekProcessed");
        }
    };

    private Player.EventListener playerListener2 = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
            Logger.e(TAG, "onTimelineChanged: " + reason);
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Logger.e(TAG, "onTracksChanged: " + trackSelections.length);
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            /*Logger.e(TAG, "onLoadingChanged: " + isLoading);*/
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Logger.e(TAG, "ExoPlayer2: duration: " + exoPlayer2.getDuration() + "currentPosition: " + exoPlayer2.getCurrentPosition());
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
                    int percentageBuffered = exoPlayer1.getBufferedPercentage();
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
            String data = shuffleModeEnabled?"Enabled":"Disabled";
            Toast.makeText(MainActivity.this, "Shuffle " + data , Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            /*Logger.e(TAG, "onPlayerError: " + error.getMessage());*/
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            int latestWindowIndex = exoPlayer1.getCurrentWindowIndex();
            Logger.e(TAG, "onPositionDiscontinuity: " + latestWindowIndex);

            if (playlist != null && !StringUtils.isEmpty(playlist.getPlaylistName())) {
                binding.layoutBottomSheet.textViewTitle.setText(playlist.getPlaylistName());
                binding.layoutBottomSheet.textViewCreator.setText(ApiConstant.DUMMY_USER);
            } else {
                binding.layoutBottomSheet.textViewTitle.setText(videos.get(latestWindowIndex).getVideoName());
                binding.layoutBottomSheet.textViewCreator.setText(videos.get(latestWindowIndex).getVideoDescription());
            }

            /*if (latestWindowIndex != playerPosition) {
                // item selected in playlist has changed, handle here
                viewModel.setPlayerPosition(latestWindowIndex);
                viewModel.setPlayerPosition(latestWindowIndex);
                Logger.e(TAG, "onPositionDiscontinuity: " + latestWindowIndex);
                // ...
            }*/
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Logger.e(TAG, "onPlaybackParametersChanged");
        }

        @Override
        public void onSeekProcessed() {
            Logger.e(TAG, "onSeekProcessed");
        }
    };

    @Override
    public void onClick(View v) {
        if (v.equals(binding.layoutBottomSheet.imageVIewClose)){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            binding.navView.setVisibility(View.VISIBLE);
            resetAllPlayers();
        }
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
        exoPlayer1.release();
        exoPlayer2.release();
    }

    BottomSheetCallback  bottomSheetCallback = new BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Logger.e(TAG, "onStateChanged: " + newState);
            switch (newState) {
                case BottomSheetBehavior.STATE_HIDDEN:
                    exoPlayer1.setPlayWhenReady(false);
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    break;
                case BottomSheetBehavior.STATE_DRAGGING:
                    break;
                case BottomSheetBehavior.STATE_SETTLING:
                    /*bottomSheetBehavior.setHideable(false);*/
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View view, float v) {

        }
    };

    public void toggleShuffleMode() {
        exoPlayer1.setShuffleModeEnabled(!exoPlayer1.getShuffleModeEnabled());
    }
}
