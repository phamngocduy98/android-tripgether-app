package cf.bautroixa.maptest.ui.trip_view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.ActivityNavigationInterface;
import cf.bautroixa.maptest.interfaces.ActivityNavigationInterfaceOwner;
import cf.bautroixa.maptest.model.repo.objects.CheckpointPublic;
import cf.bautroixa.maptest.ui.adapter.TripCheckpointAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripCheckpointFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripCheckpointFragment extends Fragment implements ActivityNavigationInterfaceOwner {
    public static final String ARG_CHECKPOINTS = "checkpoint";
    RecyclerView rvTripCheckpoints;
    TripCheckpointAdapter adapter;
    ArrayList<CheckpointPublic> checkpoints;
    private ActivityNavigationInterface activityNavigationInterface;

    public TripCheckpointFragment() {
    }

    public static TripCheckpointFragment newInstance(ArrayList<CheckpointPublic> checkpoints) {

        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CHECKPOINTS, checkpoints);
        TripCheckpointFragment fragment = new TripCheckpointFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_checkpoint, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvTripCheckpoints = view.findViewById(R.id.rv_trip_checkpoint);
        rvTripCheckpoints.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            checkpoints = args.getParcelableArrayList(ARG_CHECKPOINTS);
            if (checkpoints != null) {
                adapter = new TripCheckpointAdapter(checkpoints, activityNavigationInterface, getChildFragmentManager());
                rvTripCheckpoints.setAdapter(adapter);
            }
        }
    }

    @Override
    public void setActivityNavigationInterface(ActivityNavigationInterface activityNavigationInterface) {
        this.activityNavigationInterface = activityNavigationInterface;
    }
}
