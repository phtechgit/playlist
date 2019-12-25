package com.pheuture.playlists.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.R;
import androidx.fragment.app.FragmentActivity;
import java.util.Objects;

public class AlerterUtils {
    private static ProgressBar progressBar ;
    private static TextView textViewPercentage;
    private static TextView textViewMessage;

    public static void showSnack(View view, String message){
        //activity.findViewById(android.R.id.content)
        Snackbar mySnack = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        mySnack.show();
    }

    public static Dialog progressDeterminateShow(Context context, String msg) {
        Dialog alertDialog = null;
        try {
            alertDialog = new Dialog(context);
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.setCancelable(false);
            Objects.requireNonNull(alertDialog.getWindow()).getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
            alertDialog.show();
            alertDialog.setContentView(R.layout.layout_alert_indeterminate);

            progressBar = alertDialog.findViewById(R.id.progress_horizontal);
            textViewPercentage = alertDialog.findViewById(R.id.textView_percentage);
            textViewMessage = alertDialog.findViewById(R.id.textView_message);

            if (textViewMessage != null) {
                textViewMessage.setText(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return alertDialog;
    }

    public static void progressDeterminateUpdateMessage(String msg) {
        try {
            if (textViewMessage != null) {
                textViewMessage.setText(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void progressDeterminateUpdateProgress(long percentage) {
        try {
            if (progressBar != null) {
                progressBar.setProgress((int) percentage);
            }
            if (textViewPercentage != null) {
                textViewPercentage.setText(percentage + "%");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void progressDeterminateDismiss(Dialog alertDialog) {
        try {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}