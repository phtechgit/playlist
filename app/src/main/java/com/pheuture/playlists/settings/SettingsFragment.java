package com.pheuture.playlists.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.home.MainActivityViewModel;
import com.pheuture.playlists.R;
import com.pheuture.playlists.base.constants.DefaultValues;
import com.pheuture.playlists.databinding.FragmentSettingsBinding;
import com.pheuture.playlists.upload.UploadActivity;
import com.pheuture.playlists.base.BaseFragment;
import com.pheuture.playlists.base.constants.Constants;
import com.pheuture.playlists.base.utils.KeyboardUtils;
import com.pheuture.playlists.base.utils.SharedPrefsUtils;

public class SettingsFragment extends BaseFragment implements
        CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, DefaultValues {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private MainActivityViewModel parentViewModel;
    private SettingsViewModel viewModel;
    private FragmentSettingsBinding binding;
    private FragmentActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        parentViewModel = new ViewModelProvider(activity).get(MainActivityViewModel.class);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        parentViewModel.setTitle(activity.getResources().getString(R.string.setting_title));

        boolean downloadPlaylistMediaStatus = SharedPrefsUtils.getBooleanPreference(activity,
                Constants.DOWNLOAD_PLAYLIST_MEDIA, DOWNLOAD_PLAYLIST_MEDIA_DEFAULT);

        boolean downloadOnCellularStatus = SharedPrefsUtils.getBooleanPreference(activity,
                Constants.DOWNLOAD_USING_CELLULAR, DOWNLOAD_USING_CELLULAR_DEFAULT);
        boolean downloadWhileRoamingStatus = SharedPrefsUtils.getBooleanPreference(activity,
                Constants.DOWNLOAD_WHILE_ROAMING, DOWNLOAD_WHILE_ROAMING_DEFAULT);

        int crossFadeValue = SharedPrefsUtils.getIntegerPreference(activity,
                Constants.CROSS_FADE_VALUE, CROSS_FADE_DURATION_DEFAULT);

        binding.switchDownloadPlaylistVideosToOffline.setChecked(downloadPlaylistMediaStatus);
        binding.switchDownloadUsingCellular.setChecked(downloadOnCellularStatus);
        binding.switchDownloadWhileRoaming.setChecked(downloadWhileRoamingStatus);
        binding.seekBarCrossFade.setProgress(crossFadeValue);
        binding.textViewSeekProgress.setText(crossFadeValue + " s");
    }

    @Override
    public void setListeners() {
        binding.linearLayoutUpload.setOnClickListener(this);
        binding.switchDownloadPlaylistVideosToOffline.setOnCheckedChangeListener(this);
        binding.switchDownloadUsingCellular.setOnCheckedChangeListener(this);
        binding.switchDownloadWhileRoaming.setOnCheckedChangeListener(this);
        binding.linearLayoutDeleteOfflineVideos.setOnClickListener(this);
        binding.seekBarCrossFade.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.linearLayoutUpload)){
            parentViewModel.pausePlayback();
            openFileSelector();

        } else if (v.equals(binding.linearLayoutDeleteOfflineVideos)){
            showDeleteConfirmationDialog();
        }
    }

    private void showDeleteConfirmationDialog() {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.layout_alert);
        dialog.show();

        TextView textViewTitle = dialog.findViewById(R.id.textView_title);
        TextView textViewSubtitle = dialog.findViewById(R.id.textView_subtitle);
        TextView textViewLeft = dialog.findViewById(R.id.textView_left);
        TextView textViewRight = dialog.findViewById(R.id.textView_right);

        textViewTitle.setText(activity.getResources().getString(R.string.are_you_sure));
        textViewSubtitle.setText(activity.getResources().getString(R.string.do_you_want_remove_all_the_offline_songs));
        textViewSubtitle.setVisibility(View.VISIBLE);
        textViewRight.setText(activity.getResources().getString(R.string.delete));

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                KeyboardUtils.hideKeyboard(activity, binding.getRoot());
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                KeyboardUtils.hideKeyboard(activity, binding.getRoot());
            }
        });

        textViewLeft.setOnClickListener(view -> {
            dialog.cancel();
        });

        textViewRight.setOnClickListener(view -> {
            dialog.dismiss();
            viewModel.deleteOfflineMedia();
            parentViewModel.showSnackBar(activity.getResources().getString(R.string.offline_songs_deleted), Snackbar.LENGTH_SHORT);
        });
    }

    private void openFileSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_VIDEO_FILE_SELECT);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.equals(binding.switchDownloadPlaylistVideosToOffline)){
            SharedPrefsUtils.setBooleanPreference(activity, Constants.DOWNLOAD_PLAYLIST_MEDIA, isChecked);

        } else if (buttonView.equals(binding.switchDownloadUsingCellular)){
            SharedPrefsUtils.setBooleanPreference(activity, Constants.DOWNLOAD_USING_CELLULAR, isChecked);

        } else if (buttonView.equals(binding.switchDownloadWhileRoaming)){
            SharedPrefsUtils.setBooleanPreference(activity, Constants.DOWNLOAD_WHILE_ROAMING, isChecked);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        binding.textViewSeekProgress.setText(progress + " s");
        SharedPrefsUtils.setIntegerPreference(activity, Constants.CROSS_FADE_VALUE, progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == REQUEST_CODE_VIDEO_FILE_SELECT && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri mediaUri = resultData.getData();
                if (mediaUri != null) {
                    Intent intent = new Intent(activity, UploadActivity.class);
                    intent.putExtra(ARG_PARAM1, mediaUri);
                    startActivity(intent);
                }
            }
        }
    }
}