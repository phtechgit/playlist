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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.pheuture.playlists.MainActivity;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentPlaylistDetailBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.datasource.local.playlist_handler.playlist_media_handler.PlaylistMediaEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.Constants;
import com.pheuture.playlists.utils.KeyboardUtils;
import com.pheuture.playlists.utils.SharedPrefsUtils;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist_detail, container, false);
        viewModel = ViewModelProviders.of(this, new PlaylistDetailViewModelFactory(
                activity.getApplication(), getArguments().getLong(ARG_PARAM1))).get(PlaylistDetailViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        ((MainActivity) activity).setupToolbar(false, "");

        viewModel.getPlaylistEntity().observe(this, new Observer<PlaylistEntity>() {
            @Override
            public void onChanged(PlaylistEntity playlistEntity) {
                playlist = playlistEntity;
                binding.setModel(playlist);
            }
        });

        layoutManager = new LinearLayoutManager(activity);
        recyclerAdapter = new PlaylistVideosRecyclerAdapter(this);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);

        viewModel.getPlaylistMediaEntitiesMutableLiveData().observe(this, new Observer<List<PlaylistMediaEntity>>() {
            @Override
            public void onChanged(List<PlaylistMediaEntity> newPalylistMediaEntities) {
                playlistMediaEntities = newPalylistMediaEntities;
                recyclerAdapter.setData(playlistMediaEntities);

                //show/hide play pause button
                if (newPalylistMediaEntities.size()>0){
                    binding.imageButtonPlay.setImageResource(R.drawable.ic_play_circular_white);
                    if (newPalylistMediaEntities.size()>2) {
                        binding.imageButtonShuffle.setImageResource(R.drawable.ic_shuffle_light);
                    } else {
                        binding.imageButtonShuffle.setImageResource(R.drawable.ic_shuffle_grey);
                    }
                } else {
                    binding.imageButtonPlay.setImageResource(R.drawable.ic_play_circular_grey);
                    binding.imageButtonShuffle.setImageResource(R.drawable.ic_shuffle_grey);
                }

                boolean downloadPlaylistMedia = SharedPrefsUtils.getBooleanPreference(activity,
                        Constants.DOWNLOAD_PLAYLIST_MEDIA, false);

                if (downloadPlaylistMedia) {
                    viewModel.addToOfflineMedia(playlistMediaEntities);
                }
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
            bundle.putString("title", playlist.getPlaylistName());

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_navigation_playlist_detail_to_navigation_media, bundle);

        } else if (v.equals(binding.imageButtonPlay)) {
            if (playlistMediaEntities.size()>0) {
                ((MainActivity) activity).setMedia(playlist, playlistMediaEntities, RecyclerView.NO_POSITION);
            }

        } else if (v.equals(binding.imageButtonShuffle)) {
            if (playlistMediaEntities.size()>2) {
                ((MainActivity) activity).toggleShuffleMode();
            }
        }
    }

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {
        int position = bundle.getInt(ARG_PARAM1, -1);
        int type = bundle.getInt(ARG_PARAM2, -1);
        PlaylistMediaEntity model = bundle.getParcelable(ARG_PARAM3);

        assert model != null;
        if (type == 1){
            ((MainActivity) activity).setMedia(playlist, playlistMediaEntities, (position-1));

        } else if (type == 2){
            showRemoveMediaFromPlaylistAlert(model);
        }
    }

    private void showRemoveMediaFromPlaylistAlert(PlaylistMediaEntity model) {
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

        textViewTitle.setText("Are you sure?");
        textViewSubtitle.setText("Do you want to remove " + model.getMediaTitle() + " from the playlist?");
        textViewSubtitle.setVisibility(View.VISIBLE);
        textViewRight.setText("Remove");

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
            ((MainActivity) activity).showSnack("removed from " + playlist.getPlaylistName());
            viewModel.removeMediaFromPlaylist(model);
        });
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
