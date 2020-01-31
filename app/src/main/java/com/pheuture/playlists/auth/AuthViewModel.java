package com.pheuture.playlists.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pheuture.playlists.base.BaseAndroidViewModel;
import com.pheuture.playlists.base.interfaces.ButtonClickListener;

public class AuthViewModel extends BaseAndroidViewModel {
    private static final String TAG = AuthViewModel.class.getSimpleName();
    private String phoneNumber;
    private MutableLiveData<Boolean> showNextButton;
    private ButtonClickListener buttonClickListener;
    private MutableLiveData<Boolean> moveToOtpVerifyPage;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        showNextButton = new MutableLiveData<>(false);
        moveToOtpVerifyPage = new MutableLiveData<>(false);
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setShowNextButton(Boolean show) {
        showNextButton.postValue(show);
    }

    public LiveData<Boolean> getShowNextButton() {
        return showNextButton;
    }

    public void setOnButtonClickListener(ButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    public void setNextButtonClicked() {
        buttonClickListener.onButtonClick();
    }

    public void setMoveToOtpVerifyPage(Boolean sent) {
        moveToOtpVerifyPage.postValue(sent);
    }

    public LiveData<Boolean> getMoveToOtpVerifyPage() {
        return moveToOtpVerifyPage;
    }

}
