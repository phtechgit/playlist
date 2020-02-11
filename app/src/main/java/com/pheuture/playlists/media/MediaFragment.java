package com.pheuture.playlists.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.home.MainActivityViewModel;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentMediaBinding;
import com.pheuture.playlists.queue.QueueMediaEntity;
import com.pheuture.playlists.playlist.PlaylistEntity;
import com.pheuture.playlists.playist_detail.PlaylistMediaEntity;
import com.pheuture.playlists.base.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.base.BaseFragment;
import com.pheuture.playlists.base.utils.ParserUtil;

import java.util.Arrays;
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
    private List<MediaEntity> mediaEntities;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        Object sharedElementEnterTransition = TransitionInflater.from(activity)
                .inflateTransition(android.R.transition.fade);
        setSharedElementEnterTransition(sharedElementEnterTransition);
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            playlistModel = getArguments().getParcelable(ARG_PARAM1);
        }
        parentViewModel = new ViewModelProvider(activity).get(MainActivityViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_media, container, false);
        viewModel = new ViewModelProvider(this, new MediaViewModelFactory(
                activity.getApplication(), playlistModel)).get(MediaViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        parentViewModel.setTitle(playlistModel.getPlaylistName());

        recyclerAdapter = new MediaRecyclerAdapter(this);
        recyclerAdapter.setHasStableIds(true);
        layoutManager = new LinearLayoutManager(activity);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);
        binding.recyclerView.setHasFixedSize(true);

        /*Parcelable state = layoutManager.onSaveInstanceState();
        layoutManager.onRestoreInstanceState(state);*/

        viewModel.getPlaylistMediaListLive().observe(this, new Observer<List<MediaEntity>>() {
            @Override
            public void onChanged(List<MediaEntity> newMediaEntities) {
                mediaEntities = newMediaEntities;
                recyclerAdapter.setData(mediaEntities);
                if (mediaEntities.size()>0){
                    binding.textViewEmptySearchResult.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerView.setVisibility(View.GONE);
                    if (viewModel.getSearchQuery().length()>0) {
                        binding.textViewEmptySearchResult.setVisibility(View.VISIBLE);
                    }
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
    public void onRecyclerViewHolderClick(RecyclerView.ViewHolder viewHolder, Bundle bundle) {
        int type = bundle.getInt(ARG_PARAM1, -1);
        int position = bundle.getInt(ARG_PARAM2, -1);
        MediaEntity mediaEntity = bundle.getParcelable(ARG_PARAM3);

        if (type == SELECT){
            String objectJsonString = ParserUtil.getInstance().toJson(mediaEntities);
            List<QueueMediaEntity> queueMediaEntities = Arrays.asList(ParserUtil.getInstance()
                    .fromJson(objectJsonString, QueueMediaEntity[].class));

            parentViewModel.setMediaListToQueue(queueMediaEntities, position);

        } else {
            String objectJsonString = ParserUtil.getInstance().toJson(mediaEntity);

            if (viewModel.mediaAlreadyAddedToPlaylist(mediaEntity.getMediaID())){
                parentViewModel.showSnackBar("Already added to " + playlistModel.getPlaylistName(),
                        Snackbar.LENGTH_SHORT);
                return;
            }

            PlaylistMediaEntity playlistMediaEntity = ParserUtil.getInstance()
                    .fromJson(objectJsonString, PlaylistMediaEntity.class);

            parentViewModel.showSnackBar("added to " + playlistModel.getPlaylistName(),
                    Snackbar.LENGTH_SHORT);

            viewModel.addMediaToPlaylist(position, playlistMediaEntity);
            parentViewModel.setNewMediaAdded(true);
        }
    }

    @Override
    public void onRecyclerViewHolderLongClick(Bundle bundle) {

    }
}
