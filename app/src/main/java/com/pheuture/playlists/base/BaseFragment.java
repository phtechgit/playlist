package com.pheuture.playlists.base;

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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pheuture.playlists.R;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.constants.DefaultValues;
import com.pheuture.playlists.base.constants.NotificationID;
import com.pheuture.playlists.base.constants.RequestCodes;
import com.pheuture.playlists.base.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.base.utils.PermissionUtils;

import org.jetbrains.annotations.NotNull;
import java.util.List;

public abstract class BaseFragment extends Fragment implements DefaultValues, NotificationID, RequestCodes,
        Constants.SnackBarActions, PermissionUtils.CommonPermissions, RecyclerViewClickListener.ClickType, View.OnClickListener {
    public final String ARG_PARAM1 = "param1";
    public final String ARG_PARAM2 = "param2";
    public final String ARG_PARAM3 = "param3";
    public final String ARG_PARAM4 = "param4";
    public final String ARG_PARAM5 = "param5";
    private Snackbar snackBar;

    private Context mContext;
    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

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

    private void hideSnack() {
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
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = myFragmentView(inflater, parent, savedInstanceState);
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
                            builder.setTitle(getResources().getString(R.string.goto_settings));
                            builder.setMessage(getResources().getString(R.string.this_app_need_permissions_to_use_this_feature));
                            builder.setCancelable(false);
                            builder.setPositiveButton(getResources().getString(R.string.this_app_need_permissions_to_use_this_feature), new DialogInterface.OnClickListener() {
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
                                Toast.makeText(activity, getResources().getString(R.string.required_permissions_are_rejected), Toast.LENGTH_SHORT).show();
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

    private void setUserInteraction(boolean isActive) {
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