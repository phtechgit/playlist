package com.pheuture.playlists.home;

import android.view.View;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.pheuture.playlists.R;

public class MainViewStates {
    private String title;

    private int bottomNavigationViewVisibility = View.VISIBLE;
    private int bottomSheetVisibility = View.GONE;
    private int bottomSheetState = BottomSheetBehavior.STATE_HIDDEN;
    private SimpleExoPlayer exoPlayer;
    private int progress;
    private String currentlyPlayingMediaTitle;
    private String currentlyPLayingMediaCreator;

    private int bufferVisibility = View.GONE;
    private int togglePlayButtonImageResource = R.drawable.exo_icon_play;
    private boolean playEnabled = false;
    private int nextButtonImageResource = R.drawable.ic_next_grey;
    private boolean nextEnabled = false;
    private int previousButtonImageResource = R.drawable.ic_previous_dark;
    private boolean previousEnabled = false;
    private int shuffleButtonImageResource = R.drawable.exo_controls_shuffle_off;
    private boolean shuffleEnabled = false;
    private int repeatButtonImageResource = R.drawable.exo_controls_repeat_off;
    private boolean repeatEnabled = false;
    private String error;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public void setExoPlayer(SimpleExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    public String getCurrentlyPlayingMediaTitle() {
        return currentlyPlayingMediaTitle;
    }

    public void setCurrentlyPlayingMediaTitle(String currentlyPlayingMediaTitle) {
        this.currentlyPlayingMediaTitle = currentlyPlayingMediaTitle;
    }

    public String getCurrentlyPLayingMediaCreator() {
        return currentlyPLayingMediaCreator;
    }

    public void setCurrentlyPLayingMediaCreator(String currentlyPLayingMediaCreator) {
        this.currentlyPLayingMediaCreator = currentlyPLayingMediaCreator;
    }


    public int getBottomSheetVisibility() {
        return bottomSheetVisibility;
    }

    public void setBottomSheetVisibility(int bottomSheetVisibility) {
        this.bottomSheetVisibility = bottomSheetVisibility;
    }

    public int getBottomSheetState() {
        return bottomSheetState;
    }

    public void setBottomSheetState(int bottomSheetState) {
        this.bottomSheetState = bottomSheetState;
    }

    public int getNextButtonImageResource() {
        return nextButtonImageResource;
    }

    public void setNextButtonImageResource(int nextButtonImageResource) {
        this.nextButtonImageResource = nextButtonImageResource;
    }

    public boolean isNextEnabled() {
        return nextEnabled;
    }

    public void setNextEnabled(boolean nextEnabled) {
        this.nextEnabled = nextEnabled;
    }

    public int getShuffleButtonImageResource() {
        return shuffleButtonImageResource;
    }

    public void setShuffleButtonImageResource(int shuffleButtonImageResource) {
        this.shuffleButtonImageResource = shuffleButtonImageResource;
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    public void setShuffleEnabled(boolean shuffleEnabled) {
        this.shuffleEnabled = shuffleEnabled;
    }

    public int getPreviousButtonImageResource() {
        return previousButtonImageResource;
    }

    public void setPreviousButtonImageResource(int previousButtonImageResource) {
        this.previousButtonImageResource = previousButtonImageResource;
    }

    public boolean isPreviousEnabled() {
        return previousEnabled;
    }

    public void setPreviousEnabled(boolean previousEnabled) {
        this.previousEnabled = previousEnabled;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getRepeatButtonImageResource() {
        return repeatButtonImageResource;
    }

    public void setRepeatButtonImageResource(int repeatButtonImageResource) {
        this.repeatButtonImageResource = repeatButtonImageResource;
    }

    public boolean isRepeatEnabled() {
        return repeatEnabled;
    }

    public void setRepeatEnabled(boolean repeatEnabled) {
        this.repeatEnabled = repeatEnabled;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getTogglePlayButtonImageResource() {
        return togglePlayButtonImageResource;
    }

    public void setTogglePlayButtonImageResource(int togglePlayButtonImageResource) {
        this.togglePlayButtonImageResource = togglePlayButtonImageResource;
    }

    public boolean isPlayEnabled() {
        return playEnabled;
    }

    public void setPlayEnabled(boolean playEnabled) {
        this.playEnabled = playEnabled;
    }

    public int getBufferVisibility() {
        return bufferVisibility;
    }

    public void setBufferVisibility(int bufferVisibility) {
        this.bufferVisibility = bufferVisibility;
    }

    public int getBottomNavigationViewVisibility() {
        return bottomNavigationViewVisibility;
    }

    public void setBottomNavigationViewVisibility(int bottomNavigationViewVisibility) {
        this.bottomNavigationViewVisibility = bottomNavigationViewVisibility;
    }
}
