package com.pheuture.playlists.auth;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.user_detail.UserProfileActivity;
import com.pheuture.playlists.datasource.local.user_handler.UserModel;
import com.pheuture.playlists.databinding.ActivityAuthBinding;
import com.pheuture.playlists.interfaces.ButtonClickInterface;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.Constants;
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
                R.id.navigation_request_otp, R.id.navigation_verify_otp)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_auth);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        UserModel user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                this, Constants.USER, ""), UserModel.class);

        if (user != null && user.getUserID()!=0 && StringUtils.isEmpty(user.getUserName())){
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            finish();

        } else if(user != null && user.getUserID()!=0 && !StringUtils.isEmpty(user.getUserName())){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void setListeners() {
        binding.fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.fab)) {
            buttonClickInterface.onButtonClick();
        }
    }

    public void setOnButtonClickListener(Fragment fragment){
        if (fragment instanceof ButtonClickInterface) {
            this.buttonClickInterface = (ButtonClickInterface) fragment;
        }
    }

    public void showNextButton(boolean status) {
        if (status) {
            binding.fab.show();
        } else {
            binding.fab.hide();
        }
    }

    @Override
    public void onBackPressed() {
        if (!navController.popBackStack()) {
            super.onBackPressed();
        }
    }
}
