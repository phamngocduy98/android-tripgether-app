package cf.bautroixa.maptest.ui.map.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.MapBackgroundControllable;
import cf.bautroixa.maptest.interfaces.NavigationInterface;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.core.Document;
import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;
import cf.bautroixa.maptest.model.firestore.objects.Trip;
import cf.bautroixa.maptest.presenter.MapPresenter;
import cf.bautroixa.maptest.ui.adapter.BottomSheetCheckpointAdapter;
import cf.bautroixa.maptest.ui.dialogs.CheckpointEditDialogFragment;
import cf.bautroixa.maptest.ui.sortedlist.CheckpointSortedListAdapterCallback;
import cf.bautroixa.maptest.utils.ui_utils.DateFormatter;


public class BottomSheetCheckpointListFragment extends Fragment implements NavigationInterfaceOwner, MapBackgroundControllable {
    private ModelManager manager;
    private NavigationInterface navigationInterface;
    private String activeCheckpointId;

    private SeekBar sbTripProgress;
    private TextView tvTripName, tvTripProgress;
    private View btnAddCheckpoint;
    private BottomSheetCheckpointAdapter adapter;
    private MapPresenter.CallableMask mapBackgroundInterface;

    public BottomSheetCheckpointListFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
        manager.getCurrentTrip().attachListener(this, new Document.OnValueChangedListener<Trip>() {
            @Override
            public void onValueChanged(@Nullable Trip trip) {
                if (trip != null && trip.getActiveCheckpointRef() != null && !trip.getActiveCheckpointRef().getId().equals(activeCheckpointId)) {
                    if (activeCheckpointId != null) {
                        adapter.notifyItemChanged(manager.getCurrentTrip().getCheckpointsManager().indexOf(activeCheckpointId));
                    }
                    adapter.notifyItemChanged(manager.getCurrentTrip().getCheckpointsManager().indexOf(trip.getActiveCheckpointRef().getId()));
                    activeCheckpointId = trip.getActiveCheckpointRef().getId();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_checkpoint_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTripName = view.findViewById(R.id.tv_trip_name_bottom_sheet_checkpoint);
        tvTripProgress = view.findViewById(R.id.tv_trip_progress);
        sbTripProgress = view.findViewById(R.id.sb_trip_progress);

        RecyclerView rv = view.findViewById(R.id.rv_checkpoints_frag_trip);
        btnAddCheckpoint = view.findViewById(R.id.btn_add_checkpoint_bottom_sheet_checkpoints);

        adapter = new BottomSheetCheckpointAdapter(this, navigationInterface);
        SortedList<Checkpoint> checkpoints = new SortedList<>(Checkpoint.class, new CheckpointSortedListAdapterCallback(adapter));
        adapter.attachSortedList(checkpoints);
        manager.getCurrentTrip().getCheckpointsManager().attachSortedList(this, checkpoints);
        rv.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        manager.getCurrentTrip().getActiveCheckpoint().addOnCompleteListener(requireActivity(), new OnCompleteListener<Checkpoint>() {
            @Override
            public void onComplete(@NonNull Task<Checkpoint> task) {
                if (task.isSuccessful()) {
                    Checkpoint activeCheckpoint = task.getResult();
                    if (activeCheckpoint == null) activeCheckpoint = getTimeActiveCheckpoint();
                    if (activeCheckpoint == null) return;
                    mapBackgroundInterface.target(activeCheckpoint);
                    int activeIndex = checkpoints.indexOf(activeCheckpoint);
                    if (activeIndex < checkpoints.size() - 1) {
                        Checkpoint nextCheckpoint = checkpoints.get(activeIndex + 1);
                        long remainTime = nextCheckpoint.getTime().toDate().getTime() - System.currentTimeMillis();
                        if (remainTime > 0) {
                            tvTripProgress.setText(String.format("Còn %s tại %s", DateFormatter.formatInExactTimeLeft(remainTime), activeCheckpoint.getName()));
                        } else {
                            tvTripProgress.setText(String.format("Đã đến lúc tới %s", nextCheckpoint.getName()));
                        }
                    } else {
                        tvTripProgress.setText(String.format("Điểm cuối %s", activeCheckpoint.getName()));
                    }

                    // progress
                    if (checkpoints.size() > 1) {
                        Checkpoint firstCheckpoint = checkpoints.get(0);
                        Checkpoint lastCheckpoint = checkpoints.get(checkpoints.size() - 1);
                        long first = firstCheckpoint.getTime().toDate().getTime();
                        long total = (lastCheckpoint.getTime().toDate().getTime() - first);
                        long passed = System.currentTimeMillis() - first;
                        int progress = (int) (10 + (passed * 80L / total));
                        sbTripProgress.setProgress(progress);
                    }
                }
            }
        });

        tvTripName.setText(manager.getCurrentTrip().getName());
        sbTripProgress.setEnabled(false);

        if (manager.isTripLeader()) {
            btnAddCheckpoint.setVisibility(View.VISIBLE);
        } else {
            btnAddCheckpoint.setVisibility(View.GONE);
        }

        btnAddCheckpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckpointEditDialogFragment.newInstance(new CheckpointEditDialogFragment.OnCheckpointSetListener() {
                    @Override
                    public void onCheckpointSet(Checkpoint checkpoint) {
                        manager.getCurrentTrip().getCheckpointsManager().create(checkpoint);
                    }
                }).show(getChildFragmentManager(), "add checkpoint");
            }
        });
    }

    @Override
    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }

    @Nullable
    private Checkpoint getTimeActiveCheckpoint() {
        if (!manager.getCurrentTrip().isAvailable()) return null;
        ArrayList<Checkpoint> checkpoints = manager.getCurrentTrip().getCheckpointsManager().getList();
        if (checkpoints.size() == 0) return null;
        Checkpoint timeActiveCheckpoint = checkpoints.get(0);
        long currentTimeMillis = System.currentTimeMillis();
        long minDelta = currentTimeMillis - timeActiveCheckpoint.getTime().toDate().getTime();
        for (int i = 1; i < checkpoints.size(); i++) {
            Checkpoint checkpointI = checkpoints.get(i);
            long delta = currentTimeMillis - checkpointI.getTime().toDate().getTime();
            if (delta >= 0 && delta < minDelta) {
                minDelta = delta;
                timeActiveCheckpoint = checkpointI;
            }
        }
        return timeActiveCheckpoint;
    }

    @Override
    public void setMapBackgroundInterface(MapPresenter.CallableMask mapBackgroundInterface) {
        this.mapBackgroundInterface = mapBackgroundInterface;
    }
}
