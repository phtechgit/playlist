package com.pheuture.playlists.trending;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentTrendingsBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.datasource.local.video_handler.MediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.ContentProvider;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.ParserUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.pheuture.playlists.utils.RequestCodeConstant.REQUEST_CODE_FILE_SELECT;

public class TrendingFragment extends BaseFragment implements TextWatcher, RecyclerViewInterface {
    private static final String TAG = TrendingFragment.class.getSimpleName();
    private FragmentActivity activity;
    private FragmentTrendingsBinding binding;
    private TrendingViewModel viewModel;
    private TrendingRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_upload).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_upload) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("video/*");
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_FILE_SELECT);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trendings, container, false);
        viewModel = ViewModelProviders.of(this).get(TrendingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        ((MainActivity) activity).setupToolbar(false, "Trending");
        binding.layoutSearchBar.editTextSearch.setHint("Find in trending");
        /*binding.layoutSearchBar.editTextSearch.setText(viewModel.getSearchQuery().getValue());
        binding.layoutSearchBar.editTextSearch.setSelection(binding.layoutSearchBar.editTextSearch.getText().length());*/

        recyclerAdapter = new TrendingRecyclerAdapter(this);
        layoutManager = new LinearLayoutManager(activity);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);

        viewModel.getVideosLive().observe(this, new Observer<List<MediaEntity>>() {
            @Override
            public void onChanged(List<MediaEntity> videoEntities) {
                recyclerAdapter.setData(videoEntities);
            }
        });

        viewModel.getSearchQuery().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                viewModel.getFreshData();
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
            int remainingItems = totalItemCount - currentPosition;
            if (dy > 0 && remainingItems < visibleItemCount) {
                viewModel.getMoreData();
            }
        }
    };

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {
        MediaEntity mediaEntity = bundle.getParcelable(ARG_PARAM2);

        String objectJsonString = ParserUtil.getInstance().toJson(mediaEntity,
                MediaEntity.class);
        PlaylistMediaEntity playlistMediaEntity = ParserUtil.getInstance()
                .fromJson(objectJsonString, PlaylistMediaEntity.class);

        List<PlaylistMediaEntity> mediaEntities = new ArrayList<>();
        mediaEntities.add(playlistMediaEntity);

        ((MainActivity) activity).setMedia(null, mediaEntities);
    }

    @Override
    public void onRecyclerViewItemLongClick(Bundle bundle) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_FILE_SELECT && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri mediaUri = resultData.getData();
                if (mediaUri != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(ARG_PARAM1, mediaUri);

                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.action_navigation_trending_to_navigation_uploads, bundle);
                }
            }
        }
    }

}