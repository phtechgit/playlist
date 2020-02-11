package com.pheuture.playlists.home;

import android.app.Application;
import android.app.DownloadManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.R;
import com.pheuture.playlists.base.datasource.local.LocalRepository;
import com.pheuture.playlists.base.constants.DefaultValues;
import com.pheuture.playlists.media.OfflineMediaLocalDao;
import com.pheuture.playlists.queue.QueueMediaDao;
import com.pheuture.playlists.queue.QueueMediaEntity;
import com.pheuture.playlists.playist_detail.PlaylistMediaLocalDao;
import com.pheuture.playlists.media.OfflineMediaEntity;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.Logger;
import com.pheuture.playlists.base.utils.SharedPrefsUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static android.content.Context.AUDIO_SERVICE;

public class MainActivityViewModel extends BaseAndroidViewModel implements Constants.SnackBarActions,
        AudioManager.OnAudioFocusChangeListener, DefaultValues {

    private static final String TAG = MainActivityViewModel.class.getSimpleName();
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequestBuilder;
    private boolean playbackDelayed = false;
    private boolean playbackNowAuthorized = false;
    private boolean resumeOnFocusGain = false;
    private final Object focusLock = new Object();
    private PlaylistMediaLocalDao playlistMediaLocalDao;
    private QueueMediaDao queueMediaLocalDao;
    private OfflineMediaLocalDao offlineMediaLocalDao;
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer1;
    private SimpleExoPlayer exoPlayer2;
    private int EXO_PLAYER_1 = 1;
    private int EXO_PLAYER_2 = 2;
    private int currentPlayer;
    private int currentMediaPosition;
    private Handler timerHandler = new Handler();
    private List<QueueMediaEntity> currentQueueMediaList;
    private MainViewStates viewStates;
    private MutableLiveData<MainViewStates> viewStatesLive;
    private boolean connectedToNetwork;
    private boolean playingFromNetwork;
    private int repeatMode = ExoPlayer.REPEAT_MODE_OFF;
    private boolean mediaPlaying = false;
    private long totalDurationOfCurrentMedia = 0;
    private long currentDurationOfCurrentMedia = 0;
    private int defaultTimerInMilliSec = 100;
    private int bottomSheetState = BottomSheetBehavior.STATE_HIDDEN;
    private MutableLiveData<Boolean> isNewMediaAddedToPlaylist;
    private MutableLiveData<List<QueueMediaEntity>> currentQueueMediaListLive;

    public void setNewMediaAdded(boolean b) {
        isNewMediaAddedToPlaylist.postValue(b);
    }

    public LiveData<Boolean> isNewMediaAddedToPlaylist(){
        return isNewMediaAddedToPlaylist;
    }

    public void setTitle(String title) {
        viewStates.setTitle(title);
        viewStatesLive.postValue(viewStates);
    }

    public int calculatePercentage(long totalDuration, long currentDuration) {
        if (totalDuration == 0){
            return 0;
        }
        return (int)((currentDuration * 100)/totalDuration);
    }

    public void setBottomSheetState(int newState) {
        bottomSheetState = newState;
        switch (newState) {
            case BottomSheetBehavior.STATE_HIDDEN:
                resetAllPlayers();
                break;
            case BottomSheetBehavior.STATE_EXPANDED:
                break;
            case BottomSheetBehavior.STATE_HALF_EXPANDED:
                break;
            case BottomSheetBehavior.STATE_COLLAPSED:
                break;
            case BottomSheetBehavior.STATE_DRAGGING:
                break;
            case BottomSheetBehavior.STATE_SETTLING:
                break;
        }
    }

    public void setNetworkStatus(boolean connected) {
        connectedToNetwork = connected;
    }

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        viewStates = new MainViewStates();
        viewStatesLive = new MutableLiveData<>();
        currentQueueMediaList = new ArrayList<>();
        currentQueueMediaListLive = new MutableLiveData<>();
        currentPlayer = RecyclerView.NO_POSITION;
        currentMediaPosition = RecyclerView.NO_POSITION;
        isNewMediaAddedToPlaylist = new MutableLiveData<>();

        audioManager = (AudioManager) getApplication().getSystemService(AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build();

            audioFocusRequestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, new Handler())
                    .build();
        }

        dataSourceFactory = new DefaultDataSourceFactory(application,
                Util.getUserAgent(application, TAG));
        exoPlayer1 = ExoPlayerFactory.newSimpleInstance(application);
        exoPlayer2 = ExoPlayerFactory.newSimpleInstance(application);

        exoPlayer1.addListener(playerListener1);
        exoPlayer2.addListener(playerListener2);

        playlistMediaLocalDao = LocalRepository.getInstance(application).playlistMediaLocalDao();
        queueMediaLocalDao = LocalRepository.getInstance(application).queueMediaLocalDao();
        offlineMediaLocalDao = LocalRepository.getInstance(application).offlineMediaLocalDao();

        //set timerHandler that runs at 'defaultTimerInMilliSec' to update progress and check if
        // need to change track for crossFade feature
        setLooper();
    }

    public void requestAudioFocus(){
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

    public void abandonAudioFocus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequestBuilder);
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    public void pausePlayback() {
        exoPlayer1.setPlayWhenReady(false);
        exoPlayer2.setPlayWhenReady(false);
        abandonAudioFocus();
    }

    public void playbackNow() {
        exoPlayer1.setPlayWhenReady(true);
        exoPlayer2.setPlayWhenReady(true);
    }

    public Player.EventListener playerListener1 = new Player.EventListener() {
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
            if (currentPlayer == EXO_PLAYER_1) {
                setPlaybackState(playWhenReady, playbackState);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            String data = shuffleModeEnabled ? "Enabled" : "Disabled";
            /*Toast.makeText(getApplication(), "shuffle " + data, Toast.LENGTH_SHORT).show();*/
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Logger.e(TAG, "onPlayerError: " + error.getMessage());
            if (currentPlayer == EXO_PLAYER_1) {
                pausePlayback();
            }
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

    public Player.EventListener playerListener2 = new Player.EventListener() {
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
                setPlaybackState(playWhenReady, playbackState);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            String data = shuffleModeEnabled ? "Enabled" : "Disabled";
            /*Toast.makeText(getApplication(), "shuffle " + data, Toast.LENGTH_SHORT).show();*/
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Logger.e(TAG, "onPlayerError: " + error.getMessage());
            if (currentPlayer == EXO_PLAYER_2) {
                pausePlayback();
            }
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

    public Runnable timerRunnable = () -> {
        SimpleExoPlayer exoPlayer = viewStates.getExoPlayer();
        if (mediaPlaying) {
            loop();
        }
        //reset handler
        setLooper();
    };

    private void loop() {
        try {
            SimpleExoPlayer currentExoPlayer;
            SimpleExoPlayer nextExoPlayer;

            if (currentPlayer == EXO_PLAYER_1){
                currentExoPlayer = exoPlayer1;
                nextExoPlayer = exoPlayer2;

            } else {
                currentExoPlayer = exoPlayer2;
                nextExoPlayer = exoPlayer1;
            }

            totalDurationOfCurrentMedia = currentExoPlayer.getDuration();
            currentDurationOfCurrentMedia = currentExoPlayer.getCurrentPosition();

            int crossFadeValue = (SharedPrefsUtils.getIntegerPreference(getApplication(),
                    Constants.CROSS_FADE_VALUE, CROSS_FADE_DURATION_DEFAULT) * 1000);

            //increase volume if current player.
            if (currentExoPlayer.getVolume()<1f) {
                float volume = (float) currentDurationOfCurrentMedia / (float) crossFadeValue;
                currentExoPlayer.setVolume(volume);
                nextExoPlayer.setVolume(1f-volume);
            }

            //check if it is time to change the track
            if (totalDurationOfCurrentMedia > 0 && (totalDurationOfCurrentMedia
                    - currentDurationOfCurrentMedia) <= crossFadeValue) {
                if (repeatMode == ExoPlayer.REPEAT_MODE_ONE){
                    //If single repeat play is ON
                    nextExoPlayer.setVolume(0f);
                    loadMedia(nextExoPlayer);

                } else if (nextMediaAvailable()) {
                    //if more media available to play
                    ++currentMediaPosition;
                    nextExoPlayer.setVolume(0f);
                    loadMedia(nextExoPlayer);

                } else if (repeatMode == ExoPlayer.REPEAT_MODE_ALL){
                    currentMediaPosition = 0;
                    nextExoPlayer.setVolume(0f);
                    loadMedia(nextExoPlayer);
                }
            }

            int progress = calculatePercentage(totalDurationOfCurrentMedia,
                    currentDurationOfCurrentMedia);

            QueueMediaEntity queueMediaEntity = currentQueueMediaList.get(currentMediaPosition);
            queueMediaEntity.setProgress((int) currentDurationOfCurrentMedia);
            currentQueueMediaList.set(currentMediaPosition, queueMediaEntity);

            //update progress
            viewStates.setMaxProgress(totalDurationOfCurrentMedia);
            viewStates.setProgress(currentDurationOfCurrentMedia);
            viewStatesLive.postValue(viewStates);
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
    }

    public void resetAllPlayers(){
        pausePlayback();
    }

    public void togglePlay() {
        SimpleExoPlayer exoPlayer = getCurrentExoPlayer();

        if (exoPlayer.getPlayWhenReady()){
            pausePlayback();

        } else {
            if (exoPlayer.getPlaybackState() == Player.STATE_IDLE) {
                loadMedia(getCurrentExoPlayer());

            } else if (exoPlayer.getPlaybackState() == Player.STATE_ENDED){
                prepareMediaToPlay(getCurrentExoPlayer());
            }
            requestAudioFocus();
        }
    }

    public void setMediaListToQueue(List<QueueMediaEntity> newQueueMediaEntities, int position){
        //momentarily hold the playback to initiate the changes
        pausePlayback();

        setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);

        currentQueueMediaList = newQueueMediaEntities;
        currentMediaPosition = position;

        SimpleExoPlayer exoPlayer  = getCurrentExoPlayer();
        exoPlayer.setVolume(1f);
        loadMedia(exoPlayer);
        requestAudioFocus();
    }

    public void setShuffledMediaListToQueue(List<QueueMediaEntity> newQueueMediaEntities) {
        //momentarily hold the playback to initiate the changes
        pausePlayback();

        setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);

        Collections.shuffle(newQueueMediaEntities);
        currentQueueMediaList = newQueueMediaEntities;
        currentMediaPosition = 0;

        SimpleExoPlayer exoPlayer  = getCurrentExoPlayer();
        exoPlayer.setVolume(1f);

        loadMedia(exoPlayer);
    }

    public void loadMedia(SimpleExoPlayer exoPlayer) {
        QueueMediaEntity queueMediaEntity = currentQueueMediaList.get(currentMediaPosition);
        queueMediaEntity.setProgress(0);
        currentQueueMediaList.set(currentMediaPosition, queueMediaEntity);

        prepareMediaToPlay(exoPlayer);

        requestAudioFocus();
    }

    public void prepareMediaToPlay(SimpleExoPlayer exoPlayer){
        Uri mediaUri;
        //check if media is available offline then load from it else stream from server
        OfflineMediaEntity offlineMedia = offlineMediaLocalDao.getOfflineMedia(currentQueueMediaList.get(currentMediaPosition).getMediaID());
        if (offlineMedia != null && offlineMedia.getDownloadStatus()== DownloadManager.STATUS_SUCCESSFUL) {
            File file = new File(offlineMedia.getDownloadedFilePath());
            mediaUri = Uri.fromFile(file);
            playingFromNetwork = false;
            Logger.e(TAG, "media loading Offline from: " + mediaUri);

        } else {
            mediaUri = Uri.parse(currentQueueMediaList.get(currentMediaPosition).getMediaUrl());
            playingFromNetwork = true;
            Logger.e(TAG, "media loading Online from: " + mediaUri);
        }

        //create media source
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);
        exoPlayer.seekTo(currentQueueMediaList.get(currentMediaPosition).getProgress());

        viewStates.setExoPlayer(exoPlayer);
        viewStatesLive.postValue(viewStates);
    }

    private SimpleExoPlayer getCurrentExoPlayer() {
        if (currentPlayer == EXO_PLAYER_1 || currentPlayer == RecyclerView.NO_POSITION){
            return exoPlayer1;
        } else {
            return exoPlayer2;
        }
    }

    private SimpleExoPlayer getNextExoPlayer() {
        if (currentPlayer == EXO_PLAYER_1 || currentPlayer == RecyclerView.NO_POSITION){
            return exoPlayer2;
        } else {
            return exoPlayer1;
        }
    }

    private void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
        if (repeatMode == ExoPlayer.REPEAT_MODE_OFF){
            viewStates.setRepeatButtonImageResource(R.drawable.exo_controls_repeat_off);
            viewStates.setRepeatEnabled(false);
        } else if (repeatMode == ExoPlayer.REPEAT_MODE_ONE){
            viewStates.setRepeatButtonImageResource(R.drawable.exo_controls_repeat_one);
            viewStates.setRepeatEnabled(true);
        } else if (repeatMode == ExoPlayer.REPEAT_MODE_ALL){
            viewStates.setRepeatButtonImageResource(R.drawable.exo_controls_repeat_all);
            viewStates.setRepeatEnabled(true);
        }
        viewStatesLive.postValue(viewStates);
    }

    public void setPlaybackState(boolean playWhenReady, int playbackState) {
        if (playingFromNetwork && !connectedToNetwork && playbackState == PlaybackState.STATE_STOPPED
                && bottomSheetState!= BottomSheetBehavior.STATE_HIDDEN){
            viewStates.setError("No internet connectivity.");
        }

        switch (playbackState) {
            case Player.STATE_BUFFERING:
                viewStates.setBufferVisibility(View.VISIBLE);
                break;
            case Player.STATE_READY:
                if (playWhenReady) {
                    // media actually playing
                    viewStates.setBufferVisibility(View.GONE);
                    viewStates.setTogglePlayButtonImageResource(R.drawable.exo_icon_pause);
                } else {
                    // player paused in any state
                    viewStates.setBufferVisibility(View.GONE);
                    viewStates.setTogglePlayButtonImageResource(R.drawable.exo_icon_play);
                }
                break;
            case Player.STATE_ENDED:
                pausePlayback();

                viewStates.setBufferVisibility(View.GONE);
                viewStates.setTogglePlayButtonImageResource(R.drawable.exo_icon_play);
                break;
            case Player.STATE_IDLE:
                viewStates.setBufferVisibility(View.GONE);
                viewStates.setTogglePlayButtonImageResource(R.drawable.exo_icon_play);
                break;
            default:
                break;
        }
        viewStatesLive.postValue(viewStates);
    }

    public void seekPlayer(int progress) {
        SimpleExoPlayer exoPlayer = viewStates.getExoPlayer();
        long totalDurationOfCurrentMedia = exoPlayer.getDuration();
        long currentDurationOfCurrentMedia = (progress * totalDurationOfCurrentMedia)/100;
        exoPlayer.seekTo(currentDurationOfCurrentMedia);
    }

    public boolean previousMediaAvailable() {
        return (currentQueueMediaList.size()>0 && currentMediaPosition>0);
    }

    public void previous() {
        setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);

        //if more media available to play & no pending callbacks
        if (previousMediaAvailable()){
            if (currentPlayer == EXO_PLAYER_1) {
                exoPlayer1.setVolume(1f);
                loadMedia(exoPlayer1);
            } else {
                exoPlayer2.setVolume(1f);
                loadMedia(exoPlayer2);
            }
        }
    }

    public boolean nextMediaAvailable() {
        return (currentQueueMediaList.size()-1)> currentMediaPosition;
    }

    public void next() {
        setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);

        //if more media available to play & no pending callbacks
        if (nextMediaAvailable()){
            if (currentPlayer == EXO_PLAYER_1) {
                exoPlayer1.setVolume(1f);
                loadMedia(exoPlayer1);
            } else {
                exoPlayer2.setVolume(1f);
                loadMedia(exoPlayer2);
            }
        }
    }

    public boolean canShuffle() {
        return (currentQueueMediaList.size()-2)> currentMediaPosition;
    }

    public void shuffle() {
        stopLooper();
        if (canShuffle()){
            List<QueueMediaEntity> shuffledQueueList = currentQueueMediaList.subList(currentMediaPosition + 1, currentQueueMediaList.size());
            currentQueueMediaList.removeAll(shuffledQueueList);
            Collections.shuffle(shuffledQueueList);
            currentQueueMediaList.addAll(shuffledQueueList);
            showSnackBar("queue shuffled", Snackbar.LENGTH_SHORT);
        }
        setLooper();
    }

    public void toggleRepeatMode() {
        if (repeatMode == ExoPlayer.REPEAT_MODE_OFF) {
            setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
        } else if (repeatMode == ExoPlayer.REPEAT_MODE_ONE){
            setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
        } else if (repeatMode == ExoPlayer.REPEAT_MODE_ALL){
            setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);
        }
    }

    public void removeQueueMedia(int position) {
        stopLooper();

        QueueMediaEntity queueMediaEntity = currentQueueMediaList.get(position);
        currentQueueMediaList.remove(position);
        currentMediaPosition = currentQueueMediaList.indexOf(queueMediaEntity);

        showSnackBar(queueMediaEntity.getMediaTitle() + " removed from queue", Snackbar.LENGTH_SHORT);

        setLooper();
    }

    public void moveQueueMedia(int fromPosition, int toPosition) {
        stopLooper();

        QueueMediaEntity queueMediaEntity = currentQueueMediaList.get(fromPosition);
        currentQueueMediaList.remove(fromPosition);
        currentQueueMediaList.add(toPosition, queueMediaEntity);

        currentMediaPosition = currentQueueMediaList.indexOf(queueMediaEntity);

        setLooper();
    }

    private void setLooper(){
        timerHandler.postDelayed(timerRunnable, defaultTimerInMilliSec);
    }

    private void stopLooper() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onCleared() {
        stopLooper();

        pausePlayback();

        exoPlayer1.release();
        exoPlayer2.release();

        super.onCleared();
    }

    public LiveData<MainViewStates> getViewStatesLive() {
        return viewStatesLive;
    }

    public void closeBottomSheet() {
        viewStates.setBottomNavigationViewVisibility(View.VISIBLE);
        viewStates.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
        viewStates.setBottomSheetVisibility(View.GONE);
    }

    public LiveData<List<QueueMediaEntity>> getQueueMediaEntities() {
        return currentQueueMediaListLive;
    }
}
