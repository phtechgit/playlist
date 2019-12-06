package com.pheuture.playlists.videos;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ActivityVideosBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.BaseActivity;

import java.util.List;

public class VideosActivity extends BaseActivity implements TextWatcher, RecyclerViewInterface {
    private static final String TAG = VideosActivity.class.getSimpleName();
    private ActivityVideosBinding binding;
    private VideosViewModel viewModel;
    private VideosRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;
    private PlaylistEntity playlistModel;

    @Override
    public void initializations() {
        playlistModel = getIntent().getParcelableExtra(ARG_PARAM1);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_videos);

        setSupportActionBar(binding.toolbar);

        assert playlistModel != null;
        setTitle(playlistModel.getPlaylistName());

        viewModel = ViewModelProviders.of(this, new VideosViewModelFactory(
                this.getApplication(), playlistModel)).get(VideosViewModel.class);

        binding.layoutSearchBar.editTextSearch.setText(viewModel.getSearchQuery().getValue());
        binding.layoutSearchBar.editTextSearch.setSelection(binding.layoutSearchBar.editTextSearch.getText().length());

        recyclerAdapter = new VideosRecyclerAdapter(this);
        layoutManager = new LinearLayoutManager(this);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);

        viewModel.getVideosLive().observe(this, new Observer<List<VideoEntity>>() {
            @Override
            public void onChanged(List<VideoEntity> videoEntities) {
                recyclerAdapter.setData(videoEntities);
            }
        });

        viewModel.getSearchQuery().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                viewModel.getFreshData();
            }
        });

        viewModel.getNeedToUpdateParent().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                if (value) {
                    onBackPressed();
                }
            }
        });

        viewModel.getProgressStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                if(show){
                    showProgress(binding.progressLayout.progressFullscreen, true);
                } else {
                    hideProgress(binding.progressLayout.progressFullscreen);
                }
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

            //update current positions in adapter
            recyclerAdapter.setCurrentPositions(firstVisibleItemPosition, lastVisibleItemPosition);
        }
    };

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {
        int position = bundle.getInt(ARG_PARAM1, -1);
        VideoEntity model = bundle.getParcelable(ARG_PARAM2);

        assert model != null;
        viewModel.addVideoToPlaylist(model.getId());
    }

    @Override
    public void onRecyclerViewItemLongClick(Bundle bundle) {

    }

}
