package com.pheuture.playlists.auth.verify_otp;


import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.AuthViewModel;
import com.pheuture.playlists.auth.SplashActivity;
import com.pheuture.playlists.auth.user_detail.UserProfileActivity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.auth.request_otp.RequestOtpFragment;
import com.pheuture.playlists.databinding.FragmentVerifyOtpBinding;
import com.pheuture.playlists.interfaces.ButtonClickListener;
import com.pheuture.playlists.receiver.SMSReceiver;
import com.pheuture.playlists.base.BaseFragment;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.KeyboardUtils;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.StringUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyOtpFragment extends BaseFragment implements TextWatcher,
        SMSReceiver.OTPReceiveListener, ButtonClickListener {
    private static final String TAG = RequestOtpFragment.class.getSimpleName();
    private FragmentActivity activity;
    private FragmentVerifyOtpBinding binding;
    private VerifyOtpViewModel viewModel;
    private AuthViewModel parentViewModel;
    private SMSReceiver smsReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_verify_otp,
                container, false);
        parentViewModel = ViewModelProviders.of(activity).get(AuthViewModel.class);
        viewModel = ViewModelProviders.of(this,
                new VerifyOtpViewModelFactory(activity.getApplication(),
                        parentViewModel.getPhoneNumber())).get(VerifyOtpViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        startSMSListener();

        viewModel.getMessageToShow().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                binding.textViewMessage.setText(message);
            }
        });

        viewModel.getShowNextButton().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                parentViewModel.setShowNextButton(show);
            }
        });

        viewModel.getProgressStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                if(show){
                    KeyboardUtils.hideKeyboard(activity, binding.editTextOtp);
                    binding.editTextOtp.setEnabled(false);
                    binding.progressLayout.relativeLayoutProgress.setVisibility(View.VISIBLE);
                } else {
                    binding.progressLayout.relativeLayoutProgress.setVisibility(View.GONE);
                    binding.editTextOtp.setEnabled(true);
                }
            }
        });

        viewModel.getUserVerifiedStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean verified) {
                UserEntity user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                        activity, Constants.USER, ""), UserEntity.class);

                if (StringUtils.isEmpty(user.getUserFirstName())){
                    Intent intent = new Intent(activity, UserProfileActivity.class);
                    startActivity(intent);
                    activity.finish();

                } else {
                    activity.finish();
                    Intent intent = new Intent(activity, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void setListeners() {
        binding.editTextOtp.addTextChangedListener(this);
        parentViewModel.setOnButtonClickListener(this);
        binding.textViewResendOtp.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.editTextOtp.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        KeyboardUtils.hideKeyboard(activity, binding.editTextOtp);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.textViewResendOtp)){
            binding.textViewResendOtp.setVisibility(View.GONE);
            viewModel.requestOtp();
        }
    }

    /**
     * Starts SmsRetriever, which waits for ONE matching SMS message until timeout
     * (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
     * action SmsRetriever#SMS_RETRIEVED_ACTION.
     */
    private void startSMSListener() {
        try {
            smsReceiver = new SMSReceiver();
            smsReceiver.setOTPListener(this);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
            activity.registerReceiver(smsReceiver, intentFilter);

            SmsRetrieverClient client = SmsRetriever.getClient(activity);

            Task<Void> task = client.startSmsRetriever();
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Fail to start API
                    viewModel.setMessageToShow("Automatically detection of SMS Failed");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOTPReceived(String otp) {
        unregisterSmsReceiver();

        String[] data = otp.split(" ");
        otp = data[data.length-2];

        viewModel.setMessageToShow("OTP received successfully");
        binding.editTextOtp.setText(otp);
        binding.editTextOtp.setSelection(binding.editTextOtp.getText().length());

        viewModel.verifyOtp();
        Logger.e(TAG, "onOTPReceived");
    }

    private void unregisterSmsReceiver() {
        if (smsReceiver != null) {
            activity.unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }
    }

    @Override
    public void onOTPTimeOut() {
        viewModel.setMessageToShow("Automatically detection of SMS Timed out");
        binding.textViewResendOtp.setVisibility(View.VISIBLE);
        Logger.e(TAG, "onOTPTimeOut");
    }

    @Override
    public void onOTPReceivedError(String error) {
        viewModel.setMessageToShow("Automatically detection of SMS Failed");
        Logger.e(TAG, "onOTPReceivedError");
    }

    @Override
    public void onDestroy() {
        if (smsReceiver != null) {
            activity.unregisterReceiver(smsReceiver);
        }
        viewModel.cancelAllApiRequests();
        super.onDestroy();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        viewModel.setOtp(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onButtonClick() {
        viewModel.verifyOtp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterSmsReceiver();
    }
}
