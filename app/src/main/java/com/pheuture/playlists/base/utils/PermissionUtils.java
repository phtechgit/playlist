package com.pheuture.playlists.base.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.content.ContextCompat;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pheuture.playlists.R;
import com.pheuture.playlists.base.constants.RequestCodes;

import java.util.List;

import androidx.annotation.Nullable;

public final class PermissionUtils implements RequestCodes {

    public static Boolean hasPermissions(Context context, String[] permissions) {
        boolean hasPermissions = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                hasPermissions = false;
            }
        }
        return hasPermissions;
    }

    public static void askPermissions(final Activity activity, final int requestCode, String[] permissions,
                                      @Nullable final Runnable runnable,
                                      final boolean finishOnReject) {
        Dexter.withActivity(activity)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()){
                            if (runnable!=null) {
                                runnable.run();
                            }
                        } else {
                            Dialog alertDialog = new Dialog(activity);
                            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            alertDialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
                            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            alertDialog.setContentView(R.layout.layout_alert);
                            alertDialog.show();

                            TextView textViewTitle = alertDialog.findViewById(R.id.textView_title);
                            TextView textViewSubtitle = alertDialog.findViewById(R.id.textView_subtitle);
                            TextView textViewLeft = alertDialog.findViewById(R.id.textView_left);
                            TextView textViewRight = alertDialog.findViewById(R.id.textView_right);

                            textViewTitle.setText(activity.getResources().getString(R.string.goto_settings));
                            textViewSubtitle.setText(activity.getResources().getString(R.string.this_app_need_permissions_to_use_this_feature));

                            textViewRight.setText(activity.getResources().getString(R.string.ok));

                            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if (finishOnReject){
                                        activity.onBackPressed();
                                    }
                                }
                            });

                            textViewLeft.setOnClickListener(view -> {
                                alertDialog.cancel();
                            });

                            textViewRight.setOnClickListener(view -> {
                                alertDialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                                activity.startActivityForResult(intent, REQUEST_CODE_GRANT_PERMISSIONS);
                            });
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }}).check();
    }

    public interface CommonPermissions {
        String[] READ_WRITE_EXTERNAL_STORAGE_PERMISSION = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }
}
