package com.pheuture.playlists.base.datasource.remote.progress_dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.databinding.DataBindingUtil;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.LayoutAlertIndeterminateBinding;

import java.util.Objects;

public class ProgressDialog implements ProgressDialogActionInterface {
    private Dialog alertDialog;
    private LayoutAlertIndeterminateBinding binding;
    private ClickListener clickListener;

    public ProgressDialog(Context context, ClickListener listener) {
        try {
            this.clickListener = listener;
            binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                    R.layout.layout_alert_indeterminate, null, false);
            alertDialog = new Dialog(context);
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.setCancelable(false);
            Objects.requireNonNull(alertDialog.getWindow()).getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
            alertDialog.setContentView(binding.getRoot());

            binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener!=null) {
                        clickListener.onCancelled();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show(String title) {
        alertDialog.show();
        setMessage(title);
        setProgress(0);
    }

    @Override
    public void dismiss() {
        if (alertDialog!=null){
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @Override
    public boolean isShowing() {
        if (alertDialog!=null) {
            return alertDialog.isShowing();
        }
        return false;
    }

    @Override
    public void setProgress(int progress) {
        if (alertDialog!=null) {
            binding.progressHorizontal.setProgress(progress);
            binding.textViewPercentage.setText(progress + "%");
        }
    }

    @Override
    public void setMessage(String message) {
        if (alertDialog!=null) {
            binding.textViewMessage.setText(message);
        }
    }
}
