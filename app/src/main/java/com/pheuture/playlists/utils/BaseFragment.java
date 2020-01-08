package com.pheuture.playlists.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public abstract class BaseFragment extends Fragment implements View.OnClickListener {
    public final String ARG_PARAM1 = "param1";
    public final String ARG_PARAM2 = "param2";
    public final String ARG_PARAM3 = "param3";
    public final String ARG_PARAM4 = "param4";
    public final String ARG_PARAM5 = "param5";

    private Context mContext;
    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanseState) {
        View view = myFragmentView(inflater, parent, savedInstanseState);
        setListeners();
        initializations();
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    public abstract View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public abstract void initializations();

    public abstract void setListeners();

    public boolean isFragmentAlive() {
        return mContext != null && isAdded();
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

    public void proceedWithPermissions(FragmentActivity activity, @Nullable final Runnable runnable, boolean finishActivityOnReject) {
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("Need Permissions");
                            builder.setMessage("This app needs permission to use this feature. You can grant them in Setting.");
                            builder.setCancelable(false);
                            builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                                    startActivityForResult(intent, 0);
                                }
                            });
                            builder.setNegativeButton("No", (dialog, which) -> {
                                dialog.cancel();
                                Toast.makeText(activity, "Required permissions are rejected. You cannot proceed.", Toast.LENGTH_SHORT).show();
                                if (finishActivityOnReject) {
                                    activity.onBackPressed();
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

    /*public synchronized Dialog showProgress(Context context) {
        if (context != null) {
            MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder().Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.progress_dialog, null, false);
            alertDialog.setView(view);
            Dialog dialog = alertDialog.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(false);
            dialog.show();
            return dialog;
        }
        return null;

    }

    public synchronized void hideProgress(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }*/

    public void setUserInteraction(boolean isActive) {
        if (mContext != null) {
            if (isActive) {
                ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                ((Activity) mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }

    }

}