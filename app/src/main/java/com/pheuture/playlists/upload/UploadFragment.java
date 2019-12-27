package com.pheuture.playlists.upload;

import  androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentUploadBinding;
import com.pheuture.playlists.utils.AlerterUtils;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.RealPathUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND;
import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;
import static com.pheuture.playlists.utils.RequestCodeConstant.REQUEST_CODE_FILE_SELECT;

public class UploadFragment extends BaseFragment {
    private static final String TAG = UploadFragment.class.getSimpleName();
    private FragmentActivity activity;
    private UploadViewModel viewModel;
    private FragmentUploadBinding binding;
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;
    private Uri mediaUri;
    private Uri thumbnailUri;
    private long lastProgress = 0;
    private Dialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(UploadViewModel.class);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_upload, container,false);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        assert getArguments() != null;
        mediaUri = getArguments().getParcelable(ARG_PARAM1);

        playerView = binding.playerView;
        exoPlayer = viewModel.getExoPlayer();
        exoPlayer.setPlayWhenReady(false);
        playerView.setPlayer(exoPlayer);

        MediaSource mediaSource;
        mediaSource = new ProgressiveMediaSource.Factory(viewModel.getDataSourceFactory())
                .createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);

        createAndSetThumbnail();

        viewModel.getProgressStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                if(show){
                    alertDialog = AlerterUtils.progressDeterminateShow(activity, "Uploading Video");
                } else {
                    AlerterUtils.progressDeterminateDismiss(alertDialog);
                }
            }
        });

        viewModel.getUploadedStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean uploaded) {
                if (uploaded == null){
                    return;
                }
                if (uploaded) {
                    ((MainActivity) activity).showSnack("uploaded successfully");
                    activity.onBackPressed();
                } else {
                    ((MainActivity) activity).showSnack("uploading failed");
                }
            }
        });

        viewModel.getProgressPercentage().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer percentage) {
                if (percentage<lastProgress){
                    return;
                }
                lastProgress = percentage;

                if (percentage>100) {
                    AlerterUtils.progressDeterminateUpdateProgress(100);
                } else {
                    AlerterUtils.progressDeterminateUpdateProgress(lastProgress);
                }
            }
        });
    }

    private void createAndSetThumbnail() {
         Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(
                RealPathUtil.getRealPath(activity, mediaUri), FULL_SCREEN_KIND);

        try {
            File thumbnailFile = File.createTempFile("thumbnail", ".png", activity.getCacheDir());
            if (!thumbnailFile.exists()){
                if (!thumbnailFile.createNewFile()){
                    return;
                }
            }
            FileOutputStream fos = new FileOutputStream(thumbnailFile);
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();

            thumbnailUri = Uri.fromFile(thumbnailFile);

            showThumbnail();

        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
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
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_CODE_FILE_SELECT);
                    }
                }
            };
            proceedWithPermissions(activity, runnable, false);
        } else if (v.equals(binding.buttonSubmit)){
            if (TextUtils.getTrimmedLength(binding.ediTextTitle.getText().toString()) == 0){
                Toast.makeText(activity, "Please provide video title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.getTrimmedLength(binding.ediTextDescription.getText().toString()) == 0){
                Toast.makeText(activity, "Please provide video description", Toast.LENGTH_SHORT).show();
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    viewModel.submitMedia(mediaUri, thumbnailUri, binding.ediTextTitle.getText().toString(), binding.ediTextDescription.getText().toString());
                }
            };
            proceedWithPermissions(activity, runnable, false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_FILE_SELECT && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                thumbnailUri = resultData.getData();
                showThumbnail();
            }
        }
    }

    private void showThumbnail() {
        if (thumbnailUri != null) {
            binding.imageViewThumbnail.setPadding(0,0,0,0);
            Glide.with(activity)
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
