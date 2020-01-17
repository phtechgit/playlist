package com.pheuture.playlists.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.MainActivityViewModel;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentMediaBinding;
import com.pheuture.playlists.datasource.local.media_handler.queue.QueueMediaEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.media_handler.MediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.base.BaseFragment;
import com.pheuture.playlists.utils.ParserUtil;

import java.util.ArrayList;
import java.util.List;

public class MediaFragment extends BaseFragment implements TextWatcher, RecyclerViewClickListener {
    private static final String TAG = MediaFragment.class.getSimpleName();
    private FragmentMediaBinding binding;
    private MainActivityViewModel parentViewModel;
    private MediaViewModel viewModel;
    private MediaRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;
    private PlaylistEntity playlistModel;
    private FragmentActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        playlistModel = getArguments().getParcelable(ARG_PARAM1);
        parentViewModel = ViewModelProviders.of(activity).get(MainActivityViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_media, container, false);
        viewModel = ViewModelProviders.of(this, new MediaViewModelFactory(
                activity.getApplication(), playlistModel)).get(MediaViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        parentViewModel.setTitle(playlistModel.getPlaylistName());

        binding.layoutSearchBar.editTextSearch.setHint("Search new Videos");

        recyclerAdapter = new MediaRecyclerAdapter(this);
        recyclerAdapter.setHasStableIds(true);
        layoutManager = new LinearLayoutManager(activity);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);
        binding.recyclerView.setHasFixedSize(true);

        viewModel.getPlaylistMediaListLive().observe(this, new Observer<List<MediaEntity>>() {
            @Override
            public void onChanged(List<MediaEntity> videoEntities) {
                recyclerAdapter.setData(videoEntities);
            }
        });
    }

    @Override
    public void setListeners() {
        binding.recyclerView.addOnScrollListener(scrollListener);
        binding.layoutSearchBar.editTextSearch.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        viewModel.setSearchQuery(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int totalItemCount = layoutManager.getItemCount();
            int visibleItemCount = layoutManager.getChildCount();
            int currentPosition = layoutManager.findLastVisibleItemPosition();
            int firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
            int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            int remainingItems = totalItemCount - currentPosition;

            //fetch more data from server
            if (dy > 0 && remainingItems < visibleItemCount) {
                viewModel.getMoreData();
            }
        }
    };

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {
        int type = bundle.getInt(ARG_PARAM1, -1);
        int position = bundle.getInt(ARG_PARAM2, -1);
        MediaEntity mediaEntity = bundle.getParcelable(ARG_PARAM3);

        String objectJsonString = ParserUtil.getInstance().toJson(mediaEntity,
                MediaEntity.class);

        if (type == 1){
            QueueMediaEntity queueMediaEntity = ParserUtil.getInstance()
                    .fromJson(objectJsonString, QueueMediaEntity.class);

            parentViewModel.setMedia(null, queueMediaEntity, true);

        } else {
            PlaylistMediaEntity playlistMediaEntity = ParserUtil.getInstance()
                    .fromJson(objectJsonString, PlaylistMediaEntity.class);

            parentViewModel.showSnackBar("added to " + playlistModel.getPlaylistName(),
                    Snackbar.LENGTH_SHORT);

            viewModel.addMediaToPlaylist(position, playlistMediaEntity);
            parentViewModel.setNewMediaAdded(true);
        }
    }

    @Override
    public void onRecyclerViewItemLongClick(Bundle bundle) {

    }
}
