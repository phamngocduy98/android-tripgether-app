package cf.bautroixa.maptest.ui.map.bottomspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.ui.adapter.MainActivityPagerAdapter;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.ui.theme.ViewAnim;
import cf.bautroixa.maptest.utils.DateFormatter;


public class ActiveCheckpointWidget extends Fragment implements NavigationInterfaceOwner {
    private ModelManager manager;
    private ArrayList<Checkpoint> checkpoints;
    private boolean isReadyToCheckIn = false;
    private Checkpoint activeCheckpoint;

    private NavigationInterfaces navigationInterfaces;

    private ConstraintLayout root;
    private TextView tvTimeRemain, tvName, tvCount;
    private Button btnView, btnCheckIn;

    public ActiveCheckpointWidget() {
        manager = ModelManager.getInstance();
        if (manager.getCurrentTrip().isAvailable()) {
            checkpoints = manager.getCurrentTrip().getCheckpointsManager().getList();
        } else {
            checkpoints = new ArrayList<>();
        }
    }

    private void updateCheckIn(boolean isReadyToCheckIn, final Checkpoint activeCheckpoint) {
        if (isReadyToCheckIn) {
            btnCheckIn.setVisibility(View.VISIBLE);
            if (manager.isUserCheckedIn(activeCheckpoint)) {
                btnCheckIn.setText("Đã điểm danh");
                btnCheckIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_verified_user_black_24dp, 0, 0, 0);
            } else {
                btnCheckIn.setText("Điểm danh");
                btnCheckIn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            btnCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewAnim.toggleLoading(getContext(), btnCheckIn, true, "");
                    manager.sendCheckIn().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ViewAnim.toggleLoading(getContext(), btnCheckIn, false, "Đã điểm danh");
                                tvCount.setText(String.format("%d/%d", activeCheckpoint.getVisitsManager().getList().size(), manager.getCurrentTrip().getMembersManager().getList().size()));
                                btnCheckIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_verified_user_black_24dp, 0, 0, 0);
                            } else {
                                ViewAnim.toggleLoading(getContext(), btnCheckIn, false, "Điểm danh");
                                btnCheckIn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            }
                        }
                    });
                }
            });
        } else {
            btnCheckIn.setVisibility(View.GONE);
        }
    }

    private void update(boolean isActive, @Nullable final Checkpoint checkpoint) {
        if (checkpoint != null) {
            tvTimeRemain.setText(DateFormatter.format(checkpoint.getTime()));
            tvName.setText(checkpoint.getName());
            if (isActive) {
                tvCount.setVisibility(View.VISIBLE);
                tvCount.setText(String.format("%d/%d", checkpoint.getVisitsManager().getList().size(), manager.getCurrentTrip().getMembersManager().getList().size()));
                boolean currentlyIsReadyToCheckIn = manager.isReadyToCheckIn(activeCheckpoint);
                if (isReadyToCheckIn ^ currentlyIsReadyToCheckIn) { // only update when isReadyToCheckIn changes
                    updateCheckIn(currentlyIsReadyToCheckIn, checkpoint);
                }
            } else {
                tvCount.setVisibility(View.GONE);
            }
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterfaces.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, checkpoint);
                }
            });
            btnView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterfaces.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, checkpoint);
                }
            });
        }
    }

    @Nullable
    private Checkpoint getIncomingCheckpoint() {
        if (!manager.getCurrentTrip().isAvailable()) return null;
        ArrayList<Checkpoint> checkpoints = manager.getCurrentTrip().getCheckpointsManager().getList();
        if (checkpoints.size() == 0) return null;
        Checkpoint upcomingCheckpoint = checkpoints.get(0);
        long currentSeconds = Calendar.getInstance().getTimeInMillis() / 1000;
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
        tvTimeRemain = view.findViewById(R.id.tv_time_remain_widget_active_checkpoint);
        tvName = view.findViewById(R.id.tv_name_widget_active_checkpoint);
        tvCount = view.findViewById(R.id.tv_visited_count_widget_active_checkpoint);
        btnView = view.findViewById(R.id.btn_view_checkpoint_widget_active_checkpoint);
        btnCheckIn = view.findViewById(R.id.btn_check_in_widget_active_checkpoint);
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getCurrentTrip().getActiveCheckpoint().addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
            @Override
            public void onComplete(@NonNull Task<Checkpoint> task) {
                if (task.isSuccessful()) {
                    activeCheckpoint = task.getResult();
                    if (activeCheckpoint != null) {
                        update(true, activeCheckpoint);
                    } else {
                        update(false, getIncomingCheckpoint());
                    }
                }
            }
        });
    }

    @Override
    public void setNavigationInterfaces(NavigationInterfaces navigationInterfaces) {
        this.navigationInterfaces = navigationInterfaces;
    }
}
