package com.pheuture.playlists.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentSettingsBinding;
import com.pheuture.playlists.utils.BaseFragment;

import static com.pheuture.playlists.utils.RequestCodeConstant.REQUEST_CODE_FILE_SELECT;

public class SettingsFragment extends BaseFragment {
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

    }

    @Override
    public void setListeners() {
        binding.linearLayoutUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.linearLayoutUpload)){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("video/*");
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_FILE_SELECT);
            }
        }
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
}