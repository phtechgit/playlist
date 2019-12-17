package com.pheuture.playlists.upload;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentUploadBinding;
import com.pheuture.playlists.utils.BaseFragment;

public class UploadFragment extends BaseFragment {
    private FragmentActivity activity;
    private UploadViewModel mViewModel;
    private FragmentUploadBinding binding;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(UploadViewModel.class);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_upload, container,false);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
    }

    @Override
    public void setListeners() {
        binding.imageViewUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.imageViewUpload)){

        }
    }
}
