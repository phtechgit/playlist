package com.pheuture.playlists.auth;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.view.View;

import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ActivityAuthBinding;
import com.pheuture.playlists.interfaces.ButtonClickListener;
import com.pheuture.playlists.base.BaseActivity;
import com.pheuture.playlists.utils.Logger;

public class AuthActivity extends BaseActivity {
    private static final String TAG = AuthActivity.class.getSimpleName();
    private ActivityAuthBinding binding;
    private NavController navController;
    private AuthViewModel viewModel;


    @Override
    public void initializations() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth);
        viewModel = ViewModelProviders.of(this).get(AuthViewModel.class);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_request_otp, R.id.navigation_verify_otp)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_auth);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        viewModel.getShowNextButton().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                if (show) {
                    binding.fab.show();
                } else {
                    binding.fab.hide();
                }
            }
        });

        viewModel.getMoveToOtpVerifyPage().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean move) {
                if (move) {
                    Logger.e(TAG, "MoveToOtpVerifyPage:" + move);
                    navController.navigate(R.id.action_navigation_request_otp_to_navigation_verify_otp);
                }
            }
        });
    }

    @Override
    public void setListeners() {
        binding.fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.fab)) {
            viewModel.setNextButtonClicked();
        }
    }

}
