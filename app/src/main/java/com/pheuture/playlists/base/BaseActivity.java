package com.pheuture.playlists.base;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pheuture.playlists.R;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.interfaces.NotificationID;
import com.pheuture.playlists.interfaces.RequestCodes;

import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements NotificationID, RequestCodes,
        Constants.SnackBarActions, View.OnClickListener {
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";
    public static final String ARG_PARAM4 = "param4";
    public static final String ARG_PARAM5 = "param5";
    private Snackbar snackBar;
    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public abstract void initializations();
    public abstract void setListeners();

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

    protected void hideSnack() {
        if (snackBar != null && snackBar.isShown()){
            snackBar.dismiss();
        }
    }

    protected void showSnack(View view, Bundle bundle) {
        if (view == null || bundle == null){
            return;
        }

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
                            Dialog dialog = new Dialog(BaseActivity.this);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            dialog.setContentView(R.layout.layout_create_playlist);
                            dialog.show();

                            TextView textViewTitle = dialog.findViewById(R.id.textView_title);
                            TextView textViewSubtitle = dialog.findViewById(R.id.textView_subtitle);
                            EditText editText = dialog.findViewById(R.id.ediText);
                            TextView textViewLeft = dialog.findViewById(R.id.textView_left);
                            TextView textViewRight = dialog.findViewById(R.id.textView_right);

                            textViewTitle.setText("Need Permissions");
                            textViewSubtitle.setText("This app needs permission to use this feature. You can grant them in Setting.");
                            textViewSubtitle.setVisibility(View.VISIBLE);
                            textViewRight.setText("Goto Settings");

                            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Toast.makeText(BaseActivity.this, "Required permissions are rejected. You cannot proceed.", Toast.LENGTH_SHORT).show();
                                    if (finishActivityOnReject) {
                                        onBackPressed();
                                    }
                                }
                            });
                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {

                                }
                            });

                            textViewLeft.setOnClickListener(view -> {
                                dialog.cancel();
                            });

                            textViewRight.setOnClickListener(view -> {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivityForResult(intent, 0);
                            });
                            /*AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this, android.R.style.ThemeOverlay_Material_Dialog_Alert);
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
                            builder.show();*/
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
