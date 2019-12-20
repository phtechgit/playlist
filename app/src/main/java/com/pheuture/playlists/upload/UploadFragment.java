package com.pheuture.playlists.upload;

import androidx.appcompat.app.AppCompatDelegate;
import  androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentUploadBinding;
import com.pheuture.playlists.utils.AlerterUtils;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.ContentProvider;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.RealPathUtil;

import java.io.File;

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

        /*String mimeType = ContentProvider.getContentResolver(activity).getType(returnUri);

        Cursor returnCursor = ContentProvider.getContentResolver(activity)
                .query(returnUri, null, null, null,
                        null);

        if (returnCursor!=null && returnCursor.getCount()>0) {
            returnCursor.moveToFirst();

            String fileName = returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            long fileSize = returnCursor.getLong(returnCursor.getColumnIndex(OpenableColumns.SIZE));

            Logger.e(TAG, "fileName:" + fileName + ", fileType:" + mimeType + ", fileSize:" + fileSize);
            returnCursor.close();
        }*/

        playerView = binding.playerView;
        exoPlayer = viewModel.getExoPlayer();
        exoPlayer.setPlayWhenReady(false);
        playerView.setPlayer(exoPlayer);

        MediaSource mediaSource;
        mediaSource = new ProgressiveMediaSource.Factory(viewModel.getDataSourceFactory())
                .createMediaSource(mediaUri);
        exoPlayer.prepare(mediaSource);

        viewModel.getProgressStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                if(show){
                    AlerterUtils.progressDeterminateShow(activity, "Uploading Video");
                } else {
                    AlerterUtils.progressDeterminateDismiss();
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

    @Override
    public void setListeners() {
        binding.imageViewThumbnail.setOnClickListener(this);
        binding.buttonSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.imageViewThumbnail)){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_FILE_SELECT);
            }
        } else if (v.equals(binding.buttonSubmit)){
            viewModel.submitMedia(new File(RealPathUtil.getRealPath(activity, mediaUri)), new File(RealPathUtil.getRealPath(activity, thumbnailUri)), binding.ediTextTitle.getText().toString(), binding.ediTextDescription.getText().toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_FILE_SELECT && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                thumbnailUri = resultData.getData();
                if (thumbnailUri != null) {
                    binding.imageViewThumbnail.setPadding(0,0,0,0);
                    Glide.with(activity)
                            .load(thumbnailUri)
                            .into(binding.imageViewThumbnail);
                }
            }
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
