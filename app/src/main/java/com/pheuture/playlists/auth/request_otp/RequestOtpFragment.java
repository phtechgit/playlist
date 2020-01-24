package com.pheuture.playlists.auth.request_otp;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.AuthActivity;
import com.pheuture.playlists.auth.AuthViewModel;
import com.pheuture.playlists.databinding.FragmentRequestOtpBinding;
import com.pheuture.playlists.interfaces.ButtonClickListener;
import com.pheuture.playlists.base.BaseFragment;
import com.pheuture.playlists.utils.KeyboardUtils;
import com.pheuture.playlists.utils.NetworkUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestOtpFragment extends BaseFragment implements TextWatcher, ButtonClickListener{
    private static final String TAG = RequestOtpFragment.class.getSimpleName();
    private FragmentActivity activity;
    private FragmentRequestOtpBinding binding;
    private RequestOtpViewModel viewModel;
    private AuthViewModel parentViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_request_otp, container, false);
        parentViewModel = ViewModelProviders.of(activity).get(AuthViewModel.class);
        viewModel = ViewModelProviders.of(this).get(RequestOtpViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        binding.ediTextPhone.setText(parentViewModel.getPhoneNumber());
        binding.ediTextPhone.setSelection(binding.ediTextPhone.getText().length());

        viewModel.getShowNextButton().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                parentViewModel.setShowNextButton(show);
            }
        });
    }

    @Override
    public void setListeners() {
        binding.ediTextPhone.addTextChangedListener(this);
        parentViewModel.setOnButtonClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.ediTextPhone.requestFocus();
    }

    @Override
    public void onStop() {
        super.onStop();
        KeyboardUtils.hideKeyboard(activity, binding.ediTextPhone);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onButtonClick() {
        KeyboardUtils.hideKeyboard(activity, binding.ediTextPhone);
        parentViewModel.setMoveToOtpVerifyPage();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        parentViewModel.setPhoneNumber(s.toString());
        viewModel.setPhoneNumber(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
