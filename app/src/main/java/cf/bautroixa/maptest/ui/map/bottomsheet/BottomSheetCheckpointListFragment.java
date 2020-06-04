package cf.bautroixa.maptest.ui.map.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.Document;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Trip;
import cf.bautroixa.maptest.ui.dialogs.CheckpointEditDialogFragment;


public class BottomSheetCheckpointListFragment extends Fragment implements NavigationInterfaceOwner {
    private ModelManager manager;
    private NavigationInterfaces navigationInterfaces;
    private String activeCheckpointId;

    private View btnAddCheckpoint;
    private CheckpointListRecyclerView.CheckpointsAdapter adapter;

    public BottomSheetCheckpointListFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance();
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
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_checkpoint_list, container, false);
        btnAddCheckpoint = view.findViewById(R.id.btn_add_checkpoint_bottom_sheet_checkpoints);

        adapter = new CheckpointListRecyclerView(manager, requireContext(), getChildFragmentManager(), navigationInterfaces).getAdapter();
        manager.getCurrentTrip().getCheckpointsManager().attachAdapter(this, adapter);

        RecyclerView rv = view.findViewById(R.id.rv_checkpoints_frag_trip);
        rv.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(), layoutManager.getOrientation());
        rv.setLayoutManager(layoutManager);
//        rv.addItemDecoration(dividerItemDecoration);


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
        return view;
    }

    @Override
    public void setNavigationInterfaces(NavigationInterfaces navigationInterfaces) {
        this.navigationInterfaces = navigationInterfaces;
    }
}
