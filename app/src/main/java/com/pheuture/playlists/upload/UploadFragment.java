package com.pheuture.playlists.upload;

import  androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentUploadBinding;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.ContentProvider;
import com.pheuture.playlists.utils.Logger;

public class UploadFragment extends BaseFragment {
    private static final String TAG = UploadFragment.class.getSimpleName();
    private FragmentActivity activity;
    private UploadViewModel viewModel;
    private FragmentUploadBinding binding;
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;

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
        Uri returnUri = Uri.parse(getArguments().getString(ARG_PARAM1));

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
                .createMediaSource(returnUri);
        exoPlayer.prepare(mediaSource);
    }

    @Override
    public void setListeners() {
    }

    @Override
    public void onClick(View v) {

    }
}
