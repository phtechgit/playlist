package com.pheuture.playlists.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public final class RecyclerItemMoveCallback extends ItemTouchHelper.Callback {
    private ItemTouchHelperContract itemTouchHelperContract;

    public RecyclerItemMoveCallback(ItemTouchHelperContract itemTouchHelperContract) {
        this.itemTouchHelperContract = itemTouchHelperContract;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition =  target.getAdapterPosition();

        if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION){
            return false;
        }

        if (fromPosition == toPosition){
            return false;
        }
        itemTouchHelperContract.onRecyclerViewHolderMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            if (viewHolder != null) {
                viewHolder.itemView.setAlpha(0.5f);
            }
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setAlpha(1.0f);
    }

    public interface ItemTouchHelperContract {
        void onRecyclerViewHolderMoved(int fromPosition, int toPosition);
    }
}
