package com.pheuture.playlists;

import android.app.DownloadManager;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.databinding.ActivityMainBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.media_handler.MediaEntity;
import com.pheuture.playlists.datasource.local.media_handler.offline.OfflineMediaEntity;
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
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;

import static androidx.navigation.Navigation.findNavController;

public class MainActivity extends BaseActivity implements AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private SimpleExoPlayer exoPlayer1;
    private SimpleExoPlayer exoPlayer2;
    private int EXO_PLAYER_1 = 1;
    private int EXO_PLAYER_2 = 2;
    private PlayerView playerView;
    private static int currentPlayer = RecyclerView.NO_POSITION;
    private static int currentMediaPosition = RecyclerView.NO_POSITION;
    private List<PlaylistMediaEntity> mediaToPlay;
    private BottomSheetBehavior bottomSheetBehavior;
    private Handler timerHandler = new Handler();
    private long totalDurationOfCurrentMedia = 0;
    private long currentDurationOfCurrentMedia = 0;
    private int defaultTimerSec = 1000;
    private AudioManager audioManager;
    private AudioAttributes playbackAttributes;
    private AudioFocusRequest audioFocusRequestBuilder;
    private boolean playbackDelayed = false;
    private boolean playbackNowAuthorized = false;
    private boolean resumeOnFocusGain = false;
    private final Object focusLock = new Object();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestAudioFocus(){
        int res;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            res = audioManager.requestAudioFocus(audioFocusRequestBuilder);
        } else {
            res = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }

        synchronized(focusLock) {
            if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                playbackNowAuthorized = false;
            } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                playbackNowAuthorized = true;
                playbackNow();
            } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                playbackDelayed = true;
                playbackNowAuthorized = false;
            }
        }
    }

    private void abandonAudioFocus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequestBuilder);
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    private void pausePlayback() {
        exoPlayer1.setPlayWhenReady(false);
        exoPlayer2.setPlayWhenReady(false);
    }

    private void playbackNow() {
        exoPlayer1.setPlayWhenReady(true);
        exoPlayer2.setPlayWhenReady(true);
    }

    @Override
    public void initializations() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_playlists, R.id.navigation_trending, R.id.navigation_settings)
                .build();

        NavController navController = findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build();

            audioFocusRequestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, new Handler())
                    .build();
        }

        proceedWithPermissions(null, true);

        playerView = binding.layoutBottomSheet.playerView;

        bottomSheetBehavior = BottomSheetBehavior.from( binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer);
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        exoPlayer1 = viewModel.getExoPlayer1();
        exoPlayer2 = viewModel.getExoPlayer2();

        viewModel.getPlaylistMediaEntities().observe(this, new Observer<List<PlaylistMediaEntity>>() {
            @Override
            public void onChanged(List<PlaylistMediaEntity> videoEntities) {
                mediaToPlay = videoEntities;

                if (mediaToPlay.size()>0){
                    if (currentPlayer == EXO_PLAYER_1 || currentPlayer == RecyclerView.NO_POSITION){
                        exoPlayer1.setVolume(1f);
                        loadNextVideoIn(EXO_PLAYER_1);

                    } else if (currentPlayer == EXO_PLAYER_2) {
                        exoPlayer2.setVolume(1f);
                        loadNextVideoIn(EXO_PLAYER_2);
                    }
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
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false;
                        resumeOnFocusGain = false;
                    }
                    playbackNow();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                synchronized(focusLock) {
                    resumeOnFocusGain = false;
                    playbackDelayed = false;
                }
                pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                synchronized(focusLock) {
                    resumeOnFocusGain = true;
                    playbackDelayed = false;
                }
                pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // ... pausing or ducking depends on your app
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.layoutBottomSheet.imageViewTogglePlay)){
            SimpleExoPlayer exoPlayer = null;
            if (currentPlayer == EXO_PLAYER_1){
                exoPlayer = exoPlayer1;
            } else {
                exoPlayer = exoPlayer2;
            }

            if (exoPlayer.getPlayWhenReady()){
                abandonAudioFocus();
                exoPlayer1.setPlayWhenReady(false);
                exoPlayer2.setPlayWhenReady(false);
            } else {
                requestAudioFocus();
            }
        } else if (v.equals(binding.layoutBottomSheet.imageViewNext)){
            //if more media available to play & no pending callbacks
            if ((mediaToPlay.size() - 1)> currentMediaPosition){
                if (currentPlayer == EXO_PLAYER_1) {
                    exoPlayer1.setVolume(1f);
                    loadNextVideoIn(EXO_PLAYER_1);
                } else {
                    exoPlayer2.setVolume(1f);
                    loadNextVideoIn(EXO_PLAYER_2);
                }
            }

        } else if (v.equals(binding.layoutBottomSheet.imageViewClose)){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            binding.bottomNavView.setVisibility(View.VISIBLE);

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
                        float volume = (float) currentDurationOfCurrentMedia / (float) crossFadeValue;
                        exoPlayer1.setVolume(volume);
                        exoPlayer2.setVolume(1f-volume);
                        Logger.e(TAG, "volume: exoPlayer:" + exoPlayer1.getVolume() + ", exoPlayer2:" + exoPlayer2.getVolume());
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
                        float volume = (float) currentDurationOfCurrentMedia / (float) crossFadeValue;
                        exoPlayer2.setVolume(volume);
                        exoPlayer1.setVolume(1f-volume);

                        Logger.e(TAG, "volume: exoPlayer2:" + exoPlayer2.getVolume() + ", exoPlayer1:" + exoPlayer1.getVolume());
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

                /*Logger.e(TAG, "totalDurationOfCurrentMedia:" + totalDurationOfCurrentMedia + ", currentDurationOfCurrentMedia:" + currentDurationOfCurrentMedia + ", crossFadeValue:" + (SharedPrefsUtils.getIntegerPreference(MainActivity.this,
                        Constants.CROSS_FADE_VALUE, 0) * 1000));*/
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
            abandonAudioFocus();
            exoPlayer1.setPlayWhenReady(false);
            exoPlayer2.setPlayWhenReady(false);
        } catch (Exception ignore) {
        }
    }

    public void setMedia(PlaylistEntity playlistEntity,  List<PlaylistMediaEntity> playlistMediaEntities, int position){
        viewModel.setPlaylist(playlistEntity);
        viewModel.setPlaylistMediaEntities(playlistMediaEntities);
        currentMediaPosition = position;
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

        //check if media is available offline then load from it else stream from server
        OfflineMediaEntity offlineMedia = viewModel.getOfflineMediaForMediaID(media.getMediaID());
        if (offlineMedia != null && offlineMedia.getDownloadStatus()== DownloadManager.STATUS_SUCCESSFUL) {
            File file = new File(offlineMedia.getDownloadedFilePath());
            mediaUri = Uri.fromFile(file);
            Logger.e(TAG, "media loading Offline from: " + mediaUri);

        } else {
            mediaUri = Uri.parse(media.getMediaUrl());
            Logger.e(TAG, "media loading Online from: " + mediaUri);
        }

        //create media source
        MediaSource mediaSource = new ProgressiveMediaSource
                .Factory(viewModel.getDataSourceFactory()).createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);
        requestAudioFocus();
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

    public void setupToolbar(boolean homeAsUpEnabled, String title){
        getSupportActionBar().setTitle(title);
    }

    public void showSnack(String message) {
        Snackbar mySnack = Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_SHORT);
        View snackBarView = mySnack.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.WhiteC));

        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.grayF));

        mySnack.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        proceedWithPermissions(null, true);
    }

    @Override
    protected void onDestroy() {
        abandonAudioFocus();
        timerHandler.removeCallbacks(timerRunnable);
        exoPlayer1.release();
        exoPlayer2.release();
        super.onDestroy();
    }

}
