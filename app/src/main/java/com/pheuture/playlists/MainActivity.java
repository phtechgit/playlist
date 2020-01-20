package com.pheuture.playlists;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.pheuture.playlists.databinding.ActivityMainBinding;
import com.pheuture.playlists.datasource.local.media_handler.queue.QueueMediaEntity;
import com.pheuture.playlists.base.BaseActivity;
import com.pheuture.playlists.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.queue.MediaQueueRecyclerAdapter;
import com.pheuture.playlists.receiver.ConnectivityChangeReceiver;
import com.pheuture.playlists.utils.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import static androidx.navigation.Navigation.findNavController;

public class MainActivity extends BaseActivity implements NavController.OnDestinationChangedListener,
        RecyclerViewClickListener,
        MediaQueueRecyclerAdapter.ClickType,
        ConnectivityChangeReceiver.ConnectivityChangeListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;
    private BottomSheetBehavior bottomSheetBehavior;
    private MediaQueueRecyclerAdapter recyclerAdapter;
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.master_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupConnectivityChangeBroadcastReceiver() {
        connectivityChangeReceiver = new ConnectivityChangeReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        registerReceiver(connectivityChangeReceiver, intentFilter);
    }

    @Override
    public void initializations() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        setSupportActionBar(binding.layoutAppBar.toolbar);

        setupConnectivityChangeBroadcastReceiver();

        proceedWithPermissions(null, true);

        bottomSheetBehavior = BottomSheetBehavior.from( binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer);
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_playlists, R.id.navigation_trending, R.id.navigation_settings)
                .build();

        NavController navController = findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        navController.addOnDestinationChangedListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerAdapter = new MediaQueueRecyclerAdapter(this, this);

        binding.layoutBottomSheet.recyclerViewMediaQueue.setLayoutManager(layoutManager);
        binding.layoutBottomSheet.recyclerViewMediaQueue.setAdapter(recyclerAdapter);

        viewModel.getTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                setTitle(s);
            }
        });

        viewModel.getExoPlayer().observe(this, new Observer<SimpleExoPlayer>() {
            @Override
            public void onChanged(SimpleExoPlayer exoPlayer) {
                binding.layoutBottomSheet.playerView.setPlayer(exoPlayer);
            }
        });

        viewModel.getQueueMediaEntities().observe(this, new Observer<List<QueueMediaEntity>>() {
            @Override
            public void onChanged(List<QueueMediaEntity> queueMediaEntities) {
                Logger.e(TAG,"QueueMediaEntities size:" + queueMediaEntities.size());
                Logger.e(TAG,"QueueMediaEntitiesLiveData size:" + viewModel.getQueueMediaEntities().getValue().size());
                recyclerAdapter.setData(queueMediaEntities);
            }
        });

        viewModel.getCurrentlyPlayingQueueMedia().observe(this, new Observer<QueueMediaEntity>() {
            @Override
            public void onChanged(QueueMediaEntity currentlyPlayingQueueMediaEntity) {
                //set media info
                binding.layoutBottomSheet.textViewTitle.setText(currentlyPlayingQueueMediaEntity.getMediaTitle());
                binding.layoutBottomSheet.textViewCreator.setText(currentlyPlayingQueueMediaEntity.getMediaDescription());

                //show bottom sheet
                binding.layoutBottomSheet.constraintLayoutBottomSheetPlayer.setVisibility(View.VISIBLE);
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        viewModel.getPlayingMediaProgress().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer progress) {
                binding.layoutBottomSheet.progressBar.setProgress(progress);
            }
        });

        viewModel.getPlayBackState().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                boolean playWhenReady = bundle.getBoolean(ARG_PARAM1);
                int playBackState = bundle.getInt(ARG_PARAM2);
                checkPlayBackState(playWhenReady, playBackState);
            }
        });

        viewModel.shouldShowNextButton().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                Logger.e(TAG, "showNext:" + show);
                //if more media available to play
                if (show) {
                    binding.layoutBottomSheet.imageViewNext.setImageResource(R.drawable.ic_next_light);
                } else {
                    binding.layoutBottomSheet.imageViewNext.setImageResource(R.drawable.ic_next_grey);
                }
            }
        });

        viewModel.getSnackBar().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                if (bundle.getBoolean(SNACK_BAR_SHOW, false)){
                    showSnack(binding.coordinatorLayout, bundle);
                } else {
                    hideSnack();
                }
            }
        });
    }

    private void checkPlayBackState(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                Logger.e(TAG, "state_buffering");
                binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                binding.layoutBottomSheet.progressBuffering.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                Logger.e(TAG, "state_ended");
                binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                break;
            case Player.STATE_READY:
                if (playWhenReady) {
                    Logger.e(TAG, "state_ready_playing");
                    // media actually playing
                    binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                } else {
                    Logger.e(TAG, "state_ready_paused");
                    // player paused in any state
                    binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                }
                break;
            case Player.STATE_IDLE:
                Logger.e(TAG, "state_idle");
                binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                break;
            default:
                break;
        }
    }

    @Override
    public void setListeners() {
        binding.layoutBottomSheet.imageViewTogglePlay.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewNext.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.layoutBottomSheet.imageViewTogglePlay)){
            viewModel.togglePlay();

        } else if (v.equals(binding.layoutBottomSheet.imageViewNext)){
            viewModel.next();

        } else if (v.equals(binding.layoutBottomSheet.imageViewClose)){
            viewModel.dismissPlayer();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            binding.bottomNavView.setVisibility(View.VISIBLE);
        }
    }

    private BottomSheetCallback  bottomSheetCallback = new BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            viewModel.setBottomSheetState(newState);
            switch (newState) {
                case BottomSheetBehavior.STATE_HIDDEN:
                    viewModel.resetAllPlayers();
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    break;
                case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    /*bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);*/
                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    break;
                case BottomSheetBehavior.STATE_DRAGGING:
                    break;
                case BottomSheetBehavior.STATE_SETTLING:
                    /*bottomSheetBehavior.setHideable(false);*/
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View view, float v) {
            Logger.e(TAG, v + "");
            if (v <= 0){
                binding.bottomNavView.setVisibility(View.VISIBLE);
            } else {
                binding.bottomNavView.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onRecyclerViewItemClick(Bundle bundle) {
        int position = bundle.getInt(ARG_PARAM1, -1);
        int type = bundle.getInt(ARG_PARAM2, 1);
        QueueMediaEntity queueMediaEntity = bundle.getParcelable(ARG_PARAM3);

        if (type == SELECT) {
            viewModel.setMedia(viewModel.getPlaylistMutableLiveData().getValue(), queueMediaEntity, false);

        } else if (type == REMOVE){
            viewModel.removeQueueMedia(queueMediaEntity);
        }
    }

    @Override
    public void onRecyclerViewItemLongClick(Bundle bundle) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        proceedWithPermissions(null, true);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN
                && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectivityChangeReceiver);
    }

    @Override
    public void onConnectivityChange(boolean connected) {
        Logger.e(TAG, "onConnectivityChange");
        viewModel.setNetworkStatus(connected);
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }
}
