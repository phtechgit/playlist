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
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
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
    private List<PlaylistMediaEntity> playlistMediaEntities;
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

        viewModel.getPlaylistMediaLive().observe(this, new Observer<List<PlaylistMediaEntity>>() {
            @Override
            public void onChanged(List<PlaylistMediaEntity> newPalylistMediaEntities) {
                playlistMediaEntities = newPalylistMediaEntities;
                recyclerAdapter.setData(playlistMediaEntities);

                //show/hide play pause button
                if (newPalylistMediaEntities.size()>0){
                    binding.imageButtonPlay.setVisibility(View.VISIBLE);
                    if (newPalylistMediaEntities.size()>2) {
                        binding.imageButtonShuffle.setVisibility(View.VISIBLE);
                    } else {
                        binding.imageButtonShuffle.setVisibility(View.GONE);
                    }
                } else {
                    binding.imageButtonPlay.setVisibility(View.GONE);
                    binding.imageButtonShuffle.setVisibility(View.GONE);
                }

                viewModel.addToOfflineMedia(playlistMediaEntities);
            }
        });

        viewModel.getProgressStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                /*if(show){
                    showProgress(binding.progressLayout.progressFullscreen, true);
                } else {
                    hideProgress(binding.progressLayout.progressFullscreen);
                }*/
            }
        });
    }

    @Override
    public void setListeners() {
        binding.imageButtonPlay.setOnClickListener(this);
        binding.imageButtonShuffle.setOnClickListener(this);
        binding.imageButtonAddNewSong.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.imageButtonAddNewSong)){
            Bundle bundle = new Bundle();
            bundle.putParcelable(ARG_PARAM1, playlist);

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_playlist_detail_to_navigation_media, bundle);

        } else if (v.equals(binding.imageButtonPlay)) {
            ((MainActivity) activity).setMedia(playlist, playlistMediaEntities);

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
