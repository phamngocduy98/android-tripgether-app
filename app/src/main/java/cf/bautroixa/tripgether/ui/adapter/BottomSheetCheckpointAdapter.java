package cf.bautroixa.tripgether.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.SortedList;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.List;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Trip;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.tripgether.ui.adapter.viewholder.CheckpointVH;
import cf.bautroixa.tripgether.ui.dialogs.CheckpointEditDialogFragment;
import cf.bautroixa.tripgether.ui.map.TabMapFragment;
import cf.bautroixa.ui.OneRecyclerView;

public class BottomSheetCheckpointAdapter extends OneRecyclerView.Adapter<BottomSheetCheckpointAdapter.BottomSheetCheckpointVH> {
    SortedList<Checkpoint> checkpointSortedList;
    Fragment fragment;
    NavigationInterface navigationInterface;
    ModelManager manager;

    public BottomSheetCheckpointAdapter(Fragment fragment, NavigationInterface navigationInterface) {
        this.fragment = fragment;
        this.navigationInterface = navigationInterface;
        this.manager = ModelManager.getInstance(fragment.requireContext());
    }

    public void attachSortedList(SortedList<Checkpoint> checkpointSortedList) {
        this.checkpointSortedList = checkpointSortedList;
    }

    @NonNull
    @Override
    public BottomSheetCheckpointVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BottomSheetCheckpointVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_with_timeline, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BottomSheetCheckpointVH holder, int position) {
        holder.bind(checkpointSortedList.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull BottomSheetCheckpointVH holder, int position, @NonNull List<Object> payloads) {
        if (payloads.size() > 0) {
            holder.setActiveCheckpoint(!payloads.get(0).equals(Payload.UNSET_ACTIVE_CHECKPOINT));
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        return checkpointSortedList.size();
    }

    private void editCheckpoint(Checkpoint checkpoint) {
        CheckpointEditDialogFragment.newInstance(checkpoint, new CheckpointEditDialogFragment.OnCheckpointSetListener() {
            @Override
            public void onCheckpointSet(Checkpoint newCheckpoint) {
                checkpoint.sendUpdate(null,
                        Checkpoint.NAME, newCheckpoint.getName(),
                        Checkpoint.LOCATION, newCheckpoint.getLocation(),
                        Checkpoint.TIME, newCheckpoint.getTime(),
                        Checkpoint.COORD, newCheckpoint.getCoordinate()
                );
            }
        }, new CheckpointEditDialogFragment.OnDeleteCheckpointListener() {
            @Override
            public void onCheckpointDeleted() {
                if (manager.getCurrentTrip().isActiveCheckpoint(checkpoint)) {
                    manager.getCurrentTrip().sendUpdate(null, Trip.ACTIVE_CHECKPOINT_REF, null);
                }
                checkpoint.sendDelete(null);
            }
        }).show(fragment.getChildFragmentManager(), "edit checkpoint");
    }

    public interface Payload {
        int SET_ACTIVE_CHECKPOINT = 1;
        int UNSET_ACTIVE_CHECKPOINT = 2;
    }

    public class BottomSheetCheckpointVH extends CheckpointVH {

        public BottomSheetCheckpointVH(@NonNull View itemView, int viewType) {
            super(itemView, viewType);
        }

        @Override
        public void bind(Checkpoint checkpoint) {
            super.bind(checkpoint);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, checkpoint);
                }
            });
            if (manager.getCurrentTrip().isActiveCheckpoint(checkpoint)) {
                mTimelineView.setMarker(context.getResources().getDrawable(R.drawable.ic_ticker));
            } else {
                mTimelineView.setMarker(context.getResources().getDrawable(R.drawable.ic_marker));
            }

            if (manager.isTripLeader()) {
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editCheckpoint(checkpoint);
                    }
                });
            }
        }
    }

}
