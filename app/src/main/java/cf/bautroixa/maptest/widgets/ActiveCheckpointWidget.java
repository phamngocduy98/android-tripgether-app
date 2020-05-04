package cf.bautroixa.maptest.widgets;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.ArrayList;
import java.util.Calendar;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.utils.DateFormatter;


public class ActiveCheckpointWidget extends Fragment {
    MainAppManager manager;
    ArrayList<Checkpoint> checkpoints;

    TimelineView timelineView;
    TextView tvTimeRemain, tvName, tvLocation;

    public ActiveCheckpointWidget() {
        manager = MainAppManager.getInstance();
        checkpoints = manager.getCheckpoints();
    }

    private void update(@Nullable Checkpoint checkpoint) {
        if (checkpoint != null){
            tvTimeRemain.setText(DateFormatter.format(checkpoint.getTime()));
            tvName.setText(checkpoint.getName());
            tvLocation.setText(checkpoint.getPlaceName());
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
}
