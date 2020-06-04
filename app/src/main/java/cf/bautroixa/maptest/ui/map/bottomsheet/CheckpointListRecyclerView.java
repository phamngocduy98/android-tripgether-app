package cf.bautroixa.maptest.ui.map.bottomsheet;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Trip;
import cf.bautroixa.maptest.ui.adapter.MainActivityPagerAdapter;
import cf.bautroixa.maptest.ui.dialogs.CheckpointEditDialogFragment;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.ui.theme.OneDialog;
import cf.bautroixa.maptest.utils.DateFormatter;

public class CheckpointListRecyclerView {
    ModelManager manager;
    NavigationInterfaces navigationInterfaces;
    Context context;
    FragmentManager fragmentManager;
    ArrayList<Checkpoint> checkpoints;
    Checkpoint activeCheckpoint;
    CheckpointsAdapter adapter;

    public CheckpointListRecyclerView(ModelManager manager, Context context, FragmentManager fragmentManager, NavigationInterfaces navigationInterfaces) {
        this.manager = manager;
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.navigationInterfaces = navigationInterfaces;
        this.checkpoints = manager.getCurrentTrip().getCheckpointsManager().getList();
        this.adapter = new CheckpointsAdapter();
    }

    public CheckpointsAdapter getAdapter() {
        return adapter;
    }

    public interface Payload {
        int SET_ACTIVE_CHECKPOINT = 1;
        int UNSET_ACTIVE_CHECKPOINT = 2;
    }

    public class CheckpointVH extends RecyclerView.ViewHolder {
        public TimelineView mTimelineView;
        TextView tvName, tvTime;
        ImageButton btnEdit;
        View view;

        public CheckpointVH(@NonNull View itemView, int viewType) {
            super(itemView);
            view = itemView;
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint);
            btnEdit = itemView.findViewById(R.id.btn_edit_item_checkpoint);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
        }

        public void bind(final Checkpoint checkpoint) {
            manager.getCurrentTrip().getActiveCheckpoint().addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
                @Override
                public void onComplete(@NonNull Task<Checkpoint> task) {
                    if (task.isSuccessful()) {
                        activeCheckpoint = task.getResult();
                    }
                }
            });
            tvName.setText(checkpoint.getName());
            tvTime.setText(DateFormatter.format(checkpoint.getTime()));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterfaces.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, checkpoint);
                }
            });
            if (manager.getCurrentTrip().isAvailable() && activeCheckpoint != null) {
                setActiveCheckpoint(checkpoint.getId().equals(activeCheckpoint.getId()));
            }

            if (manager.isTripLeader()) {
                mTimelineView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (activeCheckpoint != null && activeCheckpoint.getId().equals(checkpoint.getId())) {
                            // this checkpoint is active checkpoint : click will show delete active checkpoint dialog
                            final OneDialog confirmationDialog = new OneDialog.Builder().title(R.string.dialog_title_confirm_remove_active_checkpoint)
                                    .message(R.string.dialog_message_confirm_remove_active_checkpoint)
                                    .enableNegativeButton(true).build();
                            confirmationDialog.setButtonClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    if (which == DialogInterface.BUTTON_POSITIVE) {
                                        confirmationDialog.toggleProgressBar(true);
                                        Task<Void> updateTask = manager.getCurrentTrip().sendUpdate(null, Trip.ACTIVE_CHECKPOINT_REF, null);
                                        if (updateTask != null)
                                            updateTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    confirmationDialog.toggleProgressBar(false);
                                                    dialog.dismiss();
                                                }
                                            });
                                    }
                                }
                            });
                            confirmationDialog.show(fragmentManager, "confirm set active trip");
                        } else {
                            // click with set current checkpoint as active checkpoint
                            final OneDialog confirmationDialog = new OneDialog.Builder().title(R.string.dialog_title_confirm_set_active_checkpoint)
                                    .message(R.string.dialog_message_confirm_set_active_checkpoint)
                                    .enableNegativeButton(true).build();
                            confirmationDialog.setButtonClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    if (which == DialogInterface.BUTTON_POSITIVE) {
                                        confirmationDialog.toggleProgressBar(true);
                                        manager.sendAddCheckInLocation(context, null, checkpoint.getRef()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                confirmationDialog.toggleProgressBar(false);
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                }
                            });
                            confirmationDialog.show(fragmentManager, "confirm set active trip");
                        }

                    }
                });
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckpointEditDialogFragment.newInstance(checkpoint, new CheckpointEditDialogFragment.OnCheckpointSetListener() {
                            @Override
                            public void onCheckpointSet(Checkpoint newCheckpoint) {
                                checkpoint.sendUpdate(null,
                                        Checkpoint.NAME, newCheckpoint.getName(),
                                        Checkpoint.LOCATION, newCheckpoint.getLocation(),
                                        Checkpoint.TIME, newCheckpoint.getTime(),
                                        Checkpoint.COORD, newCheckpoint.getCoordinate()
                                );
                                // TODO: do proper update here then add loading screen
                            }
                        }, new CheckpointEditDialogFragment.OnDeleteCheckpointListener() {
                            @Override
                            public void onCheckpointDeleted() {
                                if (activeCheckpoint != null && checkpoint.getId().equals(activeCheckpoint.getId())) {
                                    manager.getCurrentTrip().sendUpdate(null, Trip.ACTIVE_CHECKPOINT_REF, null);
                                }
                                manager.getCurrentTrip().getCheckpointsManager().delete(null, checkpoint.getId());
                            }
                        }).show(fragmentManager, "edit checkpoint");
                    }
                });
            }
        }

        public void setActiveCheckpoint(boolean active) {
            if (active) {
                mTimelineView.setMarker(context.getResources().getDrawable(R.drawable.ic_ticker));
            } else {
                mTimelineView.setMarker(context.getResources().getDrawable(R.drawable.ic_marker));
            }
        }
    }

    public class CheckpointsAdapter extends RecyclerView.Adapter<CheckpointVH> {

        @NonNull
        @Override
        public CheckpointVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CheckpointVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_with_timeline, parent, false), viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull CheckpointVH holder, int position) {
            holder.bind(checkpoints.get(position));
        }

        @Override
        public void onBindViewHolder(@NonNull CheckpointVH holder, int position, @NonNull List<Object> payloads) {
            if (payloads.size() > 0) {
                if (payloads.get(0).equals(Payload.UNSET_ACTIVE_CHECKPOINT)) {
                    holder.setActiveCheckpoint(false);
                } else {
                    holder.setActiveCheckpoint(true);
                }
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
            return checkpoints.size();
        }
    }
}
