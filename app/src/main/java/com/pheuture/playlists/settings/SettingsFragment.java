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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentSettingsBinding;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.KeyboardUtils;
import com.pheuture.playlists.utils.SharedPrefsUtils;

import static com.pheuture.playlists.utils.RequestCodeConstant.REQUEST_CODE_FILE_SELECT;

public class SettingsFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();
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
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        ((MainActivity) getActivity()).setupToolbar(false, "Settings");

        boolean downloadPlaylistMediaStatus = SharedPrefsUtils.getBooleanPreference(activity,
                Constants.DOWNLOAD_PLAYLIST_MEDIA, false);

        boolean downloadOnCellularStatus = SharedPrefsUtils.getBooleanPreference(activity,
                Constants.DOWNLOAD_USING_CELLULAR, false);
        boolean downloadWhileRoamingStatus = SharedPrefsUtils.getBooleanPreference(activity,
                Constants.DOWNLOAD_WHILE_ROAMING, false);

        int crossFadeValue = SharedPrefsUtils.getIntegerPreference(activity,
                Constants.CROSS_FADE_VALUE, 0);

        binding.switchDownloadPlaylistVideosToOffline.setChecked(downloadPlaylistMediaStatus);
        binding.switchDownloadUsingCellular.setChecked(downloadOnCellularStatus);
        binding.switchDownloadWhileRoaming.setChecked(downloadWhileRoamingStatus);
        binding.seekBarCrossFade.setProgress(crossFadeValue);
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
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("video/*");
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_FILE_SELECT);
            }
        } else if (v.equals(binding.linearLayoutDeleteOfflineVideos)){
            showDeleteConfirmationDialog();
        }
    }

    private void showDeleteConfirmationDialog() {
        Dialog dialog = new Dialog(activity);
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

        textViewTitle.setText("Are you sure?");
        textViewSubtitle.setText("Do you want to remove all the offline songs?");
        textViewSubtitle.setVisibility(View.VISIBLE);
        textViewRight.setText("Remove");

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                KeyboardUtils.hideKeyboard(activity, editText);
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                KeyboardUtils.hideKeyboard(activity, editText);
            }
        });

        textViewLeft.setOnClickListener(view -> {
            dialog.cancel();
        });

        textViewRight.setOnClickListener(view -> {
            dialog.dismiss();
            viewModel.deleteOfflineMedia();
            ((MainActivity) activity).showSnack("Offline songs removed.");
        });
        /*KeyboardUtils.showKeyboard(activity, editTextPlaylistName);*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_FILE_SELECT && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri mediaUri = resultData.getData();
                if (mediaUri != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(ARG_PARAM1, mediaUri);

                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.action_navigation_settings_to_navigation_uploads, bundle);
                }
            }
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
}