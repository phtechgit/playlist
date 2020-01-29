package com.pheuture.playlists.auth.user_detail;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ActivityUserProfileBinding;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.base.BaseActivity;
import com.pheuture.playlists.constants.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;

public class UserProfileActivity extends BaseActivity implements TextWatcher {
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    private ActivityUserProfileBinding binding;
    private UserProfileViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);

        UserEntity user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                this, Constants.USER, ""), UserEntity.class);

        viewModel = ViewModelProviders.of(this, new UserProfileViewModelFactory( getApplication(), user)).get(UserProfileViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.ediTextFirstName.addTextChangedListener(this);
        binding.ediTextLastName.addTextChangedListener(this);
        binding.fab.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.ediTextFirstName.requestFocus();
    }

    @Override
    public void onClick(View v) {
        viewModel.updateUserDetail(binding.ediTextFirstName.getText().toString(), binding.ediTextLastName.getText().toString());
        finish();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
