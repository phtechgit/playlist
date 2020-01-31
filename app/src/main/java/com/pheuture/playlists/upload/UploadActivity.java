package com.pheuture.playlists.upload;

import androidx.annotation.Nullable;
import  androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.ActivityNavigator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ActivityUploadBinding;
import com.pheuture.playlists.base.BaseActivity;

public class UploadActivity extends BaseActivity{
    private static final String TAG = UploadActivity.class.getSimpleName();
    private UploadViewModel viewModel;
    private ActivityUploadBinding binding;
    private SimpleExoPlayer exoPlayer;
    private Uri mediaUri;
    private Uri thumbnailUri;

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload);

        viewModel = new ViewModelProvider(this, new UploadActivityViewModelFactory(getApplication(),
                        getIntent().getParcelableExtra(ARG_PARAM1))).get(UploadViewModel.class);

        PlayerView playerView = binding.playerView;
        exoPlayer = viewModel.getExoPlayer();
        exoPlayer.setPlayWhenReady(false);
        playerView.setPlayer(exoPlayer);

        viewModel.getMediaUriLive().observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri uriLive) {
                mediaUri = uriLive;
                setMediaInPlayer();
            }
        });

        viewModel.getThumbnailUriLive().observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri uriLive) {
                thumbnailUri = uriLive;
                showThumbnail();
            }
        });

        viewModel.getSnackBar().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                if (bundle.getBoolean(SNACK_BAR_SHOW, false)){
                    showSnack(binding.getRoot(), bundle);
                } else {
                    hideSnack();
                }
            }
        });
    }

    private void setMediaInPlayer() {
        MediaSource mediaSource;
        mediaSource = new ProgressiveMediaSource.Factory(viewModel.getDataSourceFactory())
                .createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.imageViewThumbnail.setOnClickListener(this);
        binding.buttonSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.imageViewThumbnail)){
            Runnable runnable = new Runnable() {
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_CODE_IMAGE_FILE_SELECT);
                    }
                }
            };
            proceedWithPermissions(REQUEST_CODE_GRANT_PERMISSIONS, READ_WRITE_EXTERNAL_STORAGE_PERMISSION, runnable, false);

        } else if (v.equals(binding.buttonSubmit)){
            if (TextUtils.getTrimmedLength(binding.ediTextTitle.getText().toString()) == 0){
                viewModel.showSnackBar(getResources().getString(R.string.please_provide_video_title), Snackbar.LENGTH_SHORT);
                return;
            }

            if (TextUtils.getTrimmedLength(binding.ediTextDescription.getText().toString()) == 0){
                viewModel.showSnackBar(getResources().getString(R.string.please_provide_video_description), Snackbar.LENGTH_SHORT);
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    viewModel.uploadMedia(binding.ediTextTitle.getText().toString(), binding.ediTextDescription.getText().toString());
                    Toast.makeText(UploadActivity.this, getResources().getString(R.string.thanks_msg_upon_uploading_own_video),
                            Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            };
            proceedWithPermissions(REQUEST_CODE_GRANT_PERMISSIONS,READ_WRITE_EXTERNAL_STORAGE_PERMISSION, runnable, false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == REQUEST_CODE_IMAGE_FILE_SELECT && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                viewModel.setThumbnailUriLiveData(resultData.getData());
            }
        }
    }

    private void showThumbnail() {
        if (thumbnailUri != null) {
            binding.imageViewThumbnail.setPadding(0,0,0,0);
            Glide.with(this)
                    .load(thumbnailUri)
                    .into(binding.imageViewThumbnail);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayer!=null){
            exoPlayer.release();
        }
    }

    @Override
    public void finish() {
        super.finish();
        ActivityNavigator.applyPopAnimationsToPendingTransition(this);
    }

}
