package com.pheuture.playlists.my_playlists;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputLayout;
import com.pheuture.playlists.R;
import com.pheuture.playlists.videos.VideosActivity;
import com.pheuture.playlists.databinding.FragmentMyPlaylistsBinding;
import com.pheuture.playlists.utils.BaseFragment;

public class MyPlaylistsFragment extends BaseFragment {
    private FragmentActivity activity;
    private MyPlaylistsViewModel viewModel;
    private FragmentMyPlaylistsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View myFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_playlists, container, false);
        viewModel = ViewModelProviders.of(this).get(MyPlaylistsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void initializations() {

    }

    @Override
    public void handleListeners() {
        binding.buttonCreatePlaylist.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == binding.buttonCreatePlaylist.getId()){
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
                /*showProgress(binding.progressLayout.progressFullscreen, true);
                viewModel.createPlaylist(textInputLayoutPlaylistName.getEditText().getText().toString());*/
                Intent intent = new Intent(activity, VideosActivity.class);
                intent.putExtra(ARG_PARAM1, "playListID");
                startActivity(intent);
            }
        });
    }
}