package com.pheuture.playlists.trending;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.MainActivityViewModel;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentTrendingsBinding;
import com.pheuture.playlists.datasource.local.media_handler.queue.QueueMediaEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.media_handler.MediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.base.BaseFragment;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;

import java.util.ArrayList;
import java.util.List;

public class TrendingFragment extends BaseFragment implements TextWatcher, RecyclerViewClickListener {
    private static final String TAG = TrendingFragment.class.getSimpleName();
    private FragmentActivity activity;
    private MainActivityViewModel parentViewModel;
    private FragmentTrendingsBinding binding;
    private TrendingViewModel viewModel;
    private TrendingRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trendings, container, false);
        parentViewModel = ViewModelProviders.of(activity).get(MainActivityViewModel.class);
        viewModel = ViewModelProviders.of(this).get(TrendingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        parentViewModel.setTitle(activity.getResources().getString(R.string.trending_title));
        binding.layoutSearchBar.editTextSearch.setHint(activity.getResources().getString(R.string.trending_search_hint));

        recyclerAdapter = new TrendingRecyclerAdapter(this);
        layoutManager = new LinearLayoutManager(activity);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);

        viewModel.getTrendingMediaLive().observe(this, new Observer<List<MediaEntity>>() {
            @Override
            public void onChanged(List<MediaEntity> videoEntities) {
                recyclerAdapter.setData(videoEntities);
            }
        });

        recyclerAdapter.getDataCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                if (count>0){
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
        recyclerAdapter.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onRecyclerViewHolderClick(Bundle bundle) {
        MediaEntity mediaEntity = bundle.getParcelable(ARG_PARAM2);

        String objectJsonString = ParserUtil.getInstance().toJson(mediaEntity,
                MediaEntity.class);
        QueueMediaEntity queueMediaEntity = ParserUtil.getInstance()
                .fromJson(objectJsonString, QueueMediaEntity.class);

        parentViewModel.setMedia(null, queueMediaEntity, true);
    }

    @Override
    public void onRecyclerViewHolderLongClick(Bundle bundle) {

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
            int remainingItems = totalItemCount - currentPosition;
            if (dy > 0 && remainingItems < visibleItemCount) {
                /*viewModel.getMoreData();*/
            }
        }
    };
}