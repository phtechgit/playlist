package com.pheuture.playlists.upload;

import  androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ActivityUploadBinding;
import com.pheuture.playlists.utils.BaseActivity;
import com.pheuture.playlists.utils.RequestCodes;

public class UploadActivity extends BaseActivity implements RequestCodes{
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
    public void initializations() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload);

        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

        viewModel = ViewModelProviders.of(this,
                new UploadActivityViewModelFactory(getApplication(),
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
    }

    private void setMediaInPlayer() {
        MediaSource mediaSource;
        mediaSource = new ProgressiveMediaSource.Factory(viewModel.getDataSourceFactory())
                .createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);
    }

    @Override
    public void setListeners() {
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
            proceedWithPermissions(runnable, false);

        } else if (v.equals(binding.buttonSubmit)){
            if (TextUtils.getTrimmedLength(binding.ediTextTitle.getText().toString()) == 0){
                Toast.makeText(this, "Please provide video title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.getTrimmedLength(binding.ediTextDescription.getText().toString()) == 0){
                Toast.makeText(this, "Please provide video description", Toast.LENGTH_SHORT).show();
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    viewModel.uploadMedia(binding.ediTextTitle.getText().toString(), binding.ediTextDescription.getText().toString());
                    Toast.makeText(UploadActivity.this, "file added to upload queue", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            };
            proceedWithPermissions(runnable, false);
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
}