package com.pheuture.playlists.playlists.detail;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentPlaylistDetailBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.video_handler.VideoEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.BaseFragment;

import java.util.List;

public class PlaylistDetailFragment extends BaseFragment implements RecyclerViewInterface {
    private static final String TAG = PlaylistDetailFragment.class.getSimpleName();
    private FragmentPlaylistDetailBinding binding;
    private PlaylistDetailViewModel viewModel;
    private PlaylistVideosRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;
    private PlaylistEntity playlist;
    private List<VideoEntity> videos;
    private FragmentActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() == null) {
            return null;
        }
        playlist = getArguments().getParcelable(ARG_PARAM1);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist_detail, container, false);
        viewModel = ViewModelProviders.of(this, new PlaylistDetailViewModelFactory(
                activity.getApplication(), playlist)).get(PlaylistDetailViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        viewModel.getPlaylistEntity().observe(this, new Observer<PlaylistEntity>() {
            @Override
            public void onChanged(PlaylistEntity playlistEntity) {
                playlist = playlistEntity;
                binding.setModel(playlistEntity);
            }
        });

        layoutManager = new LinearLayoutManager(activity);
        recyclerAdapter = new PlaylistVideosRecyclerAdapter(this);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);

        viewModel.getVideosLive().observe(this, new Observer<List<VideoEntity>>() {
            @Override
            public void onChanged(List<VideoEntity> videoEntities) {
                videos = videoEntities;
                recyclerAdapter.setData(videoEntities);

                //show/hide play pause button
                if (videoEntities.size()>0){
                    binding.imageButtonPlay.setVisibility(View.VISIBLE);
                    if (videoEntities.size()>2) {
                        binding.imageButtonShuffle.setVisibility(View.VISIBLE);
                    } else {
                        binding.imageButtonShuffle.setVisibility(View.GONE);
                    }
                } else {
                    binding.imageButtonPlay.setVisibility(View.GONE);
                    binding.imageButtonShuffle.setVisibility(View.GONE);
                }

                viewModel.addToOfflineMedia(videos);
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

        /*viewModel.isPlayling().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                isPlaying = value;
                exoPlayer.setPlayWhenReady(isPlaying);
                if (isPlaying){
                    binding.imageButtonPlay.setImageResource(R.drawable.ic_pause_circular_light);
                } else {
                    binding.imageButtonPlay.setImageResource(R.drawable.ic_play_circular_white);
                }

                if (isPlaying && playerPosition== RecyclerView.NO_POSITION){
                    viewModel.setPlayerPosition(0);
                }
            }
        });

        viewModel.getPlayerPosition().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer value) {
                playerPosition = value;
                recyclerAdapter.setPlayerPosition(playerPosition);
            }
        });*/
    }

    @Override
    public void setListeners() {
        binding.imageButtonPlay.setOnClickListener(this);
        binding.imageButtonShuffle.setOnClickListener(this);
        binding.imageButtonAddNewSong.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.getFreshData();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.imageButtonAddNewSong)){
            Bundle bundle = new Bundle();
            bundle.putParcelable(ARG_PARAM1, playlist);

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_playlist_detail_to_navigation_videos, bundle);

        } else if (v.equals(binding.imageButtonPlay)) {
            ((MainActivity) activity).setMedia(playlist, videos);

        } else if (v.equals(binding.imageButtonShuffle)) {
            ((MainActivity) activity).toggleShuffleMode();
        }
    }

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {

    }

    @Override
    public void onRecyclerViewItemLongClick(Bundle bundle) {

    }
}
