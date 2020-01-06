package com.pheuture.playlists;

import android.app.DownloadManager;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
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
import com.pheuture.playlists.utils.AlerterUtils;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.SharedPrefsUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private int EXO_PLAYER_1 = 1;
    private int EXO_PLAYER_2 = 2;
    private PlayerView playerView;
    private static int currentPlayer;
    private static int currentMediaPosition;
    private List<PlaylistMediaEntity> mediaToPlay;
    private BottomSheetBehavior bottomSheetBehavior;
    private Handler timerHandler = new Handler();
    private long totalDurationOfCurrentMedia = 0;
    private long currentDurationOfCurrentMedia = 0;
    private int defaultTimerSec = 1000;
    private AudioManager audioManager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.master_menu, menu);
        return true;
    }

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
        BottomNavigationView bottomNavView = findViewById(R.id.bottomNav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_playlists, R.id.navigation_trending, R.id.navigation_settings)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavView, navController);
    }

    @Override
    public void initializations() {
        proceedWithPermissions(null, true);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        binding.toolbar.setNavigationIcon(R.drawable.ic_add_light);

        playerView = binding.layoutBottomSheet.playerView;

        bottomSheetBehavior = BottomSheetBehavior.from( binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer);
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        exoPlayer1 = viewModel.getExoPlayer1();
        exoPlayer2 = viewModel.getExoPlayer2();

        viewModel.getPlaylistMediaEntities().observe(this, new Observer<List<PlaylistMediaEntity>>() {
            @Override
            public void onChanged(List<PlaylistMediaEntity> videoEntities) {
                resetAllPlayers();

                mediaToPlay = videoEntities;

                if (mediaToPlay.size()>0){
                    loadNextVideoIn(EXO_PLAYER_1);
                }
            }
        });

        //set timerHandler that runs every second to update progress and check if need to change track
        // for crossFade feature
        timerHandler.postDelayed(timerRunnable, defaultTimerSec);
    }

    @Override
    public void setListeners() {
        exoPlayer1.addListener(playerListener1);
        exoPlayer2.addListener(playerListener2);
        binding.layoutBottomSheet.imageViewTogglePlay.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewNext.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.layoutBottomSheet.imageViewTogglePlay)){
            SimpleExoPlayer exoPlayer = null;
            if (currentPlayer == EXO_PLAYER_1){
                exoPlayer = exoPlayer1;
            } else if (currentPlayer == EXO_PLAYER_2){
                exoPlayer = exoPlayer2;
            }
            if (exoPlayer!=null) {
                if (exoPlayer.getPlayWhenReady()){
                    exoPlayer1.setPlayWhenReady(false);
                    exoPlayer2.setPlayWhenReady(false);
                } else {
                    exoPlayer1.setPlayWhenReady(true);
                    exoPlayer2.setPlayWhenReady(true);
                }
            }
        } else if (v.equals(binding.layoutBottomSheet.imageViewNext)){
            //if more media available to play & no pending callbacks
            if ((mediaToPlay.size() - 1)> currentMediaPosition){
                if (currentPlayer == EXO_PLAYER_1) {
                    exoPlayer2.setVolume(1f);
                    loadNextVideoIn(EXO_PLAYER_2);
                } else {
                    exoPlayer1.setVolume(1f);
                    loadNextVideoIn(EXO_PLAYER_1);
                }
            }

        } else if (v.equals(binding.layoutBottomSheet.imageViewClose)){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            binding.bottomNavView.setVisibility(View.VISIBLE);
            resetAllPlayers();
        }
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (currentPlayer == EXO_PLAYER_1) {
                    totalDurationOfCurrentMedia = exoPlayer1.getDuration();
                    currentDurationOfCurrentMedia = exoPlayer1.getCurrentPosition();

                    int crossFadeValue = (SharedPrefsUtils.getIntegerPreference(MainActivity.this,
                            Constants.CROSS_FADE_VALUE, 0) * 1000);

                    //increase volume if player
                    if (mediaToPlay.size()>1 && exoPlayer1.getVolume()<1f) {
                        exoPlayer1.setVolume(currentDurationOfCurrentMedia/crossFadeValue);
                    }

                    //if more media available to play
                    if ((mediaToPlay.size()-1)> currentMediaPosition) {
                        if (totalDurationOfCurrentMedia > 0 && (totalDurationOfCurrentMedia
                                - currentDurationOfCurrentMedia) <= crossFadeValue) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    exoPlayer2.setVolume(0f);
                                    loadNextVideoIn(EXO_PLAYER_2);
                                }
                            });
                        }
                    }
                } else if (currentPlayer == EXO_PLAYER_2) {
                    totalDurationOfCurrentMedia = exoPlayer2.getDuration();
                    currentDurationOfCurrentMedia = exoPlayer2.getCurrentPosition();

                    int crossFadeValue = (SharedPrefsUtils.getIntegerPreference(MainActivity.this,
                            Constants.CROSS_FADE_VALUE, 0) * 1000);

                    //increase volume if player
                    if (mediaToPlay.size()>1 && exoPlayer2.getVolume()<1f) {
                        exoPlayer2.setVolume(currentDurationOfCurrentMedia/crossFadeValue);
                    }

                    //if more media available to play
                    if ((mediaToPlay.size()-1)> currentMediaPosition){
                        //if remaining duration of current media <= 2sec
                        if (totalDurationOfCurrentMedia > 0 && (totalDurationOfCurrentMedia
                                - currentDurationOfCurrentMedia) <= crossFadeValue) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    exoPlayer1.setVolume(0f);
                                    loadNextVideoIn(EXO_PLAYER_1);
                                }
                            });
                        }
                    }
                }

                Logger.e(TAG, "totalDurationOfCurrentMedia:" + totalDurationOfCurrentMedia + ", currentDurationOfCurrentMedia:" + currentDurationOfCurrentMedia + ", crossFadeValue:" + (SharedPrefsUtils.getIntegerPreference(MainActivity.this,
                        Constants.CROSS_FADE_VALUE, 0) * 1000));
                //update progress
                binding.layoutBottomSheet.progressBar.setProgress(calculatePercentage(totalDurationOfCurrentMedia,
                        currentDurationOfCurrentMedia));
            } catch (Exception e) {
                Logger.e(TAG, e.toString());
            }

            timerHandler.postDelayed(timerRunnable, defaultTimerSec);
        }
    };

    private void resetAllPlayers(){
        try {
            exoPlayer1.setPlayWhenReady(false);
            exoPlayer2.setPlayWhenReady(false);

            //reset current video position to play as -1
            currentPlayer = RecyclerView.NO_POSITION;
            currentMediaPosition = RecyclerView.NO_POSITION;
        } catch (Exception ignore) {
        }
    }

    public void setMedia(PlaylistEntity playlistEntity,  List<PlaylistMediaEntity> playlistMediaEntities){
        viewModel.setPlaylist(playlistEntity);
        viewModel.setPlaylistMediaEntities(playlistMediaEntities);
    }

    private BottomSheetCallback  bottomSheetCallback = new BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Logger.e(TAG, "onStateChanged: " + newState);
            switch (newState) {
                case BottomSheetBehavior.STATE_HIDDEN:
                    resetAllPlayers();
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

    private void loadNextVideoIn(int player) {
        currentPlayer = player;

        SimpleExoPlayer exoPlayer;
        if (currentPlayer == EXO_PLAYER_1){
            exoPlayer = exoPlayer1;
        } else {
            exoPlayer = exoPlayer2;
        }

        MediaEntity media = mediaToPlay.get(++currentMediaPosition);
        Uri mediaUri;

        OfflineMediaEntity offlineMedia = viewModel.getOfflineMediaForMediaID(media.getMediaID());
        if (offlineMedia != null && offlineMedia.getDownloadStatus()== DownloadManager.STATUS_SUCCESSFUL) {
            File file = new File(offlineMedia.getDownloadedFilePath());
            mediaUri = Uri.fromFile(file);
            Logger.e(TAG, "media loading Offline from: " + mediaUri);

        } else {
            mediaUri = Uri.parse(media.getMediaUrl());
            Logger.e(TAG, "media loading Online from: " + mediaUri);
        }

        MediaSource mediaSource = new ProgressiveMediaSource
                .Factory(viewModel.getDataSourceFactory()).createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        playerView.setPlayer(exoPlayer);

        //set media info
        binding.layoutBottomSheet.textViewTitle.setText(media.getMediaName());
        binding.layoutBottomSheet.textViewCreator.setText(media.getMediaDescription());

        //if more media available to play
        if ((mediaToPlay.size()-1)> currentMediaPosition) {
            binding.layoutBottomSheet.imageViewNext.setImageResource(R.drawable.ic_next_light);
        } else {
            binding.layoutBottomSheet.imageViewNext.setImageResource(R.drawable.ic_next_grey);
        }

         //set progress
        binding.layoutBottomSheet.progressBar.setProgress(0);

        //show bottomSheet for player
        binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private int calculatePercentage(long totalDuration, long currentDuration) {
        if (totalDuration == 0){
            return 0;
        }
        return (int)((currentDuration * 100)/totalDuration);
    }

    private Player.EventListener playerListener1 = new Player.EventListener() {
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
            Logger.e(TAG, playbackState + "");
            if (currentPlayer == EXO_PLAYER_1) {
                checkPlayBackState(playWhenReady, playbackState);
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
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            /*Logger.e(TAG, "onPlaybackParametersChanged");*/
        }

        @Override
        public void onSeekProcessed() {
            /*Logger.e(TAG, "onSeekProcessed");*/
        }
    };

    private Player.EventListener playerListener2 = new Player.EventListener() {
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
            if (currentPlayer == EXO_PLAYER_2) {
                checkPlayBackState(playWhenReady, playbackState);
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

    private void checkPlayBackState(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.GONE);
                binding.layoutBottomSheet.progressBuffering.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                break;
            case Player.STATE_READY:
                if (playWhenReady) {
                    // media actually playing
                    binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                } else {
                    // player paused in any state
                    binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                }
                break;
            case Player.STATE_IDLE:
                break;
            default:
                break;
        }
    }

    public void toggleShuffleMode() {
        exoPlayer1.setShuffleModeEnabled(!exoPlayer1.getShuffleModeEnabled());
    }

    public void updateActionBarStatus(boolean visible){
        if (visible){
            binding.toolbar.setVisibility(View.VISIBLE);
        } else {
            binding.toolbar.setVisibility(View.GONE);
        }
    }

    public void setupToolbar(boolean homeAsUpEnabled, String title){
        /*if (homeAsUpEnabled){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
*/
        binding.toolbar.setTitle(title);
    }

    public void showSnack(String message) {
        AlerterUtils.showSnack(binding.coordinatorLayout, message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        proceedWithPermissions(null, true);
    }

    @Override
    public void onBackPressed() {
        if (!navController.popBackStack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
        exoPlayer1.release();
        exoPlayer2.release();
        super.onDestroy();
    }

}
