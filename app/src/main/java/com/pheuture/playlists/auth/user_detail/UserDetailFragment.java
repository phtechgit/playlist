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
import com.pheuture.playlists.databinding.FragmentUserDetailBinding;
import com.pheuture.playlists.datasource.local.user_handler.UserModel;
import com.pheuture.playlists.interfaces.ButtonClickInterface;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;

public class UserDetailFragment extends BaseFragment implements TextWatcher, ButtonClickInterface {
    private static final String TAG = UserDetailFragment.class.getSimpleName();
    private FragmentActivity activity;
    private FragmentUserDetailBinding binding;
    private UserDetailViewModel viewModel;
    private UserModel user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        ((AuthActivity)activity).setOnButtonClickListener(this);
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                activity, Constants.USER, ""), UserModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_detail, container, false);
        viewModel = ViewModelProviders.of(this, new UserDetailViewModelFactory(activity.getApplication(), user)).get(UserDetailViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        if (binding.ediTextFirstName.getText().length()>0) {
            ((AuthActivity)activity).showNextButton(true);
        } else {
            ((AuthActivity)activity).showNextButton(false);
        }
    }

    @Override
    public void setListeners() {
        binding.ediTextFirstName.addTextChangedListener(this);
        binding.ediTextLastName.addTextChangedListener(this);
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
        viewModel.updateUserDetail(binding.ediTextFirstName.getText().toString(), binding.ediTextLastName.getText().toString());

        Intent intent = new Intent(activity, MainActivity.class);
        startActivity(intent);
        activity.finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (binding.ediTextFirstName.getText().length()>0) {
            ((AuthActivity)activity).showNextButton(true);
        } else {
            ((AuthActivity)activity).showNextButton(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
