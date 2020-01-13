package com.pheuture.playlists.playlist;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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
import com.pheuture.playlists.MainActivityViewModel;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.FragmentPlaylistBinding;
import com.pheuture.playlists.datasource.local.playlist_handler.PlaylistEntity;
import com.pheuture.playlists.interfaces.RecyclerViewInterface;
import com.pheuture.playlists.utils.BaseFragment;
import com.pheuture.playlists.utils.EditTextInputFilter;
import com.pheuture.playlists.utils.KeyboardUtils;
import com.pheuture.playlists.utils.Logger;
import com.pheuture.playlists.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaylistFragment extends BaseFragment implements TextWatcher, RecyclerViewInterface {
    private static final String TAG = PlaylistFragment.class.getSimpleName();
    private FragmentActivity activity;
    private MainActivityViewModel parentViewModel;
    private PlaylistViewModel viewModel;
    private FragmentPlaylistBinding binding;
    private PlaylistsRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager layoutManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false);
        parentViewModel = ViewModelProviders.of(activity).get(MainActivityViewModel.class);
        viewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {
        parentViewModel.setTitle("Playlists");

        binding.layoutSearchBar.editTextSearch.setHint("Find in playlist");

        recyclerAdapter = new PlaylistsRecyclerAdapter(this);
        layoutManager = new LinearLayoutManager(activity);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(recyclerAdapter);

        viewModel.getPlaylistEntitiesMutableLiveData().observe(this, new Observer<List<PlaylistEntity>>() {
            @Override
            public void onChanged(List<PlaylistEntity> playlistEntities) {
                if (StringUtils.isEmpty(viewModel.getSearchQuery()) && playlistEntities.size()==0){
                    /*((MainActivity) activity).updateActionBarStatus(false);*/
                    binding.linearLayoutCreatePlaylist.setVisibility(View.VISIBLE);
                    binding.relativeLayoutPlaylists.setVisibility(View.GONE);

                } else {
                    /*((MainActivity) activity).updateActionBarStatus(true);*/
                    binding.linearLayoutCreatePlaylist.setVisibility(View.GONE);
                    binding.relativeLayoutPlaylists.setVisibility(View.VISIBLE);
                }

                recyclerAdapter.setData(playlistEntities);
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
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.layout_create_playlist);
        dialog.show();

        TextView textViewTitle = dialog.findViewById(R.id.textView_title);
        TextView textViewSubtitle = dialog.findViewById(R.id.textView_subtitle);
        EditText editText = dialog.findViewById(R.id.ediText);
        TextView textViewLeft = dialog.findViewById(R.id.textView_left);
        TextView textViewRight = dialog.findViewById(R.id.textView_right);

        textViewTitle.setText("Give your playlist a name");

        editText.setFilters(new InputFilter[]{new EditTextInputFilter()});
        editText.setVisibility(View.VISIBLE);
        textViewRight.setText("Create");

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
            if (TextUtils.getTrimmedLength(editText.getText().toString()) == 0) {
                editText.setError("This field is mandatory");
                editText.requestFocus();

            } else {
                if (viewModel.isExistingPlaylist(editText.getText().toString())) {
                    Toast.makeText(activity, "Playlist already present with same name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();

                long playlistID = viewModel.createPlaylist(editText.getText().toString());

                //go to playlist detail page
                Bundle bundle = new Bundle();
                bundle.putLong(ARG_PARAM1, playlistID);

                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_navigation_playlist_to_navigation_playlist_detail, bundle);

            }
        });

        editText.requestFocus();
        KeyboardUtils.showKeyboard(activity, editText);
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

    private void showDeletePlaylistDialog(int position, PlaylistEntity model) {
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
        textViewSubtitle.setText("Do you want to remove this playlist containing " + model.getSongsCount() + " songs?");
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
            viewModel.deletePlaylist(position, model);
        });
        /*KeyboardUtils.showKeyboard(activity, editTextPlaylistName);*/
    }

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {
        int position = bundle.getInt(ARG_PARAM1, -1);
        int type = bundle.getInt(ARG_PARAM2, -1);
        PlaylistEntity model = bundle.getParcelable(ARG_PARAM3);

        assert model != null;
        if (type == 1) {
            if (model.getPlaylistID() == RecyclerView.NO_ID){
                showCreatePlaylistNameDialog();

            } else {
                bundle.clear();
                bundle.putLong(ARG_PARAM1, model.getPlaylistID());

                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_navigation_playlist_to_navigation_playlist_detail, bundle);
            }
        } else {
            showDeletePlaylistDialog(position, model);
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