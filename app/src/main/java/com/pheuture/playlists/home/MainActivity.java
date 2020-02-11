package com.pheuture.playlists.home;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.SeekBar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.pheuture.playlists.R;
import com.pheuture.playlists.databinding.ActivityMainBinding;
import com.pheuture.playlists.queue.QueueMediaEntity;
import com.pheuture.playlists.base.BaseActivity;
import com.pheuture.playlists.base.interfaces.RecyclerViewClickListener;
import com.pheuture.playlists.queue.MediaQueueRecyclerAdapter;
import com.pheuture.playlists.base.receiver.ConnectivityChangeReceiver;
import com.pheuture.playlists.base.utils.KeyboardUtils;
import com.pheuture.playlists.base.utils.RecyclerItemMoveCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import static androidx.navigation.Navigation.findNavController;

public class MainActivity extends BaseActivity implements NavController.OnDestinationChangedListener,
        RecyclerViewClickListener, RecyclerItemMoveCallback.ItemTouchHelperContract,
        ConnectivityChangeReceiver.ConnectivityChangeListener, SeekBar.OnSeekBarChangeListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;
    private BottomSheetBehavior bottomSheetBehavior;
    private MediaQueueRecyclerAdapter recyclerAdapter;
    private ConnectivityChangeReceiver connectivityChangeReceiver;
    private ItemTouchHelper itemTouchHelper;
    private List<QueueMediaEntity> queueMediaEntities;

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
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        setSupportActionBar(binding.layoutAppBar.toolbar);

        Runnable runnable = this::initiate;

        if (savedInstanceState == null){
            //Check permissions first time
            proceedWithPermissions(REQUEST_CODE_GRANT_PERMISSIONS,
                    READ_WRITE_EXTERNAL_STORAGE_PERMISSION, runnable, true);

        } else {
            initiate();
        }
    }

    private void initiate() {
        setupConnectivityChangeBroadcastReceiver();

        setUpBottomNavigation();

        setUpBottomSheet();

        setUpRecycler();

        viewModel.getQueueMediaEntities().observe(this, new Observer<List<QueueMediaEntity>>() {
            @Override
            public void onChanged(List<QueueMediaEntity> newQueueMediaEntities) {
                //Update Data.
                queueMediaEntities = newQueueMediaEntities;
                recyclerAdapter.setData(queueMediaEntities);
            }
        });

        viewModel.getViewStatesLive().observe(this, new Observer<MainViewStates>() {
            @Override
            public void onChanged(MainViewStates viewStates) {
                //Update Views.
                setTitle(viewStates.getTitle());

                binding.bottomNavView.setVisibility(viewStates.getBottomNavigationViewVisibility());

                binding.layoutBottomSheet.constraintLayoutBottomSheet.setVisibility(viewStates.getBottomSheetVisibility());
                bottomSheetBehavior.setState(viewStates.getBottomSheetState());

                binding.layoutBottomSheet.playerView.setPlayer(viewStates.getExoPlayer());

                binding.layoutBottomSheet.progressBuffering.setVisibility(viewStates.getBufferVisibility());

                binding.layoutBottomSheet.imageViewTogglePlay.setImageResource(viewStates.getTogglePlayButtonImageResource());
                binding.layoutBottomSheet.progressBar.setMax((int) viewStates.getMaxProgress());
                binding.layoutBottomSheet.progressBar.setProgress((int) viewStates.getProgress());

                binding.layoutBottomSheet.textViewTitle.setText(viewStates.getCurrentlyPlayingMediaTitle());
                binding.layoutBottomSheet.textViewCreator.setText(viewStates.getCurrentlyPLayingMediaCreator());

                binding.layoutBottomSheet.imageViewPrevious.setImageResource(viewStates.getPreviousButtonImageResource());
                binding.layoutBottomSheet.imageViewPrevious.setEnabled(viewStates.isPreviousEnabled());

                binding.layoutBottomSheet.imageViewNext.setImageResource(viewStates.getNextButtonImageResource());
                binding.layoutBottomSheet.imageViewNext.setEnabled(viewStates.isNextEnabled());

                binding.layoutBottomSheet.imageViewShuffle.setImageResource(viewStates.getShuffleButtonImageResource());
                binding.layoutBottomSheet.imageViewShuffle.setEnabled(viewStates.isShuffleEnabled());

                binding.layoutBottomSheet.imageViewRepeat.setImageResource(viewStates.getRepeatButtonImageResource());
                binding.layoutBottomSheet.imageViewRepeat.setEnabled(viewStates.isRepeatEnabled());

                setError(binding.coordinatorLayoutSnackBar, viewStates.getError());
            }
        });
    }

    private void setUpBottomNavigation() {
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_playlists, R.id.navigation_trending, R.id.navigation_settings).build();

        NavController navController = findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        navController.addOnDestinationChangedListener(this);
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from( binding.layoutBottomSheet.constraintLayoutBottomSheet);
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setUpRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerAdapter = new MediaQueueRecyclerAdapter(this, this);
        binding.layoutBottomSheet.recyclerViewMediaQueue.setLayoutManager(layoutManager);
        binding.layoutBottomSheet.recyclerViewMediaQueue.setAdapter(recyclerAdapter);

        //enable drag listener
        RecyclerItemMoveCallback itemMoveCallback = new RecyclerItemMoveCallback(this);
        itemTouchHelper = new ItemTouchHelper(itemMoveCallback);
        itemTouchHelper.attachToRecyclerView(binding.layoutBottomSheet.recyclerViewMediaQueue);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            viewModel.closeBottomSheet();

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
        }

        @Override
        public void onSlide(@NonNull View view, float slideOffset) {
            animateBottomNavigationView(slideOffset);
        }
    };

    private void animateBottomNavigationView(float slideOffset) {
        if (slideOffset>=0) {
            float height_to_animate = slideOffset * binding.bottomNavView.getHeight();
            ViewPropertyAnimator animator = binding.bottomNavView.animate();
            animator.translationY(height_to_animate)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(0)
                    .start();
        }
    }

    @Override
    public void onConnectivityChange(boolean connected) {
        viewModel.setNetworkStatus(connected);
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED){
            viewModel.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        KeyboardUtils.hideKeyboard(this, binding.getRoot());
    }

    @Override
    public void onRecyclerViewHolderMoved(int fromPosition, int toPosition) {
        viewModel.moveQueueMedia(fromPosition, toPosition);
    }

    @Override
    public void onRecyclerViewHolderClick(RecyclerView.ViewHolder viewHolder, Bundle bundle) {
        int position = bundle.getInt(ARG_PARAM1, -1);
        int clickType = bundle.getInt(ARG_PARAM2, 1);
        QueueMediaEntity queueMediaEntity = bundle.getParcelable(ARG_PARAM3);

        if (clickType == SELECT) {
            viewModel.setMediaListToQueue(queueMediaEntities, position);

        } else if (clickType == REMOVE){
            viewModel.removeQueueMedia(position);
        } else if (clickType == DRAG) {
            itemTouchHelper.startDrag(viewHolder);
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

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            viewModel.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        KeyboardUtils.hideKeyboard(this, binding.getRoot());
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectivityChangeReceiver);
    }

    @Override
    public void finish() {
        super.finish();
        ActivityNavigator.applyPopAnimationsToPendingTransition(this);
    }
}
