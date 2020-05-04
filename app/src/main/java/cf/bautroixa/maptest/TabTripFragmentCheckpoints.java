package cf.bautroixa.maptest;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import cf.bautroixa.maptest.dialogs.DialogCheckpointEditFragment;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.interfaces.DataItemsSelectable;
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.theme.OneDialog;
import cf.bautroixa.maptest.utils.DateFormatter;


public class TabTripFragmentCheckpoints extends Fragment implements DataItemsSelectable<Checkpoint> {
    private MainAppManager manager;
    private ArrayList<Checkpoint> checkpoints;
    private String activeCheckpointId;
    private boolean isLeader;

    private DatasManager.OnItemInsertedListener<Checkpoint> onCheckpointInsertedListener;
    private DatasManager.OnItemChangedListener<Checkpoint> onCheckpointChangedListener;
    private DatasManager.OnItemRemovedListener<Checkpoint> onCheckpointRemovedListener;
    private DatasManager.OnDataSetChangedListener<Checkpoint> onCheckpointDataSetChangedListener;
    private Data.OnNewValueListener<Trip> onTripChanged;
    private OnDataItemSelected<Checkpoint> onCheckpointItemSelected;

    private Button btnAddCheckpoint;
    private CheckpointsAdapter adapter;

    public TabTripFragmentCheckpoints() {
        manager = MainAppManager.getInstance();
        checkpoints = manager.getCheckpoints();

        onTripChanged = new Data.OnNewValueListener<Trip>() {
            @Override
            public void onNewData(Trip trip) {
                if (trip.getActiveCheckpoint() != null && !trip.getActiveCheckpoint().getId().equals(activeCheckpointId)) {
                    if (activeCheckpointId != null) {
                        adapter.notifyItemChanged(manager.getCheckpointsManager().indexOf(activeCheckpointId));
                    }
                    adapter.notifyItemChanged(manager.getCheckpointsManager().indexOf(trip.getActiveCheckpoint().getId()));
                    activeCheckpointId = trip.getActiveCheckpoint().getId();
                }
            }
        };
        onCheckpointInsertedListener = new DatasManager.OnItemInsertedListener<Checkpoint>() {
            @Override
            public void onItemInserted(int position, Checkpoint data) {
                adapter.notifyItemInserted(position);
                if (btnAddCheckpoint != null) {
                    if (isLeader && manager.getCheckpoints().size() == 0) {
                        btnAddCheckpoint.setVisibility(View.VISIBLE);
                    } else {
                        btnAddCheckpoint.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
        onCheckpointChangedListener = new DatasManager.OnItemChangedListener<Checkpoint>() {
            @Override
            public void onItemChanged(final int position, Checkpoint data) {
                adapter.notifyItemChanged(position);
            }
        };
        onCheckpointRemovedListener = new DatasManager.OnItemRemovedListener<Checkpoint>() {
            @Override
            public void onItemRemoved(int position, Checkpoint checkpoint) {
                adapter.notifyItemRemoved(position);
                if (isLeader && manager.getCheckpoints().size() == 0 && btnAddCheckpoint != null)
                    btnAddCheckpoint.setVisibility(View.VISIBLE);
            }
        };
        onCheckpointDataSetChangedListener = new DatasManager.OnDataSetChangedListener<Checkpoint>() {
            @Override
            public void onDataSetChanged(ArrayList<Checkpoint> checkpoints) {
                adapter.notifyDataSetChanged();
                if (isLeader && manager.getCheckpoints().size() == 0 && btnAddCheckpoint != null)
                    btnAddCheckpoint.setVisibility(View.VISIBLE);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        String userId = manager.getCurrentUser().getId();
        String leaderId = manager.getCurrentTrip().getLeader().getId();
        isLeader = userId.equals(leaderId);
        btnAddCheckpoint.setVisibility(isLeader && manager.getCheckpoints().size() == 0 ? View.VISIBLE : View.INVISIBLE);
        if (manager.getCurrentTripRef() != null) onTripChanged.onNewData(manager.getCurrentTrip());
        manager.getCurrentTrip().addOnNewValueListener(onTripChanged);
        manager.getCheckpointsManager().addOnItemChangedListener(onCheckpointChangedListener)
                .addOnItemInsertedListener(onCheckpointInsertedListener)
                .addOnItemRemovedListener(onCheckpointRemovedListener)
                .addOnDataSetChangedListener(onCheckpointDataSetChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getCurrentTrip().removeOnNewValueListener(onTripChanged);
        manager.getCheckpointsManager().removeOnItemChangedListener(onCheckpointChangedListener)
                .removeOnItemInsertedListener(onCheckpointInsertedListener)
                .removeOnItemRemovedListener(onCheckpointRemovedListener)
                .removeOnDataSetChangedListener(onCheckpointDataSetChangedListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_trip_subtab_checkpoints, container, false);
        btnAddCheckpoint = view.findViewById(R.id.btn_add_checkpoint_frag_tab_trip_subtab_checkpoints);
        adapter = new CheckpointsAdapter();
        RecyclerView rv = view.findViewById(R.id.rv_checkpoints_frag_trip);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        btnAddCheckpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogCheckpointEditFragment.newInstance(new DialogCheckpointEditFragment.OnCheckpointSetListener() {
                    @Override
                    public void onCheckpointSet(Checkpoint checkpoint) {
                        manager.getCheckpointsManager().create(null, checkpoint);
                    }
                }).show(getChildFragmentManager(), "add checkpoint");
            }
        });
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCheckpointItemSelected = null;
    }

    @Override
    public void setOnDataItemSelected(OnDataItemSelected<Checkpoint> onDataItemSelected) {
        this.onCheckpointItemSelected = onCheckpointItemSelected;
    }

    public class CheckpointVH extends RecyclerView.ViewHolder {
        public TimelineView mTimelineView;
        TextView tvName, tvTime, tvLocation;
        ImageButton btnEdit;
        View view;

        public CheckpointVH(@NonNull View itemView, int viewType) {
            super(itemView);
            view = itemView;
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint);
            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint);
            btnEdit = itemView.findViewById(R.id.btn_edit_item_checkpoint);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
        }

        public void bind(final Checkpoint checkpoint) {
            tvName.setText(checkpoint.getName());
            tvTime.setText(DateFormatter.format(checkpoint.getTime()));
            tvLocation.setText(checkpoint.getLocation());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCheckpointItemSelected != null)
                        onCheckpointItemSelected.selectItem(checkpoint);
                }
            });
            if (manager.getCurrentTripRef() != null && manager.getCurrentTrip().getActiveCheckpoint() != null) {
                setActiveCheckpoint(checkpoint.getId().equals(manager.getCurrentTrip().getActiveCheckpoint().getId()));
            }

            if (isLeader) {
                mTimelineView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Checkpoint activeCheckpoint = manager.getActiveCheckpoint();
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
                                        Task<Void> updateTask = manager.getCurrentTrip().sendUpdate(null, Trip.ACTIVE_CHECKPOINT, null);
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
                            confirmationDialog.show(getChildFragmentManager(), "confirm set active trip");
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
                                        manager.sendAddCheckInLocation(null, checkpoint.getRef(), new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                confirmationDialog.toggleProgressBar(false);
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                }
                            });
                            confirmationDialog.show(getChildFragmentManager(), "confirm set active trip");
                        }

                    }
                });
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogCheckpointEditFragment.newInstance(checkpoint, new DialogCheckpointEditFragment.OnCheckpointSetListener() {
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
                        }, new DialogCheckpointEditFragment.OnDeleteCheckpointListener() {
                            @Override
                            public void onCheckpointDeleted() {
                                if (manager.getCurrentTrip().getActiveCheckpoint() != null && checkpoint.getId().equals(manager.getCurrentTrip().getActiveCheckpoint().getId())) {
                                    manager.getCurrentTrip().sendUpdate(null, Trip.ACTIVE_CHECKPOINT, null);
                                }
                                manager.getCheckpointsManager().delete(null, checkpoint.getId());
                            }
                        }).show(getChildFragmentManager(), "edit checkpoint");
                    }
                });
            }
        }

        public void setActiveCheckpoint(boolean active) {
            if (active) {
                mTimelineView.setMarker(getResources().getDrawable(R.drawable.ic_ticker));
            } else {
                mTimelineView.setMarker(getResources().getDrawable(R.drawable.btn_flat_with_border));
            }
        }
    }

    public class CheckpointsAdapter extends RecyclerView.Adapter<CheckpointVH> {

        @NonNull
        @Override
        public CheckpointVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CheckpointVH(getLayoutInflater().inflate(R.layout.item_checkpoint_with_timeline, parent, false), viewType);
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

    public interface Payload {
        int SET_ACTIVE_CHECKPOINT = 1;
        int UNSET_ACTIVE_CHECKPOINT = 2;
    }
}
