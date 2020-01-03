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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.AuthActivity;
import com.pheuture.playlists.auth.user_detail.UserProfileActivity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.auth.request_otp.RequestOtpFragment;
import com.pheuture.playlists.databinding.FragmentVerifyOtpBinding;
import com.pheuture.playlists.receiver.SMSReceiver;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.StringUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyOtpFragment extends BaseFragment implements SMSReceiver.OTPReceiveListener{
    private static final String TAG = RequestOtpFragment.class.getSimpleName();
    private FragmentActivity activity;
    private FragmentVerifyOtpBinding binding;
    private VerifyOtpViewModel viewModel;
    private String phone;
    private SMSReceiver smsReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        ((AuthActivity)activity).setOnButtonClickListener(this);
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() == null) {
            return null;
        }
        phone = getArguments().getString(ARG_PARAM1);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_verify_otp,
                container, false);
        viewModel = ViewModelProviders.of(this,
                new VerifyOtpViewModelFactory(activity.getApplication(),
                        phone)).get(VerifyOtpViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        ((AuthActivity)activity).showNextButton(false);

        binding.textViewMessage.setText("Waiting to automatically detect an SMS sent to " + phone);

        viewModel.getUserLive().observe(this, new Observer<UserEntity>() {
            @Override
            public void onChanged(UserEntity user) {
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

        viewModel.getProgressStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                if(show){
                    showProgress(binding.progressLayout.relativeLayoutProgress, true);
                } else {
                    hideProgress(binding.progressLayout.relativeLayoutProgress);
                }
            }
        });

        startSMSListener();
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onClick(View v) {

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
                    showToast("SMS sent to " + phone);
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Fail to start API
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onOTPReceived(String otp) {
        if (smsReceiver != null) {
            activity.unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }

        String[] data = otp.split(" ");
        otp = data[data.length-2];

        binding.textViewOtp.setText(otp);

        viewModel.verifyOtp(otp);
    }

    @Override
    public void onOTPTimeOut() {
        showToast("OTP Time out");
    }

    @Override
    public void onOTPReceivedError(String error) {
        showToast(error);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (smsReceiver != null) {
            activity.unregisterReceiver(smsReceiver);
        }
    }


    private void showToast(String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}
