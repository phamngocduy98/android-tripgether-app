package cf.bautroixa.tripgether.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.gauravbhola.ripplepulsebackground.RipplePulseLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Visit;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomCheckpointsPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.tripgether.ui.map.TabMapFragment;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;
import cf.bautroixa.ui.ViewAnim;

public class BottomCheckpointsAdapter extends RecyclerView.Adapter<BottomCheckpointsAdapter.ViewHolder> {
    private static final String TAG = "BottomCheckpointsAdapter";
    BottomCheckpointsPresenterImpl bottomCheckpointsPresenter;
    NavigationInterface navigationInterface;
    SortedList<Checkpoint> checkpoints;
    private DocumentsManager.OnListChangedListener<Visit> onVisitsChangedListener;

    public BottomCheckpointsAdapter(BottomCheckpointsPresenterImpl bottomCheckpointsPresenter, NavigationInterface navigationInterface) {
        this.bottomCheckpointsPresenter = bottomCheckpointsPresenter;
        this.navigationInterface = navigationInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_full, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Checkpoint checkpoint = checkpoints.get(position);
        if (bottomCheckpointsPresenter.isActiveCheckpoint(checkpoint)) {
            if (bottomCheckpointsPresenter.isReadyToCheckIn(checkpoint)) {
                holder.bind(position, checkpoint, ViewHolder.STATE_READY_TO_CHECK_IN);
            } else {
                holder.bind(position, checkpoint, ViewHolder.STATE_NOT_READY_TO_CHECK_IN);
            }
        } else {
            holder.bind(position, checkpoint, ViewHolder.STATE_NORMAL_CHECKPOINT);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.size() > 0) {
            Checkpoint checkpoint = checkpoints.get(position);
            for (Object payload : payloads) {
                if (payload instanceof UpdateVisitCountPayload) {
                    if (bottomCheckpointsPresenter.isReadyToCheckIn(checkpoint)) {
                        holder.handleState(ViewHolder.STATE_READY_TO_CHECK_IN, (UpdateVisitCountPayload) payload);
                    } else {
                        holder.handleState(ViewHolder.STATE_NOT_READY_TO_CHECK_IN, (UpdateVisitCountPayload) payload);
                    }
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return checkpoints.size();
    }

    public void setCheckpoints(SortedList<Checkpoint> checkpoints) {
        this.checkpoints = checkpoints;
    }

    /**
     * ViewHolder
     */

    public static class UpdateVisitCountPayload {
        int visitedCount, membersCount;
        boolean userCheckedIn;

        public UpdateVisitCountPayload(boolean userCheckedIn, int visitedCount, int membersCount) {
            this.userCheckedIn = userCheckedIn;
            this.visitedCount = visitedCount;
            this.membersCount = membersCount;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public static final int STATE_NORMAL_CHECKPOINT = 0;
        // active checkpoint:
        public static final int STATE_NOT_READY_TO_CHECK_IN = 11;
        public static final int STATE_READY_TO_CHECK_IN = 12;

        Checkpoint holdenCheckpoint;
        RipplePulseLayout ripplePulseLayout;
        TextView tvNo, tvName, tvLocation, tvTime, tvVisitCount;
        Button btnRoute, btnGatherHere, btnCheckIn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ripplePulseLayout = itemView.findViewById(R.id.layout_ripplepulse);
            tvVisitCount = itemView.findViewById(R.id.tv_visit_count_frag_trip_overview);
            tvNo = itemView.findViewById(R.id.tv_no_item_checkpoint_frag_trip_overview);
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint_frag_trip_overview);
            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint_frag_trip_overview);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint_frag_trip_overview);
            btnRoute = itemView.findViewById(R.id.btn_draw_route_item_checkpoint_frag_trip_overview);
            btnCheckIn = itemView.findViewById(R.id.btn_check_in_item_checkpoint_frag_trip_overview);
            btnGatherHere = itemView.findViewById(R.id.btn_gather_here_item_checkpoint_frag_trip_overview);
        }

        void handleState(int state, @Nullable BottomCheckpointsAdapter.UpdateVisitCountPayload updateVisitCountPayload) {
            if (state != STATE_NORMAL_CHECKPOINT && updateVisitCountPayload != null) {
                if (updateVisitCountPayload.userCheckedIn) { // đã điểm danh
                    btnCheckIn.setText("Bạn đã có mặt!");
                    if (updateVisitCountPayload.visitedCount == 1) {
                        tvVisitCount.setText(String.format("Bạn có mặt (%d/%d)", updateVisitCountPayload.visitedCount, updateVisitCountPayload.membersCount));
                    } else {
                        tvVisitCount.setText(String.format("Bạn và %d khác (%d/%d)", updateVisitCountPayload.visitedCount - 1, updateVisitCountPayload.visitedCount, updateVisitCountPayload.membersCount));
                    }
                } else {
                    btnCheckIn.setText("Tôi đã có mặt");
                    tvVisitCount.setText(String.format("%d thành viên (%d/%d)", updateVisitCountPayload.visitedCount, updateVisitCountPayload.visitedCount, updateVisitCountPayload.membersCount));
                }
            }
            if (bottomCheckpointsPresenter.isTripLeader()) {
                btnGatherHere.setVisibility(View.VISIBLE);
                final boolean isActiveCheckpoint = state != STATE_NORMAL_CHECKPOINT;
                final String text = isActiveCheckpoint ? "Dừng tập hợp" : "Tập hợp tại đây";
                btnGatherHere.setText(text);
                // Tập hợp tại đây button
                btnGatherHere.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewAnim.toggleLoading(bottomCheckpointsPresenter.getContext(), btnGatherHere, true, "Đang xử lí");
                        bottomCheckpointsPresenter.setActiveCheckpoint(bottomCheckpointsPresenter.getContext(), isActiveCheckpoint ? null : holdenCheckpoint).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                btnGatherHere.setEnabled(true);
                            }
                        });
                    }
                });
            }
            if (state == STATE_NORMAL_CHECKPOINT) {
                // is normal checkpoint
                tvVisitCount.setText("");
                ripplePulseLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        ripplePulseLayout.stopRippleAnimation();
                    }
                });
                btnCheckIn.setVisibility(View.GONE);
                btnRoute.setVisibility(View.VISIBLE);

            } else {
                // is Active checkpoint
                ripplePulseLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        ripplePulseLayout.startRippleAnimation();
                    }
                });
                if (state == STATE_READY_TO_CHECK_IN) { // user stay near checkpoint
                    btnRoute.setVisibility(View.GONE);
                    btnCheckIn.setVisibility(View.VISIBLE); // check-in
                } else { // user far from checkpoint
                    btnRoute.setVisibility(View.VISIBLE);
                }
            }
        }

        void bind(int index, final Checkpoint checkpoint, int state) {
            this.holdenCheckpoint = checkpoint;
            tvNo.setText(String.valueOf(index + 1));
            tvName.setText(checkpoint.getName());
            tvLocation.setText(checkpoint.getLocation());
            tvTime.setText(DateFormatter.format(checkpoint.getTime()));
            // Tôi đã có mặt button
            btnCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewAnim.toggleLoading(btnCheckIn.getContext(), btnCheckIn, true, "Đang gửi");
                    bottomCheckpointsPresenter.sendCheckIn().addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                ViewAnim.toggleLoading(btnCheckIn.getContext(), btnCheckIn, false, "Bạn đã có mặt!");
                            }
                        }
                    });
                }
            });
            // Chỉ đường button
            btnRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_ROUTE, checkpoint.getLatLng());
                }
            });
            // Bạn va n khác đã có mặt
            tvVisitCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_FRIEND_LIST_EXPANDED);
                }
            });
            handleState(state, bottomCheckpointsPresenter.getUpdateVisitCountPayload(checkpoint));
        }
    }
}