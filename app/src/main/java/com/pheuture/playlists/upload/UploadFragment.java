package com.pheuture.playlists.upload;

import androidx.core.content.FileProvider;
import  androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.progress_dialog.ProgressDialog;
import com.pheuture.playlists.utils.progress_dialog.ProgressDialogActionInterface;

import org.jetbrains.annotations.NotNull;

import java.nio.file.spi.FileSystemProvider;

import static com.pheuture.playlists.utils.RequestCodeConstant.REQUEST_CODE_FILE_SELECT;

public class UploadFragment extends BaseFragment implements ProgressDialogActionInterface.ClickListener{
    private static final String TAG = UploadFragment.class.getSimpleName();
    private FragmentActivity activity;
    private UploadViewModel viewModel;
    private FragmentUploadBinding binding;
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;
    private Uri mediaUri;
    private Uri thumbnailUri;
    private int lastProgress = 0;
    private ProgressDialogActionInterface progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        progressDialog = new ProgressDialog(activity, this);
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

        if (mediaUri == null){
            Toast.makeText(activity, "Invalid media file", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel = ViewModelProviders.of(this, new UploadViewModelFactory(
                activity.getApplication(), mediaUri)).get(UploadViewModel.class);

        playerView = binding.playerView;
        exoPlayer = viewModel.getExoPlayer();
        exoPlayer.setPlayWhenReady(false);
        playerView.setPlayer(exoPlayer);

        viewModel.getProgressStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                if(show){
                    progressDialog.show("Uploading Video");
                } else {
                    progressDialog.dismiss();
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
                   progressDialog.setProgress(100);
                } else {
                    progressDialog.setProgress(lastProgress);
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
                    progressDialog.dismiss();
                    ((MainActivity) activity).showSnack("uploading failed");
                }
            }
        });

        viewModel.getThumbnailLive().observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri uri) {
                thumbnailUri = uri;
                showThumbnail();
            }
        });

        viewModel.getMediaUri().observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri uri) {
                mediaUri = uri;
                setMediaInPlayer();
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
                    viewModel.uploadMedia(binding.ediTextTitle.getText().toString(), binding.ediTextDescription.getText().toString());
                }
            };
            proceedWithPermissions(activity, runnable, false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_FILE_SELECT && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                viewModel.setThumbnailUri(resultData.getData());
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

    @Override
    public void onCancelled() {
        Logger.e(TAG, "onCancelled");
        /*viewModel.cancelUpload();*/
    }
}
