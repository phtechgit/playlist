package com.pheuture.playlists.base;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.interfaces.NotificationID;

public class BaseAndroidViewModel extends AndroidViewModel implements NotificationID,
        Constants.SnackBarConstants {

    private MutableLiveData<Bundle> snackBarBundleMutableLiveData;
    private Handler snackBarHandler = new Handler();
    private Runnable snackBarDismissRunnable = new Runnable() {
        @Override
        public void run() {
            Bundle bundle = new Bundle();
            bundle.putBoolean(SNACK_BAR_SHOW, false);
            snackBarBundleMutableLiveData.postValue(bundle);
        }
    };

    public BaseAndroidViewModel(@NonNull Application application) {
        super(application);
        snackBarBundleMutableLiveData = new MutableLiveData<>();
    }

    public void showSnackBar(String message, int length){
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.SnackBarConstants.SNACK_BAR_SHOW, true);
        bundle.putString(Constants.SnackBarConstants.SNACK_BAR_MESSAGE, message);
        bundle.putInt(Constants.SnackBarConstants.SNACK_BAR_LENGTH, length);

        snackBarBundleMutableLiveData.postValue(bundle);

        //set timeOut
        snackBarHandler.removeCallbacks(snackBarDismissRunnable);

        int snackBarDuration = 1000;
        if (bundle.getInt(SNACK_BAR_LENGTH) == Snackbar.LENGTH_SHORT){
            snackBarHandler.postDelayed(snackBarDismissRunnable, snackBarDuration);

        } else if (bundle.getInt(SNACK_BAR_LENGTH) == Snackbar.LENGTH_LONG){
            snackBarDuration = 3 * snackBarDuration;
            snackBarHandler.postDelayed(snackBarDismissRunnable, snackBarDuration);
        }
    }

    public MutableLiveData<Bundle> getSnackBar() {
        return snackBarBundleMutableLiveData;
    }
}
