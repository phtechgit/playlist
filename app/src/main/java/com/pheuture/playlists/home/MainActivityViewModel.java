package com.pheuture.playlists.home;

import android.app.Application;
import android.app.DownloadManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

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
import com.pheuture.playlists.base.LocalRepository;
import com.pheuture.playlists.base.constants.DefaultValues;
import com.pheuture.playlists.media.OfflineMediaLocalDao;
import com.pheuture.playlists.queue.QueueMediaDao;
import com.pheuture.playlists.queue.QueueMediaEntity;
import com.pheuture.playlists.playlist.PlaylistEntity;
import com.pheuture.playlists.playist_detail.PlaylistMediaLocalDao;
import com.pheuture.playlists.media.OfflineMediaEntity;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.playist_detail.PlaylistMediaEntity;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.Logger;
import com.pheuture.playlists.base.utils.ParserUtil;
import com.pheuture.playlists.base.utils.SharedPrefsUtils;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static android.content.Context.AUDIO_SERVICE;

public class MainActivityViewModel extends BaseAndroidViewModel implements Constants.SnackBarActions,
        AudioManager.OnAudioFocusChangeListener, DefaultValues {

    private static final String TAG = MainActivityViewModel.class.getSimpleName();
    private MutableLiveData<String> title;
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer exoPlayer1;
    private SimpleExoPlayer exoPlayer2;
    private MutableLiveData<SimpleExoPlayer> exoPlayerMutableLiveData;
    private MutableLiveData<PlaylistEntity> playlistMutableLiveData;
    private int bottomSheetState = BottomSheetBehavior.STATE_HIDDEN;
    private MutableLiveData<Bundle> playbackStateMutableLiveData;
    private MutableLiveData<Integer> playingMediaProgress;
    private MutableLiveData<QueueMediaEntity> currentlyPlayingQueueMediaMutableLiveData;
    private MutableLiveData<Boolean> isNewMediaAddedToPlaylist;
    private MutableLiveData<Boolean> showActionBar;
    private PlaylistMediaLocalDao playlistMediaLocalDao;
    private QueueMediaDao queueMediaLocalDao;
    private OfflineMediaLocalDao offlineMediaLocalDao;
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
    private MutableLiveData<List<QueueMediaEntity>> queueMediaEntitiesMutableLiveData;
    private MutableLiveData<Integer> repeatModeMutableLiveData = new MutableLiveData<>(ExoPlayer.REPEAT_MODE_OFF);

    public LiveData<PlaylistEntity> getPlaylistMutableLiveData() {
        return playlistMutableLiveData;
    }

    public LiveData<List<QueueMediaEntity>> getQueueMediaEntities() {
        return queueMediaEntitiesMutableLiveData;
    }

    public void setNewMediaAdded(boolean b) {
        isNewMediaAddedToPlaylist.postValue(b);
    }

    public LiveData<Boolean> isNewMediaAddedToPlaylist(){
        return isNewMediaAddedToPlaylist;
    }

    public LiveData<String> getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title.postValue(title);
    }

    public int calculatePercentage(long totalDuration, long currentDuration) {
        if (totalDuration == 0){
            return 0;
        }
        return (int)((currentDuration * 100)/totalDuration);
    }

    public LiveData<SimpleExoPlayer> getExoPlayer() {
        return exoPlayerMutableLiveData;
    }

    public LiveData<Integer> getPlayingMediaProgress() {
        return playingMediaProgress;
    }

    public void setBottomSheetState(int newState) {
        bottomSheetState = newState;
    }

    public LiveData<Bundle> getPlayBackState() {
        return playbackStateMutableLiveData;
    }

    public void setNetworkStatus(boolean connected) {
        connectedToNetwork = connected;
    }

    public LiveData<QueueMediaEntity> getCurrentlyPlayingQueueMedia() {
        return currentlyPlayingQueueMediaMutableLiveData;
    }

    public LiveData<Boolean> getShowActionBar() {
        return showActionBar;
    }

    public LiveData<Integer> getRepeatMode() {
        return repeatModeMutableLiveData;
    }

    public MainActivityViewModel(@NonNull Application application) {
        super(application);

        //set timerHandler that runs at 'defaultTimerInMilliSec' to update progress and check if
        // need to change track for crossFade feature
        setLooper();

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

        playlistMediaLocalDao = LocalRepository.getInstance(application).playlistMediaLocalDao();
        queueMediaLocalDao = LocalRepository.getInstance(application).queueMediaLocalDao();
        offlineMediaLocalDao = LocalRepository.getInstance(application).offlineMediaLocalDao();

        playlistMutableLiveData = new MutableLiveData<>();
        currentlyPlayingQueueMediaMutableLiveData = new MutableLiveData<>();
        playbackStateMutableLiveData = new MutableLiveData<>();
        playingMediaProgress = new MutableLiveData<>();
        isNewMediaAddedToPlaylist = new MutableLiveData<>();
        showActionBar = new MutableLiveData<>();

        title = new MutableLiveData<>();

        queueMediaEntitiesMutableLiveData = new MutableLiveData<>();
        queueMediaEntitiesMutableLiveData.postValue(queueMediaLocalDao.getQueueMediaEntities());
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
        Logger.e(TAG, "playback paused");
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
                abandonAudioFocus();
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
                abandonAudioFocus();
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

    public Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            SimpleExoPlayer exoPlayer = getExoPlayer().getValue();
            if (exoPlayer != null && bottomSheetState != BottomSheetBehavior.STATE_HIDDEN
                    && exoPlayer.getPlaybackState() == Player.STATE_READY && exoPlayer.getPlayWhenReady()) {
                proceed();
            }
            //reset handler
            setLooper();
        }
    };

    public void proceed() {
        try {
            int nextPlayer;
            SimpleExoPlayer primaryExoPlayer;
            SimpleExoPlayer secondaryExoPlayer;

            if (currentPlayer == EXO_PLAYER_1){
                primaryExoPlayer = exoPlayer1;
                secondaryExoPlayer = exoPlayer2;
                nextPlayer = EXO_PLAYER_2;
            } else {
                primaryExoPlayer = exoPlayer2;
                secondaryExoPlayer = exoPlayer1;
                nextPlayer = EXO_PLAYER_1;
            }
            totalDurationOfCurrentMedia = primaryExoPlayer.getDuration();
            currentDurationOfCurrentMedia = primaryExoPlayer.getCurrentPosition();

            int crossFadeValue = (SharedPrefsUtils.getIntegerPreference(getApplication(),
                    Constants.CROSS_FADE_VALUE, CROSS_FADE_DURATION_DEFAULT) * 1000);

            //increase volume if player
            if (queueMediaEntitiesMutableLiveData.getValue().size()>1 && primaryExoPlayer.getVolume()<1f) {
                float volume = (float) currentDurationOfCurrentMedia / (float) crossFadeValue;
                primaryExoPlayer.setVolume(volume);
                secondaryExoPlayer.setVolume(1f-volume);
                /*Logger.e(TAG, "volume: exoPlayerMutableLiveData:" + exoPlayer1.getVolume() + ", exoPlayer2:" + exoPlayer2.getVolume());*/
            }

            //check if it is time to change the track
            if (totalDurationOfCurrentMedia > 0 && (totalDurationOfCurrentMedia
                    - currentDurationOfCurrentMedia) <= crossFadeValue) {

                int repeatMode = repeatModeMutableLiveData.getValue();
                if (repeatMode == ExoPlayer.REPEAT_MODE_ONE){
                    //If single repeat play is ON
                    secondaryExoPlayer.setVolume(0f);
                    loadMediaIn(nextPlayer, queueMediaEntitiesMutableLiveData.getValue().get(currentMediaPosition));

                } else if (nextMediaAvailable()) {
                    //if more media available to play
                    secondaryExoPlayer.setVolume(0f);
                    loadMediaIn(nextPlayer, queueMediaEntitiesMutableLiveData.getValue().get(++currentMediaPosition));

                } else if (repeatMode == ExoPlayer.REPEAT_MODE_ALL){
                    currentMediaPosition = RecyclerView.NO_POSITION;
                    secondaryExoPlayer.setVolume(0f);
                    loadMediaIn(nextPlayer, queueMediaEntitiesMutableLiveData.getValue().get(++currentMediaPosition));
                }
            }

            int progress = calculatePercentage(totalDurationOfCurrentMedia,
                    currentDurationOfCurrentMedia);

            if (currentlyPlayingQueueMediaMutableLiveData.getValue() != null) {

                List<QueueMediaEntity> queueMediaEntities = queueMediaEntitiesMutableLiveData.getValue();
                QueueMediaEntity queueMediaEntity = queueMediaEntities.get(currentMediaPosition);
                queueMediaEntity.setProgress((int) currentDurationOfCurrentMedia);
                queueMediaEntities.set(currentMediaPosition, queueMediaEntity);

                queueMediaEntitiesMutableLiveData.setValue(queueMediaEntities);
                currentlyPlayingQueueMediaMutableLiveData.setValue(queueMediaEntity);
                //update progress
                playingMediaProgress.postValue(progress);
            }
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
    }

    public void resetAllPlayers(){
        pausePlayback();
        abandonAudioFocus();
    }

    public void togglePlay() {
        SimpleExoPlayer exoPlayer;
        if (currentPlayer == EXO_PLAYER_1){
            exoPlayer = exoPlayer1;
        } else {
            exoPlayer = exoPlayer2;
        }

        if (exoPlayer.getPlayWhenReady()){
            pausePlayback();
            abandonAudioFocus();
        } else {
            if (exoPlayer.getPlaybackState() == Player.STATE_IDLE) {
                playMedia(exoPlayer, currentlyPlayingQueueMediaMutableLiveData.getValue());

            } else if (exoPlayer.getPlaybackState() == Player.STATE_ENDED){
                rePlayCurrentMedia();
            }
            requestAudioFocus();
        }
    }

    private void rePlayCurrentMedia() {
        QueueMediaEntity queueMediaEntity = currentlyPlayingQueueMediaMutableLiveData.getValue();
        if (queueMediaEntity==null){
            return;
        }
        queueMediaEntity.setProgress(0);
        List<QueueMediaEntity> queueMediaEntities = queueMediaEntitiesMutableLiveData.getValue();
        if (queueMediaEntities==null){
            return;
        }
        queueMediaEntities.set(currentMediaPosition, queueMediaEntity);
        queueMediaLocalDao.insert(queueMediaEntity);
        queueMediaEntitiesMutableLiveData.setValue(queueMediaEntities);
        currentlyPlayingQueueMediaMutableLiveData.setValue(queueMediaEntity);
        playingMediaProgress.postValue(0);
        playMedia(getExoPlayer().getValue(), queueMediaEntity);
    }

    public void setMedia(PlaylistEntity playlistEntity, QueueMediaEntity queueMediaEntity, boolean refreshData){
        //momentarily hold the playback to initiate the changes
        pausePlayback();

        repeatModeMutableLiveData.postValue(ExoPlayer.REPEAT_MODE_OFF);

        currentMediaPosition = RecyclerView.NO_POSITION;

        if (refreshData) {
            queueMediaLocalDao.deleteAll();
        }

        if (playlistEntity == null){
            queueMediaEntity.setPosition(++currentMediaPosition);
            queueMediaLocalDao.insert(queueMediaEntity);

        } else {
            List<QueueMediaEntity> queueMediaEntities = null;
            if (refreshData) {
                List<PlaylistMediaEntity> playlistMediaEntities = playlistMediaLocalDao.getPlaylistMediaMediaEntities(playlistEntity.getPlaylistID());

                String objectJsonString = ParserUtil.getInstance().toJson(playlistMediaEntities);
                queueMediaEntities = Arrays.asList(ParserUtil.getInstance()
                        .fromJson(objectJsonString, QueueMediaEntity[].class));

                for (int i=0; i<queueMediaEntities.size(); i++){
                    QueueMediaEntity queueMediaEntity1 = queueMediaEntities.get(i);
                    queueMediaEntity1.setPosition(i);
                    queueMediaEntities.set(i, queueMediaEntity1);
                }

                //insert all media with 'In_Queue' status.
                queueMediaLocalDao.insertAll(queueMediaEntities);
            }

            if (queueMediaEntity == null){
                if (queueMediaEntities != null) {
                    queueMediaEntity = queueMediaEntities.get(++currentMediaPosition);
                }
            } else {
                if (refreshData) {
                    queueMediaEntity = queueMediaLocalDao.getQueueMediaEntity(queueMediaEntity.getMediaID());
                    currentMediaPosition = queueMediaEntity.getPosition();

                } else {
                    currentMediaPosition = queueMediaEntity.getPosition();
                }
            }
        }
        playlistMutableLiveData.postValue(playlistEntity);

        if (currentPlayer == EXO_PLAYER_1 || currentPlayer == RecyclerView.NO_POSITION){
            exoPlayer1.setVolume(1f);
            loadMediaIn(EXO_PLAYER_1, queueMediaEntity);

        } else if (currentPlayer == EXO_PLAYER_2) {
            exoPlayer2.setVolume(1f);
            loadMediaIn(EXO_PLAYER_2, queueMediaEntity);
        }
        requestAudioFocus();
    }

    public void loadMediaIn(int player, QueueMediaEntity queueMediaEntity) {
        currentPlayer = player;

        SimpleExoPlayer exoPlayer;
        if (currentPlayer == EXO_PLAYER_1){
            exoPlayer = exoPlayer1;
        } else {
            exoPlayer = exoPlayer2;
        }

        queueMediaLocalDao.changeStateOfAllMedia(QueueMediaEntity.QueueMediaState.IN_QUEUE);
        queueMediaLocalDao.setMediaStatusBelowPosition(QueueMediaEntity.QueueMediaState.PLAYED, queueMediaEntity.getPosition());

        queueMediaEntity.setProgress(0);
        queueMediaEntity.setState(QueueMediaEntity.QueueMediaState.PLAYING);
        queueMediaLocalDao.insert(queueMediaEntity);

        queueMediaEntitiesMutableLiveData.setValue(queueMediaLocalDao.getQueueMediaEntities());
        currentlyPlayingQueueMediaMutableLiveData.setValue(queueMediaEntity);
        exoPlayerMutableLiveData.setValue(exoPlayer);
        playingMediaProgress.postValue(0);

        playMedia(exoPlayer, queueMediaEntity);
    }

    public void playMedia(SimpleExoPlayer exoPlayer, QueueMediaEntity queueMediaEntity) {
        Uri mediaUri;

        //check if media is available offline then load from it else stream from server
        OfflineMediaEntity offlineMedia = offlineMediaLocalDao.getOfflineMedia(queueMediaEntity.getMediaID());
        if (offlineMedia != null && offlineMedia.getDownloadStatus()== DownloadManager.STATUS_SUCCESSFUL) {
            File file = new File(offlineMedia.getDownloadedFilePath());
            mediaUri = Uri.fromFile(file);
            playingFromNetwork = false;
            Logger.e(TAG, "media loading Offline from: " + mediaUri);


        } else {
            mediaUri = Uri.parse(queueMediaEntity.getMediaUrl());
            playingFromNetwork = true;
            Logger.e(TAG, "media loading Online from: " + mediaUri);
        }

        //create media source
        MediaSource mediaSource = new ProgressiveMediaSource
                .Factory(dataSourceFactory).createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);
        exoPlayer.seekTo(queueMediaEntity.getProgress());
    }

    public void setPlaybackState(boolean playWhenReady, int playbackState) {
        if (playingFromNetwork && !connectedToNetwork && playbackState == PlaybackState.STATE_STOPPED
                && bottomSheetState!= BottomSheetBehavior.STATE_HIDDEN){
            showSnackBar("No internet connectivity...", Snackbar.LENGTH_LONG);
        }
        if (playbackState == Player.STATE_ENDED){
            pausePlayback();
            abandonAudioFocus();
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.ARG_PARAM1, playWhenReady);
        bundle.putInt(Constants.ARG_PARAM2, playbackState);
        playbackStateMutableLiveData.postValue(bundle);
    }

    public void seekPlayer(int progress) {
        try {
            SimpleExoPlayer exoPlayer = exoPlayerMutableLiveData.getValue();

            long totalDurationOfCurrentMedia = exoPlayer.getDuration();
            long currentDurationOfCurrentMedia = (progress * totalDurationOfCurrentMedia)/100;
            exoPlayer.seekTo(currentDurationOfCurrentMedia);
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
    }

    public void previous() {
        repeatModeMutableLiveData.postValue(ExoPlayer.REPEAT_MODE_OFF);

        //if more media available to play & no pending callbacks
        if (queueMediaEntitiesMutableLiveData.getValue().size()>0 && currentMediaPosition != 0){
            if (currentPlayer == EXO_PLAYER_1) {
                exoPlayer1.setVolume(1f);
                loadMediaIn(EXO_PLAYER_1, queueMediaEntitiesMutableLiveData.getValue().get(--currentMediaPosition));
            } else {
                exoPlayer2.setVolume(1f);
                loadMediaIn(EXO_PLAYER_2, queueMediaEntitiesMutableLiveData.getValue().get(--currentMediaPosition));
            }
        }
    }

    public boolean previousMediaAvailable() {
        return (queueMediaEntitiesMutableLiveData.getValue().size()>0 && currentMediaPosition!=0);
    }

    public boolean nextMediaAvailable() {
        return (queueMediaEntitiesMutableLiveData.getValue().size()-1)> currentMediaPosition;
    }

    public void next() {
        repeatModeMutableLiveData.postValue(ExoPlayer.REPEAT_MODE_OFF);

        //if more media available to play & no pending callbacks
        if ((queueMediaEntitiesMutableLiveData.getValue().size() - 1)> currentMediaPosition){
            if (currentPlayer == EXO_PLAYER_1) {
                exoPlayer1.setVolume(1f);
                loadMediaIn(EXO_PLAYER_1, queueMediaEntitiesMutableLiveData.getValue().get(++currentMediaPosition));
            } else {
                exoPlayer2.setVolume(1f);
                loadMediaIn(EXO_PLAYER_2, queueMediaEntitiesMutableLiveData.getValue().get(++currentMediaPosition));
            }
        }
    }

    public boolean canShuffle() {
        return (queueMediaEntitiesMutableLiveData.getValue().size()-2)> currentMediaPosition;
    }

    public void shuffle() {
        List<QueueMediaEntity> queueMediaEntityList = queueMediaLocalDao.getQueueMediaEntities(QueueMediaEntity.QueueMediaState.IN_QUEUE);
        if (queueMediaEntityList.size()>1) {
            queueMediaLocalDao.delete(queueMediaEntityList);

            int startingPosition = queueMediaEntityList.get(0).getPosition();
            Collections.shuffle(queueMediaEntityList);
            for (int i=0; i<queueMediaEntityList.size(); i++){
                QueueMediaEntity queueMediaEntity = queueMediaEntityList.get(i);
                queueMediaEntity.setPosition(startingPosition++);
                queueMediaEntityList.set(i, queueMediaEntity);
            }
            queueMediaLocalDao.insertAll(queueMediaEntityList);
            queueMediaEntitiesMutableLiveData.postValue(queueMediaLocalDao.getQueueMediaEntities());
            showSnackBar("queue shuffled", Snackbar.LENGTH_SHORT);
        }
    }

    public void setShuffledMedia(PlaylistEntity playlist) {
        //momentarily hold the playback to initiate the changes
        pausePlayback();

        repeatModeMutableLiveData.postValue(ExoPlayer.REPEAT_MODE_OFF);

        currentMediaPosition = RecyclerView.NO_POSITION;

        queueMediaLocalDao.deleteAll();

        List<PlaylistMediaEntity> playlistMediaEntities = playlistMediaLocalDao.getPlaylistMediaMediaEntities(playlist.getPlaylistID());

        String objectJsonString = ParserUtil.getInstance().toJson(playlistMediaEntities);
        List<QueueMediaEntity> queueMediaEntities = Arrays.asList(ParserUtil.getInstance()
                .fromJson(objectJsonString, QueueMediaEntity[].class));

        //shuffle the media
        Collections.shuffle(queueMediaEntities);

        for (int i=0; i<queueMediaEntities.size(); i++){
            QueueMediaEntity queueMediaEntity1 = queueMediaEntities.get(i);
            queueMediaEntity1.setPosition(i);
            queueMediaEntities.set(i, queueMediaEntity1);
        }

        //insert all media with 'In_Queue' status.
        queueMediaLocalDao.insertAll(queueMediaEntities);

        QueueMediaEntity queueMediaEntity = queueMediaEntities.get(++currentMediaPosition);
        playlistMutableLiveData.postValue(playlist);

        if (currentPlayer == EXO_PLAYER_1 || currentPlayer == RecyclerView.NO_POSITION){
            exoPlayer1.setVolume(1f);
            Logger.e(TAG, "ExoPlayer1 volume set to: 1f");
            loadMediaIn(EXO_PLAYER_1, queueMediaEntity);

        } else if (currentPlayer == EXO_PLAYER_2) {
            exoPlayer2.setVolume(1f);
            loadMediaIn(EXO_PLAYER_2, queueMediaEntity);
        }

        requestAudioFocus();
    }

    public void toggleRepeatMode() {
        int repeatMode = repeatModeMutableLiveData.getValue();
        if (repeatMode == ExoPlayer.REPEAT_MODE_OFF) {
            repeatModeMutableLiveData.postValue(ExoPlayer.REPEAT_MODE_ONE);
        } else if (repeatMode == ExoPlayer.REPEAT_MODE_ONE){
            repeatModeMutableLiveData.postValue(ExoPlayer.REPEAT_MODE_ALL);
        } else if (repeatMode == ExoPlayer.REPEAT_MODE_ALL){
            repeatModeMutableLiveData.postValue(ExoPlayer.REPEAT_MODE_OFF);
        }
    }

    public void removeQueueMedia(QueueMediaEntity queueMediaEntity) {
        String title = queueMediaEntity.getMediaTitle();
        int position = queueMediaEntity.getPosition();
        List<QueueMediaEntity> queueMediaEntities = queueMediaEntitiesMutableLiveData.getValue();
        if (queueMediaEntities != null && queueMediaEntities.size()>0) {
            queueMediaEntities.remove(position);
            queueMediaLocalDao.delete(queueMediaEntity);

            for (int i=position; i<queueMediaEntities.size(); i++){
                queueMediaEntity = queueMediaEntities.get(i);
                queueMediaEntity.setPosition(i);
                queueMediaEntities.set(i, queueMediaEntity);
            }
            queueMediaLocalDao.insertAll(queueMediaEntities);
            queueMediaEntitiesMutableLiveData.postValue(queueMediaEntities);
            showSnackBar(title + " removed from queue", Snackbar.LENGTH_SHORT);
        }
    }

    public void moveQueueMedia(int fromPosition, int toPosition) {
        stopLooper();

        try {
            int startPosition;
            int endPosition;
            if (toPosition < fromPosition) {
                startPosition = toPosition;
                endPosition = fromPosition;
            } else {
                startPosition = fromPosition;
                endPosition = toPosition;
            }

            List<QueueMediaEntity> queueMediaEntities = queueMediaEntitiesMutableLiveData.getValue();
            if (queueMediaEntities == null) {
                return;
            }
            QueueMediaEntity queueMediaEntity = queueMediaEntities.get(fromPosition);
            queueMediaEntities.remove(fromPosition);
            queueMediaEntities.add(toPosition, queueMediaEntity);

            if ((fromPosition<currentMediaPosition && toPosition<currentMediaPosition)
                    || (fromPosition>currentMediaPosition && toPosition>currentMediaPosition)){
                for (int i = startPosition; i <= endPosition; i++){
                    queueMediaEntity = queueMediaEntities.get(i);
                    queueMediaEntity.setPosition(i);
                    queueMediaEntities.set(i, queueMediaEntity);
                }
            } else if (fromPosition!=currentMediaPosition){
                if (toPosition>=currentMediaPosition){
                    queueMediaEntity.setState(QueueMediaEntity.QueueMediaState.IN_QUEUE);
                } else {
                    queueMediaEntity.setState(QueueMediaEntity.QueueMediaState.PLAYED);
                }
                for (int i = startPosition; i <= endPosition; i++){
                    queueMediaEntity = queueMediaEntities.get(i);
                    queueMediaEntity.setPosition(i);
                    queueMediaEntities.set(i, queueMediaEntity);
                    if (queueMediaEntity.getState() == QueueMediaEntity.QueueMediaState.PLAYING){
                        currentMediaPosition = i;
                    }
                }
            } else {
                int state;
                if (toPosition>currentMediaPosition){
                    state = QueueMediaEntity.QueueMediaState.PLAYED;
                } else {
                    state = QueueMediaEntity.QueueMediaState.IN_QUEUE;
                }
                for (int i = startPosition; i <= endPosition; i++){
                    queueMediaEntity = queueMediaEntities.get(i);
                    queueMediaEntity.setPosition(i);
                    if (queueMediaEntity.getState()!= QueueMediaEntity.QueueMediaState.PLAYING) {
                        queueMediaEntity.setState(state);
                    } else {
                        currentMediaPosition = i;
                    }
                    queueMediaEntities.set(i, queueMediaEntity);
                }
            }

            queueMediaLocalDao.insertAll(queueMediaEntities);
            queueMediaEntitiesMutableLiveData.setValue(queueMediaEntities);
            currentlyPlayingQueueMediaMutableLiveData.setValue(queueMediaEntities.get(currentMediaPosition));
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }

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

        exoPlayer1.release();
        exoPlayer2.release();

        abandonAudioFocus();

        super.onCleared();
    }
}
