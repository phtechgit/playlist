package com.pheuture.playlists.auth;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.user_detail.UserModel;
import com.pheuture.playlists.databinding.ActivityAuthBinding;
import com.pheuture.playlists.interfaces.ButtonClickInterface;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.StringUtils;

public class AuthActivity extends BaseActivity {
    private static final String TAG = AuthActivity.class.getSimpleName();
    private ActivityAuthBinding binding;
    private ButtonClickInterface buttonClickInterface;
    private NavController navController;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.master_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initializations() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth);
        setSupportActionBar(binding.toolbar);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_request_otp, R.id.navigation_verify_otp, R.id.navigation_user_detail)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_auth);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        UserModel user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                this, Constants.USER, ""), UserModel.class);

        if (user != null && user.getUserId()!=0){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void setListeners() {
        binding.button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.button)) {
            buttonClickInterface.onButtonClick();
        }
    }

    public void setOnButtonClickListener(Fragment fragment){
        Logger.e(TAG, "setOnButtonClickListener");
        if (fragment instanceof ButtonClickInterface) {
            this.buttonClickInterface = (ButtonClickInterface) fragment;
        }
    }

    public void showNextButton(boolean status) {
        binding.button.setEnabled(status);
    }

    @Override
    public void onBackPressed() {
        if (!navController.popBackStack()) {
            super.onBackPressed();
        }
    }
}
