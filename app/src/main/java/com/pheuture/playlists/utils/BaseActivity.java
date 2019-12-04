package com.pheuture.playlists.utils;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";
    public static final String ARG_PARAM4 = "param4";
    public static final String ARG_PARAM5 = "param5";

    private String[] permissions = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializations();
        setListeners();
    }

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

    public void hideProgress(View progressBar){
        if (progressBar!=null) {
            progressBar.setVisibility(View.GONE);
            setUserInteraction(true);
        }
    }

    public abstract void initializations();

    public abstract void setListeners();

    public boolean hasPermissions () {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void proceedWithPermissions(@Nullable final Runnable runnable, boolean finishActivityOnReject) {
        Dexter.withActivity(this)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()){
                            if (runnable!=null) {
                                runnable.run();
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
                            builder.setTitle("Need Permissions");
                            builder.setMessage("This app needs permission to use this feature. You can grant them in Setting.");
                            builder.setCancelable(false);
                            builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                                    startActivityForResult(intent, 0);
                                }
                            });
                            builder.setNegativeButton("No", (dialog, which) -> {
                                dialog.cancel();
                                Toast.makeText(BaseActivity.this, "Required permissions are rejected. You cannot proceed.", Toast.LENGTH_SHORT).show();
                                if (finishActivityOnReject) {
                                    onBackPressed();
                                }
                            });
                            builder.show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }})
                .onSameThread()
                .check();
    }
}
