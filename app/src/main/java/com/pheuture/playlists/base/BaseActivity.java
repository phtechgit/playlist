package com.pheuture.playlists.base;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.R;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.constants.DefaultValues;
import com.pheuture.playlists.base.constants.NotificationID;
import com.pheuture.playlists.base.constants.RequestCodes;
import com.pheuture.playlists.base.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.base.utils.PermissionUtils;

public abstract class BaseActivity extends AppCompatActivity implements DefaultValues,
        NotificationID, RequestCodes, Constants.SnackBarActions, PermissionUtils.CommonPermissions,
        RecyclerViewClickListener.ClickType, View.OnClickListener {
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";
    public static final String ARG_PARAM4 = "param4";
    public static final String ARG_PARAM5 = "param5";
    private Snackbar snackBar;

    public void setUserInteraction(boolean isActive) {
        if (isActive) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void showProgress(View progressBar) {
        if (progressBar!=null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void showProgress(View progressBar, boolean disableUserInteraction) {
        if (disableUserInteraction) {
            setUserInteraction(false);
        }
        if (progressBar!=null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void setSnackBar(View view, Bundle bundle) {
        if (view == null || bundle == null){
            return;
        }
        if (bundle.getBoolean(SNACK_BAR_SHOW, false)){
            showSnack(view, bundle);
        } else {
            hideSnack();
        }
    }

    protected void hideSnack() {
        if (snackBar != null && snackBar.isShown()){
            snackBar.dismiss();
        }
    }

    protected void showSnack(View view, Bundle bundle) {
        String message = bundle.getString(SNACK_BAR_MESSAGE, "");
        int length = bundle.getInt(SNACK_BAR_LENGTH, Snackbar.LENGTH_SHORT);

        snackBar = Snackbar.make(view, message, length);
        View snackBarView = snackBar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.WhiteC));

        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.grayF));

        snackBar.show();
    }

    public void hideProgress(View progressBar){
        if (progressBar!=null) {
            progressBar.setVisibility(View.GONE);
            setUserInteraction(true);
        }
    }

    public boolean hasPermissions (String[] permissions) {
        return PermissionUtils.hasPermissions(this, permissions);
    }

    public void proceedWithPermissions(int requestCode, String[] permissions, @Nullable final Runnable runnable, boolean finishOnReject) {
        PermissionUtils.askPermissions(this, requestCode, permissions, runnable, finishOnReject);
    }

}
