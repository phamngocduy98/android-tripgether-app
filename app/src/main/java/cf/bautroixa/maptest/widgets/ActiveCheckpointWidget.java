package cf.bautroixa.maptest.widgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.ArrayList;
import java.util.Calendar;

import cf.bautroixa.maptest.MainActivity;
import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.TabMapFragment;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.interfaces.Navigable;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.utils.DateFormatter;


public class ActiveCheckpointWidget extends Fragment implements Navigable {
    MainAppManager manager;
    ArrayList<Checkpoint> checkpoints;

    ConstraintLayout root;
    private NavigationInterfaces navigationInterfaces;
    TimelineView timelineView;
    TextView tvTimeRemain, tvName, tvLocation;

    public ActiveCheckpointWidget() {
        manager = MainAppManager.getInstance();
        checkpoints = manager.getCheckpoints();
    }

    private void update(@Nullable final Checkpoint checkpoint) {
        if (checkpoint != null){
            tvTimeRemain.setText(DateFormatter.format(checkpoint.getTime()));
            tvName.setText(checkpoint.getName());
            tvLocation.setText(checkpoint.getPlaceName());
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterfaces.navigate(MainActivity.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, checkpoint);
                }
            });
        }
    }

    @Nullable
    private Checkpoint getIncomingCheckpoint() {
        ArrayList<Checkpoint> checkpoints = manager.getCheckpoints();
        if (checkpoints.size() == 0) return null;
        Checkpoint upcomingCheckpoint = checkpoints.get(0);
        long currentSeconds = Calendar.getInstance().getTimeInMillis()/1000;
        long minDelta = upcomingCheckpoint.getTime().getSeconds() - currentSeconds;
        for (int i = 1; i < checkpoints.size(); i++) {
            Checkpoint checkpointI = checkpoints.get(i);
            long delta = checkpointI.getTime().getSeconds() - currentSeconds;
            if (delta < minDelta) {
                minDelta = delta;
                upcomingCheckpoint = checkpointI;
            }
        }
        return upcomingCheckpoint;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.widget_active_checkpoint, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view.findViewById(R.id.root_widget_active_checkpoint);
        timelineView = view.findViewById(R.id.timeline_widget_active_checkpoint);
        tvTimeRemain = view.findViewById(R.id.tv_time_remain_widget_active_checkpoint);
        tvName = view.findViewById(R.id.tv_name_widget_active_checkpoint);
        tvLocation = view.findViewById(R.id.tv_location_widget_active_checkpoint);

        timelineView.initLine(0); // NORMAL = 0; START = 1; END = 2; ONLYONE = 3;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (manager.getCurrentTrip().getActiveCheckpoint() != null) {
            update(manager.getActiveCheckpoint());
        } else {
            update(getIncomingCheckpoint());
        }
    }

    @Override
    public void setNavigationInterfaces(NavigationInterfaces navigationInterfaces) {
        this.navigationInterfaces = navigationInterfaces;
    }
}
