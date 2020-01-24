package com.pheuture.playlists.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.auth.user_detail.UserProfileActivity;
import com.pheuture.playlists.datasource.local.user_handler.UserEntity;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;
import com.pheuture.playlists.utils.StringUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            UserEntity user = ParserUtil.getInstance().fromJson(SharedPrefsUtils.getStringPreference(
                    SplashActivity.this, Constants.USER, ""), UserEntity.class);

            if(user != null && user.getUserID()!=0 && !StringUtils.isEmpty(user.getUserFirstName())){
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            } else if (user != null && user.getUserID()!=0 && StringUtils.isEmpty(user.getUserFirstName())){
                Intent intent = new Intent(SplashActivity.this, UserProfileActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
                startActivity(intent);
            }
            finish();
        }, 1000);
    }
}
