package com.pheuture.playlists;

import android.app.DownloadManager;
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
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.offline.OfflineMediaEntity;
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

import java.io.File;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private NavController navController;
    private MainViewModel viewModel;
    private SimpleExoPlayer exoPlayer1;
    private SimpleExoPlayer exoPlayer2;
    private PlayerView playerView;
    private PlaylistEntity playlistToPlay;
    private List<PlaylistMediaEntity> mediaToPlay;
    private ConstraintLayout constraintLayoutBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private static int currentPlayer;
    private static int currentMediaPosition;
    private Handler handler = new Handler();
    private long totalDurationOfCurrentMedia = 0;
    private long currentDurationOfCurrentMedia = 0;
    private float oldVolume;
    private float newVolume;
    private int defaultRemainingSecToSwitchPlayer = 4000;
    private int defaultTimerSec = 1000;
    private Handler player1handler = new Handler();
    private Handler player2handler = new Handler();
    private Runnable player1runnable = new Runnable() {
        @Override
        public void run() {
            exoPlayer1.setVolume(newVolume);
            exoPlayer2.setVolume(oldVolume);
        }
    };
    private Runnable player2Runnable = new Runnable() {
        @Override
        public void run() {
            exoPlayer2.setVolume(newVolume);
            exoPlayer1.setVolume(oldVolume);
        }
    };

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
        proceedWithPermissions(null, true);

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
                playlistToPlay = playlistEntity;
            }
        });

        viewModel.getPlaylistMediaEntities().observe(this, new Observer<List<PlaylistMediaEntity>>() {
            @Override
            public void onChanged(List<PlaylistMediaEntity> videoEntities) {
                resetAllPlayers();

                mediaToPlay = videoEntities;

                if (mediaToPlay.size()>0){
                    loadNextVideoIn(exoPlayer1);
                    exoPlayer1.setPlayWhenReady(true);
                    playerView.setPlayer(exoPlayer1);
                    currentPlayer = 1;

                    //set progress
                    totalDurationOfCurrentMedia = exoPlayer1.getDuration();
                    currentDurationOfCurrentMedia = exoPlayer1.getCurrentPosition();
                    binding.layoutBottomSheet.progressBar.setProgress(0);

                    //if more media available to play
                    if ((mediaToPlay.size()-1)> currentMediaPosition) {
                        binding.layoutBottomSheet.imageViewNext.setVisibility(View.VISIBLE);
                    } else {
                        binding.layoutBottomSheet.imageViewNext.setVisibility(View.GONE);
                    }

                    binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer.setVisibility(View.VISIBLE);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    if (playlistToPlay != null && !StringUtils.isEmpty(playlistToPlay.getPlaylistName())) {
                        binding.layoutBottomSheet.textViewTitle.setText(playlistToPlay.getPlaylistName());
                        binding.layoutBottomSheet.textViewCreator.setText(ApiConstant.DUMMY_USER);
                    } else {
                        binding.layoutBottomSheet.textViewTitle.setText(mediaToPlay.get(0).getVideoName());
                        binding.layoutBottomSheet.textViewCreator.setText(mediaToPlay.get(0).getVideoDescription());
                    }

                    //set handler
                    handler.postDelayed(runnable, defaultTimerSec);
                }
            }
        });
    }

    private void loadNextVideoIn(SimpleExoPlayer exoPlayer) {
        MediaEntity media = mediaToPlay.get(++currentMediaPosition);
        Uri mediaUri;

        OfflineMediaEntity offlineMedia = viewModel.getOfflineMediaForMediaID(media.getMediaID());
        if (offlineMedia != null && offlineMedia.getDownloadStatus()== DownloadManager.STATUS_SUCCESSFUL) {
            File file = new File(offlineMedia.getDownloadedFilePath());
            mediaUri = Uri.fromFile(file);
            Logger.e(TAG, "media loading Offline from: " + mediaUri);

        } else {
            mediaUri = Uri.parse(media.getVideoUrl());
            Logger.e(TAG, "media loading Online from: " + mediaUri);
        }

        MediaSource mediaSource;
        mediaSource = new ProgressiveMediaSource.Factory(viewModel.getDataSourceFactory())
                .createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (currentPlayer == 1) {
                    totalDurationOfCurrentMedia = exoPlayer1.getDuration();
                    currentDurationOfCurrentMedia = exoPlayer1.getCurrentPosition();

                    //if more media available to play
                    if ((mediaToPlay.size()-1)> currentMediaPosition) {
                        //if remaining duration of current media <= 2sec
                        if ((totalDurationOfCurrentMedia - currentDurationOfCurrentMedia) <= defaultRemainingSecToSwitchPlayer) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        oldVolume = exoPlayer1.getVolume();
                                        newVolume = (oldVolume>0) ? oldVolume/2 : 0;

                                        exoPlayer2.setVolume(newVolume);
                                        loadNextVideoIn(exoPlayer2);
                                        exoPlayer2.setPlayWhenReady(true);
                                        playerView.setPlayer(exoPlayer2);
                                        currentPlayer = 2;

                                        //get progress
                                        totalDurationOfCurrentMedia = exoPlayer1.getDuration();
                                        currentDurationOfCurrentMedia = exoPlayer1.getCurrentPosition();

                                        player1handler.postDelayed(player1runnable, defaultTimerSec);
                                    } catch (Exception e) {
                                        Logger.e(TAG, e.toString());
                                    }
                                }
                            });
                        }
                    }
                } else if (currentPlayer == 2) {
                    totalDurationOfCurrentMedia = exoPlayer2.getDuration();
                    currentDurationOfCurrentMedia = exoPlayer2.getCurrentPosition();

                    //if more media available to play
                    if ((mediaToPlay.size()-1)> currentMediaPosition){
                        //if remaining duration of current media <= 2sec
                        if ((totalDurationOfCurrentMedia - currentDurationOfCurrentMedia) <= defaultRemainingSecToSwitchPlayer) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        oldVolume = exoPlayer2.getVolume();
                                        newVolume = (oldVolume>0) ? oldVolume/2 : 0;

                                        exoPlayer1.setVolume(newVolume);
                                        loadNextVideoIn(exoPlayer1);
                                        exoPlayer1.setPlayWhenReady(true);
                                        playerView.setPlayer(exoPlayer1);
                                        currentPlayer = 1;

                                        //get progress
                                        totalDurationOfCurrentMedia = exoPlayer2.getDuration();
                                        currentDurationOfCurrentMedia = exoPlayer2.getCurrentPosition();

                                        player2handler.postDelayed(player2Runnable, defaultTimerSec);
                                    } catch (Exception e) {
                                        Logger.e(TAG, e.toString());
                                    }
                                }
                            });
                        }
                    }
                }

                //update progress
                binding.layoutBottomSheet.progressBar.setProgress(calculatePercentage(totalDurationOfCurrentMedia, currentDurationOfCurrentMedia));
            } catch (Exception e) {
                Logger.e(TAG, e.toString());
            }

            //check if next button is not hidden by handler that change next button visibility after
            // a while
            if (binding.layoutBottomSheet.imageViewNext.getVisibility()==View.VISIBLE) {
                //if more media available to play
                if ((mediaToPlay.size()-1)> currentMediaPosition) {
                    binding.layoutBottomSheet.imageViewNext.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutBottomSheet.imageViewNext.setVisibility(View.GONE);
                }
            }

            //if more media available to play or current media playback time is remaining
            if ((mediaToPlay.size()-1)> currentMediaPosition || totalDurationOfCurrentMedia != currentDurationOfCurrentMedia) {
                handler.postDelayed(runnable, defaultTimerSec);
            }
        }
    };

    private int calculatePercentage(long totalDuration, long currentDuration) {
        if (totalDuration == 0){
            return 0;
        }
        return (int)((currentDuration * 100)/totalDuration);
    }

    private void resetAllPlayers(){
        try {
            exoPlayer1.setPlayWhenReady(false);
            exoPlayer2.setPlayWhenReady(false);
            handler.removeCallbacks(runnable);
            //reset current video position to play as -1
            currentPlayer = RecyclerView.NO_POSITION;
            currentMediaPosition = RecyclerView.NO_POSITION;
        } catch (Exception ignore) {
        }
    }

    @Override
    public void setListeners() {
        exoPlayer1.addListener(playerListener1);
        exoPlayer2.addListener(playerListener2);
        binding.layoutBottomSheet.imageViewTogglePlay.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewNext.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewClose.setOnClickListener(this);
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
            if (currentPlayer == 1) {
                if (playWhenReady) {
                    // media actually playing
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                } else {
                    // player paused in any state
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                }

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
                        /*Logger.e(TAG, "onPlayerStateChanged: idle");*/
                        break;
                    case Player.STATE_READY:
                        /*Logger.e(TAG, "onPlayerStateChanged: ready");*/
                        break;
                    default:
                        break;
                }
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
            if (playlistToPlay != null && !StringUtils.isEmpty(playlistToPlay.getPlaylistName())) {
                binding.layoutBottomSheet.textViewTitle.setText(playlistToPlay.getPlaylistName());
                binding.layoutBottomSheet.textViewCreator.setText(ApiConstant.DUMMY_USER);
            } else {
                binding.layoutBottomSheet.textViewTitle.setText(mediaToPlay.get(latestWindowIndex).getVideoName());
                binding.layoutBottomSheet.textViewCreator.setText(mediaToPlay.get(latestWindowIndex).getVideoDescription());
            }

            /*if (latestWindowIndex != playerPosition) {
                // item selected in playlistToPlay has changed, handle here
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

            if (currentPlayer == 2) {
                if (playWhenReady) {
                    // media actually playing
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                } else {
                    // player paused in any state
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                }
            }

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
                    /*Logger.e(TAG, "onPlayerStateChanged: idle");*/
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
            if (playlistToPlay != null && !StringUtils.isEmpty(playlistToPlay.getPlaylistName())) {
                binding.layoutBottomSheet.textViewTitle.setText(playlistToPlay.getPlaylistName());
                binding.layoutBottomSheet.textViewCreator.setText(ApiConstant.DUMMY_USER);
            } else {
                binding.layoutBottomSheet.textViewTitle.setText(mediaToPlay.get(latestWindowIndex).getVideoName());
                binding.layoutBottomSheet.textViewCreator.setText(mediaToPlay.get(latestWindowIndex).getVideoDescription());
            }
            /*if (latestWindowIndex != playerPosition) {
                // item selected in playlistToPlay has changed, handle here
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
        if (v.equals(binding.layoutBottomSheet.imageViewTogglePlay)){
            if (currentPlayer == 1){
                if (exoPlayer1.getPlayWhenReady()) {
                    // media actually playing, so stop it
                    exoPlayer1.setPlayWhenReady(false);
                } else {
                    // player paused in any state
                    exoPlayer1.setPlayWhenReady(true);
                }
            } else if (currentPlayer == 2){
                if (exoPlayer2.getPlayWhenReady()) {
                    // media actually playing, so stop it
                    exoPlayer2.setPlayWhenReady(false);
                } else {
                    // player paused in any state
                    exoPlayer2.setPlayWhenReady(true);
                }
            }

        } else if (v.equals(binding.layoutBottomSheet.imageViewNext)){
            //if more media available to play & no pending callbacks
            if ((mediaToPlay.size()-1)> currentMediaPosition){
                if (currentPlayer == 1){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                oldVolume = exoPlayer1.getVolume();
                                newVolume = (oldVolume>0) ? oldVolume/2 : 0;

                                exoPlayer2.setVolume(newVolume);
                                loadNextVideoIn(exoPlayer2);
                                exoPlayer2.setPlayWhenReady(true);
                                playerView.setPlayer(exoPlayer2);
                                currentPlayer = 2;

                                totalDurationOfCurrentMedia = exoPlayer2.getDuration();
                                currentDurationOfCurrentMedia = exoPlayer2.getCurrentPosition();

                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        exoPlayer2.setVolume(oldVolume);
                                        exoPlayer1.setPlayWhenReady(false);
                                    }
                                };
                                player1handler.postDelayed(runnable, defaultTimerSec);
                            } catch (Exception e) {
                                Logger.e(TAG, e.toString());
                            }
                        }
                    });
                } else  if (currentPlayer == 2) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                oldVolume = exoPlayer2.getVolume();
                                newVolume = (oldVolume>0) ? oldVolume/2 : 0;

                                exoPlayer1.setVolume(newVolume);
                                loadNextVideoIn(exoPlayer1);
                                exoPlayer1.setPlayWhenReady(true);
                                playerView.setPlayer(exoPlayer1);
                                currentPlayer = 1;

                                totalDurationOfCurrentMedia = exoPlayer1.getDuration();
                                currentDurationOfCurrentMedia = exoPlayer1.getCurrentPosition();

                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        exoPlayer1.setVolume(oldVolume);
                                        exoPlayer2.setPlayWhenReady(false);
                                    }
                                };
                                player2handler.postDelayed(runnable, defaultTimerSec);
                            } catch (Exception e) {
                                Logger.e(TAG, e.toString());
                            }
                        }
                    });
                }
                //update progress bar
                binding.layoutBottomSheet.progressBar.setProgress(calculatePercentage(totalDurationOfCurrentMedia, currentDurationOfCurrentMedia));
            }

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Logger.e(TAG, "next allowed");
                    //if more media available to play
                    if ((mediaToPlay.size()-1)> currentMediaPosition) {
                        binding.layoutBottomSheet.imageViewNext.setVisibility(View.VISIBLE);
                    } else {
                        binding.layoutBottomSheet.imageViewNext.setVisibility(View.GONE);
                    }
                }
            };
            new Handler().postDelayed(runnable, 1500);

            //hide next button to complete all pending callbacks before allowing to next again
            binding.layoutBottomSheet.imageViewNext.setVisibility(View.GONE);
            Logger.e(TAG, "next hidden");

        } else if (v.equals(binding.layoutBottomSheet.imageViewClose)){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            binding.navView.setVisibility(View.VISIBLE);
            resetAllPlayers();
        }
    }

    public void setMedia(PlaylistEntity playlistEntity,  List<PlaylistMediaEntity> playlistMediaEntities){
        viewModel.setPlaylist(playlistEntity);
        viewModel.setPlaylistMediaEntities(playlistMediaEntities);
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

    private BottomSheetCallback  bottomSheetCallback = new BottomSheetCallback() {
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
