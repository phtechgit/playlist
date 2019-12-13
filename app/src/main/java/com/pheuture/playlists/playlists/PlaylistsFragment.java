package com.pheuture.playlists.playlists;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.pheuture.playlists.R;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.databinding.FragmentMyPlaylistsBinding;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.playlists.detail.PlaylistDetailFragment;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.KeyboardUtils;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.SimpleDividerItemDecoration;

import java.util.List;

public class PlaylistsFragment extends BaseFragment implements TextWatcher, RecyclerViewInterface {
    private static final String TAG = PlaylistsFragment.class.getSimpleName();
    private FragmentActivity activity;
    private PlaylistsViewModel viewModel;
    private FragmentMyPlaylistsBinding binding;
    private PlaylistsRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_playlists, container, false);
        viewModel = ViewModelProviders.of(this).get(PlaylistsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        /*binding.layoutSearchBar.editTextSearch.setText(viewModel.getSearchQuery().getValue());
        binding.layoutSearchBar.editTextSearch.setSelection(binding.layoutSearchBar.editTextSearch.getText().length());*/

        recyclerAdapter = new PlaylistsRecyclerAdapter(this);
        layoutManager = new LinearLayoutManager(activity);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);
        /*binding.recyclerView.addItemDecoration(
                new SimpleDividerItemDecoration(getResources().getDrawable(R.drawable.line_divider),
                        0, 0));*/

        viewModel.getPlaylists().observe(this, new Observer<List<PlaylistEntity>>() {
            @Override
            public void onChanged(List<PlaylistEntity> playlistEntities) {
                viewModel.setProgressStatus(false);

                if (playlistEntities.size()>0) {
                    viewModel.setShouldShowPlaylists(true);
                }

                if (viewModel.getShouldShowPlaylists()){
                    binding.linearLayoutCreatePlaylist.setVisibility(View.GONE);
                    binding.relativeLayoutPlaylists.setVisibility(View.VISIBLE);

                } else {
                    binding.linearLayoutCreatePlaylist.setVisibility(View.VISIBLE);
                    binding.relativeLayoutPlaylists.setVisibility(View.GONE);
                }
                recyclerAdapter.setData(playlistEntities);
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
        binding.buttonCreatePlaylist.setOnClickListener(this);
        binding.recyclerView.addOnScrollListener(scrollListener);
        binding.layoutSearchBar.editTextSearch.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.buttonCreatePlaylist)){
            showCreatePlaylistNameDialog();
        }
    }

    private void showCreatePlaylistNameDialog() {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.setContentView(R.layout.layout_create_playlist_name);

        TextInputLayout textInputLayoutPlaylistName = dialog.findViewById(R.id.textView_playlist_name);
        textInputLayoutPlaylistName.requestFocus();
        Button buttonCancel = dialog.findViewById(R.id.button_cancel);
        Button buttonCreate = dialog.findViewById(R.id.button_create);

        buttonCancel.setOnClickListener(view -> dialog.cancel());
        buttonCreate.setOnClickListener(view -> {
            if (TextUtils.getTrimmedLength(textInputLayoutPlaylistName.getEditText().getText().toString()) == 0) {
                textInputLayoutPlaylistName.setError("This field is mandatory");
                textInputLayoutPlaylistName.requestFocus();

            } else {
                dialog.dismiss();
                KeyboardUtils.hideKeyboard(activity);
                viewModel.createPlaylist(textInputLayoutPlaylistName.getEditText().getText().toString());
            }
        });
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

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {
        int position = bundle.getInt(ARG_PARAM1, -1);
        PlaylistEntity model = bundle.getParcelable(ARG_PARAM2);

        assert model != null;
        if (position == 0){
            showCreatePlaylistNameDialog();

        } else {
            bundle.clear();
            bundle.putParcelable(ARG_PARAM1, model);

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_playlists_to_navigation_playlists_detail, bundle);
        }

    }

    @Override
    public void onRecyclerViewItemLongClick(Bundle bundle) {

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
}