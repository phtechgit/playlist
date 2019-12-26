package com.pheuture.playlists.auth.user_detail;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.AuthActivity;
import com.pheuture.playlists.databinding.ActivityUserProfileBinding;
import com.pheuture.playlists.datasource.local.user_handler.UserModel;
import com.pheuture.playlists.interfaces.ButtonClickInterface;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;

public class UserProfileActivity extends BaseActivity implements TextWatcher {
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    private ActivityUserProfileBinding binding;
    private UserProfileViewModel viewModel;
    private UserModel user;

    @Override
    public void initializations() {
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                this, Constants.USER, ""), UserModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);
        viewModel = ViewModelProviders.of(this, new UserProfileViewModelFactory( getApplication(), user)).get(UserProfileViewModel.class);
    }

    @Override
    public void setListeners() {
        binding.ediTextFirstName.addTextChangedListener(this);
        binding.ediTextLastName.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        viewModel.updateUserDetail(binding.ediTextFirstName.getText().toString(), binding.ediTextLastName.getText().toString());

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (binding.ediTextFirstName.getText().length()>0) {
            binding.fab.show();
        } else {
            binding.fab.hide();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
