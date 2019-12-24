package com.pheuture.playlists.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.user_detail.UserModel;
import com.pheuture.playlists.databinding.ActivityAuthBinding;
import com.pheuture.playlists.interfaces.ButtonClickInterface;
import com.pheuture.playlists.utils.ApiConstant;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.StringUtils;

public class AuthActivity extends BaseActivity {
    private static final String TAG = AuthActivity.class.getSimpleName();
    private ActivityAuthBinding binding;
    private FragmentManager fragmentManager;
    private ButtonClickInterface buttonClickInterface;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.master_menu, menu);
        return true;
    }

    @Override
    public void initializations() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth);

        UserModel user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(this, Constants.USER, ""), UserModel.class);
        if (user == null || user.getUserId()==0){
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.navigation_request_otp);

        } else if (StringUtils.isEmpty(user.getUserName())){
            Bundle bundle = new Bundle();
            bundle.putParcelable(ARG_PARAM1, user);

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_verify_otp_to_navigation_user_detail);

        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.button)) {
            buttonClickInterface.onButtonClick();
        }
    }

    public void setOnButtonClickListener(Fragment fragment){
        if (fragment instanceof ButtonClickInterface) {
            this.buttonClickInterface = (ButtonClickInterface) fragment;
        }
    }

}
