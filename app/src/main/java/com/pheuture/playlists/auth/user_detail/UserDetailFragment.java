package com.pheuture.playlists.auth.user_detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.request_otp.RequestOtpViewModel;
import com.pheuture.playlists.databinding.FragmentUserDetailBinding;
import com.pheuture.playlists.interfaces.ButtonClickInterface;
import com.pheuture.playlists.utils.BaseFragment;

public class UserDetailFragment extends BaseFragment implements ButtonClickInterface {
    private static final String TAG = UserDetailFragment.class.getSimpleName();
    private FragmentActivity activity;
    private FragmentUserDetailBinding binding;
    private UserDetailViewModel viewModel;
    private UserModel user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() == null) {
            return null;
        }
        user = getArguments().getParcelable(ARG_PARAM1);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_detail, container, false);
        viewModel = ViewModelProviders.of(this).get(UserDetailViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {

    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onClick(View v) {
        viewModel.updateUserDetail(binding.ediTextFirstName.getText().toString(), binding.ediTextLastName.getText().toString());

        Intent intent = new Intent(activity, MainActivity.class);
        startActivity(intent);
        activity.finish();
    }

    @Override
    public void onButtonClick() {

    }
}
