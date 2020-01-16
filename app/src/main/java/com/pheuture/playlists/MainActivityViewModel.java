package com.pheuture.playlists;

import android.app.Application;
import android.app.DownloadManager;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.exoplayer2.ExoPlaybackException;
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
import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.datasource.local.LocalRepository;
import com.pheuture.playlists.datasource.local.media_handler.queue.QueueMediaDao;
import com.pheuture.playlists.datasource.local.media_handler.queue.QueueMediaEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaDao;
import com.pheuture.playlists.datasource.local.media_handler.offline.OfflineMediaDao;
import com.pheuture.playlists.datasource.local.media_handler.offline.OfflineMediaEntity;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.receiver.ConnectivityChangeReceiver;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import static android.content.Context.AUDIO_SERVICE;

public class MainActivityViewModel extends BaseAndroidViewModel implements Constants.SnackBarActions,
        ConnectivityChangeReceiver.ConnectivityChangeListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = MainActivityViewModel.class.getSimpleName();
    private MutableLiveData<String> title;
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer1;
    private SimpleExoPlayer exoPlayer2;
    private MutableLiveData<SimpleExoPlayer> exoPlayerMutableLiveData;
    private MutableLiveData<PlaylistEntity> playlistMutableLiveData;
    private LiveData<List<QueueMediaEntity>> queueMediaEntitiesLiveData;
    private PlaylistMediaDao playlistMediaDao;
    private QueueMediaDao queueMediaDao;
    private OfflineMediaDao offlineMediaDao;
    private MutableLiveData<Boolean> isNewMediaAddedToPlaylist;
    private MutableLiveData<Integer> bottomSheetState = new MutableLiveData<>();
    private MutableLiveData<Bundle> playbackStateMutableLiveData = new MutableLiveData<>();

    private ConnectivityChangeReceiver connectivityChangeReceiver;
    private int EXO_PLAYER_1 = 1;
    private int EXO_PLAYER_2 = 2;
    private int currentPlayer = RecyclerView.NO_POSITION;
    private int currentMediaPosition = RecyclerView.NO_POSITION;
    private Handler timerHandler = new Handler();
    private long totalDurationOfCurrentMedia = 0;
    private long currentDurationOfCurrentMedia = 0;
    private int defaultTimerInMilliSec = 100;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequestBuilder;
    private boolean playbackDelayed = false;
    private boolean playbackNowAuthorized = false;
    private boolean resumeOnFocusGain = false;
    private final Object focusLock = new Object();
    private boolean connectedToNetwork = false;
    private boolean playingFromNetwork;
    private MutableLiveData<Integer> playingMediaProgress;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);

        //set timerHandler that runs at 'defaultTimerInMilliSec' to update progress and check if
        // need to change track for crossFade feature
        timerHandler.postDelayed(timerRunnable, defaultTimerInMilliSec);

        setupConnectivityChangeBroadcastReceiver();

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
        exoPlayerMutableLiveData = new MutableLiveData<>();

        playlistMediaDao = LocalRepository.getInstance(application).playlistMediaDao();
        queueMediaDao = LocalRepository.getInstance(application).queueMediaDao();
        offlineMediaDao = LocalRepository.getInstance(application).offlineMediaDao();

        playlistMutableLiveData = new MutableLiveData<>();
        queueMediaEntitiesLiveData = queueMediaDao.getQueueMediaEntitiesLive();
        playingMediaProgress = new MutableLiveData<>(0);
        isNewMediaAddedToPlaylist = new MutableLiveData<>();

        title = new MutableLiveData<>();
    }



    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentPlayer == EXO_PLAYER_1) {
                totalDurationOfCurrentMedia = exoPlayer1.getDuration();
                currentDurationOfCurrentMedia = exoPlayer1.getCurrentPosition();

                int crossFadeValue = (SharedPrefsUtils.getIntegerPreference(getApplication(),
                        Constants.CROSS_FADE_VALUE, 0) * 1000);

                //increase volume if player
                if (queueMediaEntitiesLiveData.getValue().size()>1 && exoPlayer1.getVolume()<1f) {
                    float volume = (float) currentDurationOfCurrentMedia / (float) crossFadeValue;
                    exoPlayer1.setVolume(volume);
                    exoPlayer2.setVolume(1f-volume);
                    Logger.e(TAG, "volume: exoPlayerMutableLiveData:" + exoPlayer1.getVolume() + ", exoPlayer2:" + exoPlayer2.getVolume());
                }

                //if more media available to play
                if ((queueMediaEntitiesLiveData.getValue().size()-1)> currentMediaPosition) {
                    if (totalDurationOfCurrentMedia > 0 && (totalDurationOfCurrentMedia
                            - currentDurationOfCurrentMedia) <= crossFadeValue) {
                        exoPlayer2.setVolume(0f);
                        loadNextVideoIn(EXO_PLAYER_2);
                    }
                }
            } else if (currentPlayer == EXO_PLAYER_2) {
                totalDurationOfCurrentMedia = exoPlayer2.getDuration();
                currentDurationOfCurrentMedia = exoPlayer2.getCurrentPosition();

                int crossFadeValue = (SharedPrefsUtils.getIntegerPreference(getApplication(),
                        Constants.CROSS_FADE_VALUE, 0) * 1000);

                //increase volume if player
                if (queueMediaEntitiesLiveData.getValue().size()>1 && exoPlayer2.getVolume()<1f) {
                    float volume = (float) currentDurationOfCurrentMedia / (float) crossFadeValue;
                    exoPlayer2.setVolume(volume);
                    exoPlayer1.setVolume(1f-volume);

                    Logger.e(TAG, "volume: exoPlayer2:" + exoPlayer2.getVolume() + ", exoPlayer1:" + exoPlayer1.getVolume());
                }

                //if more media available to play
                if ((queueMediaEntitiesLiveData.getValue().size()-1)> currentMediaPosition){
                    //if remaining duration of current media <= 2sec
                    if (totalDurationOfCurrentMedia > 0 && (totalDurationOfCurrentMedia
                            - currentDurationOfCurrentMedia) <= crossFadeValue) {
                        exoPlayer1.setVolume(0f);
                        loadNextVideoIn(EXO_PLAYER_1);
                    }
                }
            }

            //update progress
            playingMediaProgress.postValue(calculatePercentage(totalDurationOfCurrentMedia,
                    currentDurationOfCurrentMedia));

            //reset handler
            timerHandler.postDelayed(timerRunnable, defaultTimerInMilliSec);
        }
    };

    private void setupConnectivityChangeBroadcastReceiver() {
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        getApplication().registerReceiver(connectivityChangeReceiver, intentFilter);
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

    public MutableLiveData<PlaylistEntity> getPlaylistMutableLiveData() {
        return playlistMutableLiveData;
    }

    public LiveData<List<QueueMediaEntity>> getQueueMediaEntities() {
        return queueMediaEntitiesLiveData;
    }

    public void setNewMediaAdded(boolean b) {
        isNewMediaAddedToPlaylist.postValue(b);
    }

    public MutableLiveData<Boolean> isNewMediaAddedToPlaylist(){
        return isNewMediaAddedToPlaylist;
    }

    public MutableLiveData<String> getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title.postValue(title);
    }

    public void resetAllPlayers(){
        pausePlayback();
        abandonAudioFocus();
    }

    public void togglePlay() {
        SimpleExoPlayer exoPlayer = null;
        if (currentPlayer == EXO_PLAYER_1){
            exoPlayer = exoPlayer1;
        } else {
            exoPlayer = exoPlayer2;
        }

        if (exoPlayer.getPlayWhenReady()){
            pausePlayback();
            abandonAudioFocus();
        } else {
            requestAudioFocus();
        }
    }

    public void next() {
        //if more media available to play & no pending callbacks
        if ((queueMediaEntitiesLiveData.getValue().size() - 1)> currentMediaPosition){
            if (currentPlayer == EXO_PLAYER_1) {
                exoPlayer1.setVolume(1f);
                loadNextVideoIn(EXO_PLAYER_1);
            } else {
                exoPlayer2.setVolume(1f);
                loadNextVideoIn(EXO_PLAYER_2);
            }
        }
    }

    public void dismissPlayer() {
    }

    public void setMedia(PlaylistEntity playlistEntity, QueueMediaEntity queueMediaEntity){
        //stop exoPlayerMutableLiveData before changing the queue
        playlistMutableLiveData.postValue(playlistEntity);
        queueMediaDao.deleteAll();

        if (playlistEntity == null){
            if (queueMediaEntity!=null){
                queueMediaEntity.setState(QueueMediaEntity.QueueMediaState.PLAYING);
                queueMediaDao.insert(queueMediaEntity);
            }
        } else {
            if (queueMediaEntity == null){

                List<PlaylistMediaEntity> playlistMediaEntities = playlistMediaDao.getPlaylistMediaMediaEntities(playlistEntity.getPlaylistID());

                String objectJsonString = ParserUtil.getInstance().toJson(playlistMediaEntities);
                List<QueueMediaEntity> queueMediaEntities = Arrays.asList(ParserUtil.getInstance()
                        .fromJson(objectJsonString, QueueMediaEntity[].class));

                //insert all media with In_Queue status.
                queueMediaDao.insertAll(queueMediaEntities);

                //set state of first media as 'PLAYING'.
                queueMediaEntity = queueMediaEntities.get(0);
                queueMediaEntity.setState(QueueMediaEntity.QueueMediaState.PLAYING);
                queueMediaDao.insert(queueMediaEntity);

            } else {
                //insert playlist media in queue only when new playlist/playlist media is selected
                if (playlistMutableLiveData.getValue()!=null
                        && (playlistMutableLiveData.getValue().getPlaylistID() != playlistEntity.getPlaylistID())) {

                    List<PlaylistMediaEntity> playlistMediaEntities = playlistMediaDao.getPlaylistMediaMediaEntities(playlistEntity.getPlaylistID());

                    String objectJsonString = ParserUtil.getInstance().toJson(playlistMediaEntities);
                    List<QueueMediaEntity> queueMediaEntities = Arrays.asList(ParserUtil.getInstance()
                            .fromJson(objectJsonString, QueueMediaEntity[].class));

                            //insert all media with In_Queue status.
                    queueMediaDao.insertAll(queueMediaEntities);
                }
                queueMediaDao.changeStateOfAllMedia(QueueMediaEntity.QueueMediaState.IN_QUEUE);

                queueMediaEntity.setState(QueueMediaEntity.QueueMediaState.PLAYING);
                queueMediaDao.insert(queueMediaEntity);

                //check if current playlist is playing or not.
                // if playing then just update the current playing media position
            }
        }

        if (currentPlayer == EXO_PLAYER_1 || currentPlayer == RecyclerView.NO_POSITION){
            exoPlayer1.setVolume(1f);
            loadNextVideoIn(EXO_PLAYER_1);

        } else if (currentPlayer == EXO_PLAYER_2) {
            exoPlayer2.setVolume(1f);
            loadNextVideoIn(EXO_PLAYER_2);
        }
    }

    private void loadNextVideoIn(int player) {
        currentPlayer = player;

        SimpleExoPlayer exoPlayer;
        if (currentPlayer == EXO_PLAYER_1){
            exoPlayer = exoPlayer1;
        } else {
            exoPlayer = exoPlayer2;
        }

        QueueMediaEntity media = queueMediaEntitiesLiveData.getValue().get(++currentMediaPosition);
        Uri mediaUri;

        //check if media is available offline then load from it else stream from server
        OfflineMediaEntity offlineMedia = offlineMediaDao.getOfflineMedia(media.getMediaID());
        if (offlineMedia != null && offlineMedia.getDownloadStatus()== DownloadManager.STATUS_SUCCESSFUL) {
            File file = new File(offlineMedia.getDownloadedFilePath());
            mediaUri = Uri.fromFile(file);
            playingFromNetwork = false;
            Logger.e(TAG, "media loading Offline from: " + mediaUri);


        } else {
            mediaUri = Uri.parse(media.getMediaUrl());
            playingFromNetwork = true;
            Logger.e(TAG, "media loading Online from: " + mediaUri);
        }

        //create media source
        MediaSource mediaSource = new ProgressiveMediaSource
                .Factory(dataSourceFactory).createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);
        requestAudioFocus();
        exoPlayerMutableLiveData.postValue(exoPlayer);
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
            if (currentPlayer == EXO_PLAYER_1) {
                setPlaybackState(playWhenReady, playbackState);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            String data = shuffleModeEnabled?"Enabled":"Disabled";
            Toast.makeText(getApplication(), "Shuffle " + data , Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Logger.e(TAG, "onPlayerError: " + error.getMessage());
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
                setPlaybackState(playWhenReady, playbackState);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            String data = shuffleModeEnabled ? "Enabled" : "Disabled";
            Toast.makeText(getApplication(), "Shuffle " + data , Toast.LENGTH_SHORT).show();
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

    private void setPlaybackState(boolean playWhenReady, int playbackState) {
        if (playingFromNetwork && !connectedToNetwork){
            pausePlayback();
            abandonAudioFocus();
            showSnackBar("No internet connectivity...", Snackbar.LENGTH_LONG);
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.ARG_PARAM1, playWhenReady);
        bundle.putInt(Constants.ARG_PARAM1, playbackState);
        playbackStateMutableLiveData.postValue(bundle);
    }

    public void toggleShuffleMode() {
        if (currentPlayer == EXO_PLAYER_1) {
            exoPlayer1.setShuffleModeEnabled(!exoPlayer1.getShuffleModeEnabled());
        } else {
            exoPlayer2.setShuffleModeEnabled(!exoPlayer2.getShuffleModeEnabled());
        }
    }

    public MutableLiveData<SimpleExoPlayer> getExoPlayer() {
        return exoPlayerMutableLiveData;
    }

    public MutableLiveData<Integer> getPlayingMediaProgress() {
        return playingMediaProgress;
    }

    public void removeQueueMedia(QueueMediaEntity queueMediaEntity) {
        queueMediaDao.delete(queueMediaEntity);
    }

    public QueueMediaEntity getPlayingMedia() {
        return queueMediaEntitiesLiveData.getValue().get(currentMediaPosition);
    }

    public boolean shouldShowNextButton() {
        return (queueMediaEntitiesLiveData.getValue().size()-1)> currentMediaPosition;
    }

    public MutableLiveData<Integer> getBottomSheetState() {
        return bottomSheetState;
    }

    public void setBottomSheetState(int newState) {
        bottomSheetState.postValue(newState);
    }

    public MutableLiveData<Bundle> getPlayBackState() {
        return playbackStateMutableLiveData;
    }

    @Override
    public void onConnectivityChange(boolean connected) {
        connectedToNetwork = connected;
    }

    @Override
    protected void onCleared() {
        getApplication().unregisterReceiver(connectivityChangeReceiver);

        exoPlayer1.release();
        exoPlayer2.release();
        abandonAudioFocus();

        timerHandler.removeCallbacks(timerRunnable);

        super.onCleared();
    }
}
