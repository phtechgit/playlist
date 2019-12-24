package com.pheuture.playlists.auth.request_otp;


import android.content.Context;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.AuthActivity;
import com.pheuture.playlists.databinding.FragmentRequestOtpBinding;
import com.pheuture.playlists.interfaces.ButtonClickInterface;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.Logger;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestOtpFragment extends BaseFragment implements TextWatcher, ButtonClickInterface {
    private static final String TAG = RequestOtpFragment.class.getSimpleName();
    private FragmentActivity activity;
    private FragmentRequestOtpBinding binding;
    private RequestOtpViewModel viewModel;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        ((AuthActivity)activity).setOnButtonClickListener(this);
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_request_otp, container, false);
        viewModel = ViewModelProviders.of(this).get(RequestOtpViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {

    }

    @Override
    public void setListeners() {
        binding.ediTextPhone.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onAttach(@NotNull Context context) {
        mContext = context;
        Logger.e(TAG, "onAttach");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mContext = null;
        Logger.e(TAG, "onDetach");
        super.onDetach();
    }

    @Override
    public void onButtonClick() {
        if (mContext==null){
            return;
        }
        String phone = binding.ediTextPhone.getText().toString();
        viewModel.requestOTP(phone);

        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, phone);

        Navigation.findNavController(binding.getRoot())
                .navigate(R.id.action_navigation_request_otp_to_navigation_verify_otp, bundle);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (binding.ediTextPhone.getText().length()==10) {
            ((AuthActivity)activity).showNextButton(true);
        } else {
            ((AuthActivity)activity).showNextButton(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
