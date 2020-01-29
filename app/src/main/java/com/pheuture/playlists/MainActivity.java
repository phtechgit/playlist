package com.pheuture.playlists;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.SeekBar;

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
import com.pheuture.playlists.utils.RecyclerItemMoveCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import static androidx.navigation.Navigation.findNavController;
import static com.pheuture.playlists.constants.Constants.PlayerRepeatModes.REPEAT_MODE_ALL;
import static com.pheuture.playlists.constants.Constants.PlayerRepeatModes.REPEAT_MODE_OFF;
import static com.pheuture.playlists.constants.Constants.PlayerRepeatModes.REPEAT_MODE_ONE;

public class MainActivity extends BaseActivity implements NavController.OnDestinationChangedListener,
        RecyclerViewClickListener, MediaQueueRecyclerAdapter.ClickType,
        RecyclerItemMoveCallback.ItemTouchHelperContract,
        ConnectivityChangeReceiver.ConnectivityChangeListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;
    private BottomSheetBehavior bottomSheetBehavior;
    private MediaQueueRecyclerAdapter recyclerAdapter;
    private ConnectivityChangeReceiver connectivityChangeReceiver;
    private ItemTouchHelper itemTouchHelper;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        setSupportActionBar(binding.layoutAppBar.toolbar);

        setupConnectivityChangeBroadcastReceiver();

        proceedWithPermissions(REQUEST_CODE_GRANT_PERMISSIONS,READ_WRITE_EXTERNAL_STORAGE_PERMISSION, null, true);

        bottomSheetBehavior = BottomSheetBehavior.from( binding.layoutBottomSheet.constraintLayoutBottomSheet);
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

        //enable drag listener
        RecyclerItemMoveCallback itemMoveCallback = new RecyclerItemMoveCallback(this);
        itemTouchHelper = new ItemTouchHelper(itemMoveCallback);
        itemTouchHelper.attachToRecyclerView(binding.layoutBottomSheet.recyclerViewMediaQueue);

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

        viewModel.getCurrentlyPlayingQueueMedia().observe(this, new Observer<QueueMediaEntity>() {
            @Override
            public void onChanged(QueueMediaEntity currentlyPlayingQueueMediaEntity) {
                //set media info
                binding.layoutBottomSheet.textViewTitle.setText(currentlyPlayingQueueMediaEntity.getMediaTitle());
                binding.layoutBottomSheet.textViewCreator.setText(currentlyPlayingQueueMediaEntity.getMovieName());

                //show bottom sheet
                binding.layoutBottomSheet.constraintLayoutBottomSheet.setVisibility(View.VISIBLE);
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        viewModel.getQueueMediaEntities().observe(this, new Observer<List<QueueMediaEntity>>() {
            @Override
            public void onChanged(List<QueueMediaEntity> queueMediaEntities) {
                recyclerAdapter.updateData(queueMediaEntities);

                //if more media available to play
                if (viewModel.nextMediaAvailable()) {
                    binding.layoutBottomSheet.imageViewNext.setImageResource(R.drawable.ic_next_light);
                } else {
                    binding.layoutBottomSheet.imageViewNext.setImageResource(R.drawable.ic_next_grey);
                }

                //if can shuffle
                if (viewModel.canShuffle()){
                    binding.layoutBottomSheet.imageViewShuffle.setImageResource(R.drawable.exo_controls_shuffle_on);
                } else {
                    binding.layoutBottomSheet.imageViewShuffle.setImageResource(R.drawable.exo_controls_shuffle_off);
                }

                if (viewModel.previousMediaAvailable()) {
                    binding.layoutBottomSheet.imageViewPrevious.setImageResource(R.drawable.ic_previous_light);
                } else {
                    binding.layoutBottomSheet.imageViewPrevious.setImageResource(R.drawable.ic_previous_dark);
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

        viewModel.getRepeatMode().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer repeatMode) {
                if (repeatMode == REPEAT_MODE_OFF) {
                    binding.layoutBottomSheet.imageViewRepeat.setImageDrawable(getResources().getDrawable(R.drawable.exo_controls_repeat_off));
                } else if (repeatMode == REPEAT_MODE_ONE){
                    binding.layoutBottomSheet.imageViewRepeat.setImageDrawable(getResources().getDrawable(R.drawable.exo_controls_repeat_one ));
                } else if (repeatMode == REPEAT_MODE_ALL){
                    binding.layoutBottomSheet.imageViewRepeat.setImageDrawable(getResources().getDrawable(R.drawable.exo_controls_repeat_all));
                }
            }
        });

        viewModel.getSnackBar().observe(this, new Observer<Bundle>() {
            @Override
            public void onChanged(Bundle bundle) {
                if (bundle.getBoolean(SNACK_BAR_SHOW, false)){
                    showSnack(binding.coordinatorLayoutSnackBar, bundle);
                } else {
                    hideSnack();
                }
            }
        });

        viewModel.getShowActionBar().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean show) {
                if (show){
                    binding.layoutAppBar.toolbar.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutAppBar.toolbar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void checkPlayBackState(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                Logger.e(TAG, "state_buffering");
                /*binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));*/
                binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                binding.layoutBottomSheet.progressBuffering.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                Logger.e(TAG, "state_ended");
                binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(R.drawable.exo_icon_play));
                break;
            case Player.STATE_READY:
                if (playWhenReady) {
                    Logger.e(TAG, "state_ready_playing");
                    // media actually playing
                    binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(R.drawable.exo_icon_pause));
                } else {
                    Logger.e(TAG, "state_ready_paused");
                    // player paused in any state
                    binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                    binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(R.drawable.exo_icon_play));
                }
                break;
            case Player.STATE_IDLE:
                Logger.e(TAG, "state_idle");
                binding.layoutBottomSheet.progressBuffering.setVisibility(View.GONE);
                binding.layoutBottomSheet.imageViewTogglePlay.setVisibility(View.VISIBLE);
                binding.layoutBottomSheet.imageViewTogglePlay.setImageDrawable(getResources().getDrawable(R.drawable.exo_icon_play));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.layoutBottomSheet.imageViewTogglePlay.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewNext.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewClose.setOnClickListener(this);
        binding.layoutBottomSheet.progressBar.setOnSeekBarChangeListener(this);
        binding.layoutBottomSheet.imageViewShuffle.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewPrevious.setOnClickListener(this);
        binding.layoutBottomSheet.imageViewRepeat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.layoutBottomSheet.imageViewTogglePlay)){
            viewModel.togglePlay();

        } else if (v.equals(binding.layoutBottomSheet.imageViewNext)){
            viewModel.next();

        } else if (v.equals(binding.layoutBottomSheet.imageViewClose)){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            binding.bottomNavView.setVisibility(View.VISIBLE);

        } else if (v.equals(binding.layoutBottomSheet.imageViewShuffle)){
            viewModel.shuffle();

        } else if (v.equals(binding.layoutBottomSheet.imageViewPrevious)){
            viewModel.previous();

        } else if (v.equals(binding.layoutBottomSheet.imageViewRepeat)){
            viewModel.toggleRepeatMode();
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
                    binding.layoutBottomSheet.progressBar.getThumb().setVisible(true, true);
                    break;
                case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    /*bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);*/
                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    binding.layoutBottomSheet.progressBar.getThumb().setVisible(false, true);
                    break;
                case BottomSheetBehavior.STATE_DRAGGING:
                    break;
                case BottomSheetBehavior.STATE_SETTLING:
                    /*bottomSheetBehavior.setHideable(false);*/
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View view, float slideOffset) {
            if (slideOffset>=0) {
                float height_to_animate = slideOffset * binding.bottomNavView.getHeight();
                ViewPropertyAnimator animator = binding.bottomNavView.animate();
                animator.translationY(height_to_animate)
                        .setInterpolator(new DecelerateInterpolator())
                        .setDuration(0)
                        .start();
            }
        }
    };

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

    @Override
    public void onRecyclerViewHolderMoved(int fromPosition, int toPosition) {
        viewModel.moveQueueMedia(fromPosition, toPosition);
    }

    @Override
    public void onRecyclerViewHolderClick(Bundle bundle) {
        int position = bundle.getInt(ARG_PARAM1, -1);
        int clickType = bundle.getInt(ARG_PARAM2, 1);
        QueueMediaEntity queueMediaEntity = bundle.getParcelable(ARG_PARAM3);

        if (clickType == SELECT) {
            viewModel.setMedia(viewModel.getPlaylistMutableLiveData().getValue(), queueMediaEntity, false);

        } else if (clickType == REMOVE){
            if (queueMediaEntity != null) {
                viewModel.removeQueueMedia(queueMediaEntity);
            }
        } else if (clickType == DRAG) {
            Logger.e(TAG, "drag started");
            itemTouchHelper.startDrag(binding.layoutBottomSheet.recyclerViewMediaQueue.findViewHolderForLayoutPosition(position));
        }
    }

    @Override
    public void onRecyclerViewHolderLongClick(Bundle bundle) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        proceedWithPermissions(REQUEST_CODE_GRANT_PERMISSIONS,READ_WRITE_EXTERNAL_STORAGE_PERMISSION, null, true);
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser){
            viewModel.seekPlayer(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
