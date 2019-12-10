package com.pheuture.playlists.videos;

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

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentVideosBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends BaseFragment implements TextWatcher, RecyclerViewInterface {
    private static final String TAG = VideosFragment.class.getSimpleName();
    private FragmentVideosBinding binding;
    private VideosViewModel viewModel;
    private VideosRecyclerAdapter recyclerAdapter;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_videos, container, false);
        viewModel = ViewModelProviders.of(this, new VideosViewModelFactory(
                activity.getApplication(), playlistModel)).get(VideosViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void initializations() {
        playlistModel = getArguments().getParcelable(ARG_PARAM1);

        binding.layoutSearchBar.editTextSearch.setText(viewModel.getSearchQuery().getValue());
        binding.layoutSearchBar.editTextSearch.setSelection(binding.layoutSearchBar.editTextSearch.getText().length());

        recyclerAdapter = new VideosRecyclerAdapter(this);
        layoutManager = new LinearLayoutManager(activity);

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
                    activity.onBackPressed();
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
        }
    };

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {
        int type = bundle.getInt(ARG_PARAM1, -1);
        int position = bundle.getInt(ARG_PARAM2, -1);
        VideoEntity video = bundle.getParcelable(ARG_PARAM3);

        assert video != null;
        if (type == 1){
            List<VideoEntity> videos = new ArrayList<VideoEntity>();
            videos.add(video);
            ((MainActivity) activity).setMedia(null, videos);
        } else {
            viewModel.addVideoToPlaylist(video.getId());
        }
    }

    @Override
    public void onRecyclerViewItemLongClick(Bundle bundle) {

    }

}
