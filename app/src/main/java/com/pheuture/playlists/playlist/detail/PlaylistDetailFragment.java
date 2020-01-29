package com.pheuture.playlists.playlist.detail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.pheuture.playlists.MainActivityViewModel;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentPlaylistDetailBinding;
import com.pheuture.playlists.datasource.local.media_handler.queue.QueueMediaEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.base.BaseFragment;
import com.pheuture.playlists.constants.Constants;
import com.pheuture.playlists.utils.KeyboardUtils;
import com.pheuture.playlists.utils.ParserUtil;
import com.pheuture.playlists.utils.SharedPrefsUtils;

import java.util.List;

public class PlaylistDetailFragment extends BaseFragment implements RecyclerViewClickListener {
    public static final String TAG = PlaylistDetailFragment.class.getSimpleName();
    private FragmentPlaylistDetailBinding binding;
    private MainActivityViewModel parentViewModel;
    private PlaylistDetailViewModel viewModel;
    private PlaylistMediaRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;
    private PlaylistEntity playlist;
    private List<PlaylistMediaEntity> playlistMediaEntities;
    private FragmentActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        activity = getActivity();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.menu_delete).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            showDeletePlaylistDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeletePlaylistDialog() {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.layout_create_playlist);
        dialog.show();

        TextView textViewTitle = dialog.findViewById(R.id.textView_title);
        TextView textViewSubtitle = dialog.findViewById(R.id.textView_subtitle);
        EditText editText = dialog.findViewById(R.id.ediText);
        TextView textViewLeft = dialog.findViewById(R.id.textView_left);
        TextView textViewRight = dialog.findViewById(R.id.textView_right);

        textViewTitle.setText(getResources().getString(R.string.are_you_sure));
        textViewSubtitle.setText(getResources().getString(R.string.do_you_want_to_remove_this_playlist_containing) + playlistMediaEntities.size() + " songs?");
        textViewSubtitle.setVisibility(View.VISIBLE);
        textViewRight.setText(getResources().getString(R.string.remove));

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                KeyboardUtils.hideKeyboard(activity, editText);
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                KeyboardUtils.hideKeyboard(activity, editText);
            }
        });

        textViewLeft.setOnClickListener(view -> {
            dialog.cancel();
        });

        textViewRight.setOnClickListener(view -> {
            dialog.dismiss();
            parentViewModel.showSnackBar(playlist.getPlaylistName() + " deleted.", Snackbar.LENGTH_SHORT);
            viewModel.deletePlaylist();
            activity.onBackPressed();
        });
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist_detail, container, false);
        parentViewModel = ViewModelProviders.of(activity).get(MainActivityViewModel.class);
        viewModel = ViewModelProviders.of(this, new PlaylistDetailViewModelFactory(
                activity.getApplication(), getArguments().getLong(ARG_PARAM1))).get(PlaylistDetailViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        parentViewModel.setTitle(" ");

        viewModel.getPlaylistEntity().observe(this, new Observer<PlaylistEntity>() {
            @Override
            public void onChanged(PlaylistEntity playlistEntity) {
                playlist = playlistEntity;
                binding.setModel(playlist);
            }
        });

        layoutManager = new LinearLayoutManager(activity);
        recyclerAdapter = new PlaylistMediaRecyclerAdapter(this);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);

        viewModel.getPlaylistMediaEntitiesMutableLiveData().observe(this, new Observer<List<PlaylistMediaEntity>>() {
            @Override
            public void onChanged(List<PlaylistMediaEntity> newPlaylistMediaEntities) {
                playlistMediaEntities = newPlaylistMediaEntities;
                recyclerAdapter.setData(playlistMediaEntities);

                //show/hide play pause button
                if (newPlaylistMediaEntities.size()>0){
                    binding.imageViewPlay.setImageResource(R.drawable.ic_play_circular_white);
                    if (newPlaylistMediaEntities.size()>2) {
                        binding.imageViewShuffle.setImageResource(R.drawable.ic_shuffle_light);
                    } else {
                        binding.imageViewShuffle.setImageResource(R.drawable.ic_shuffle_grey);
                    }

                    binding.linearLayoutEmpty.setVisibility(View.GONE);
                } else {
                    binding.imageViewPlay.setImageResource(R.drawable.ic_play_circular_grey);
                    binding.imageViewShuffle.setImageResource(R.drawable.ic_shuffle_grey);
                    binding.linearLayoutEmpty.setVisibility(View.VISIBLE);
                }

                boolean downloadPlaylistMedia = SharedPrefsUtils.getBooleanPreference(activity,
                        Constants.DOWNLOAD_PLAYLIST_MEDIA, DOWNLOAD_PLAYLIST_MEDIA_DEFAULT);

                if (downloadPlaylistMedia) {
                    viewModel.addToOfflineMedia(playlistMediaEntities);
                }
            }
        });

        parentViewModel.isNewMediaAddedToPlaylist().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean){
                    parentViewModel.setNewMediaAdded(false);
                    viewModel.getFreshData();
                }
            }
        });
    }

    @Override
    public void setListeners() {
        binding.imageViewPlay.setOnClickListener(this);
        binding.imageViewShuffle.setOnClickListener(this);
        binding.imageViewAddNewSong.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.imageViewAddNewSong)){
            Bundle bundle = new Bundle();
            bundle.putParcelable(ARG_PARAM1, playlist);
            bundle.putString("title", playlist.getPlaylistName());

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_playlist_detail_to_navigation_media, bundle);

        } else if (v.equals(binding.imageViewPlay)) {
            if (playlistMediaEntities.size()>0) {
                parentViewModel.setMedia(playlist, null, true);
            }

        } else if (v.equals(binding.imageViewShuffle)) {
            if (playlistMediaEntities.size()>2) {
                parentViewModel.setShuffledMedia(playlist);
            }
        }
    }

    @Override
    public void onRecyclerViewHolderClick(Bundle bundle) {
        int position = bundle.getInt(ARG_PARAM1, -1);
        int type = bundle.getInt(ARG_PARAM2, -1);
        PlaylistMediaEntity playlistMediaEntity = bundle.getParcelable(ARG_PARAM3);

        assert playlistMediaEntity != null;
        if (type == 1){
            String objectJsonString = ParserUtil.getInstance().toJson(playlistMediaEntity,
                    PlaylistMediaEntity.class);
            QueueMediaEntity queueMediaEntity = ParserUtil.getInstance()
                    .fromJson(objectJsonString, QueueMediaEntity.class);

            parentViewModel.setMedia(playlist, queueMediaEntity, true);

        } else if (type == 2){
            showRemoveMediaFromPlaylistAlert(position, playlistMediaEntity);
        }
    }

    private void showRemoveMediaFromPlaylistAlert(int position, PlaylistMediaEntity model) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.layout_create_playlist);
        dialog.show();

        TextView textViewTitle = dialog.findViewById(R.id.textView_title);
        TextView textViewSubtitle = dialog.findViewById(R.id.textView_subtitle);
        EditText editText = dialog.findViewById(R.id.ediText);
        TextView textViewLeft = dialog.findViewById(R.id.textView_left);
        TextView textViewRight = dialog.findViewById(R.id.textView_right);

        textViewTitle.setText(getResources().getString(R.string.are_you_sure));
        textViewSubtitle.setText("Do you want to remove " + model.getMediaTitle() + " from the playlist?");
        textViewSubtitle.setVisibility(View.VISIBLE);
        textViewRight.setText(getResources().getString(R.string.remove));

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                KeyboardUtils.hideKeyboard(activity, editText);
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                KeyboardUtils.hideKeyboard(activity, editText);
            }
        });

        textViewLeft.setOnClickListener(view -> {
            dialog.cancel();
        });

        textViewRight.setOnClickListener(view -> {
            dialog.dismiss();
            viewModel.removeMediaFromPlaylist(position, model);
        });
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
                viewModel.getMoreData();
            }
        }
    };
}
