package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Objects;

import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.CollectionManager;
import cf.bautroixa.maptest.model.firestore.Document;
import cf.bautroixa.maptest.model.firestore.DocumentsManager;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Trip;
import cf.bautroixa.maptest.model.firestore.Visit;
import cf.bautroixa.maptest.presenter.BottomCheckpointsPresenter;
import cf.bautroixa.maptest.ui.adapter.BottomCheckpointsAdapter;

import static cf.bautroixa.maptest.presenter.BottomCheckpointsPresenter.SavedStateKeys.SAVED_ACTIVE_POS;

public class BottomCheckpointsPresenterImpl implements BottomCheckpointsPresenter, BottomCheckpointsPresenter.CallableMask {
    private static final String TAG = "BottomCheckpointsPresenterImpl";
    Checkpoint activeCheckpoint;
    ModelManager manager;
    BottomCheckpointsAdapter adapter;
    private Context context;
    private View view;
    private int activePos = 0;
    private SortedList<Checkpoint> checkpoints;

    public BottomCheckpointsPresenterImpl(Context context, LifecycleOwner lifecycleOwner, View view) {
        this.context = context;
        this.view = view;
        this.manager = ModelManager.getInstance();
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void connectListener() {
                onScrollNewPosition(activePos);
            }
        });
    }

    @Override
    public void initAdapter(LifecycleOwner lifecycleOwner, NavigationInterfaces navigationInterfaces) {
        this.adapter = new BottomCheckpointsAdapter(this, navigationInterfaces);
        this.checkpoints = new SortedList<>(Checkpoint.class, new SortedListAdapterCallback<Checkpoint>(adapter) {
            @Override
            public int compare(Checkpoint o1, Checkpoint o2) {
                return o1.getTime().compareTo(o2.getTime());
            }

            @Override
            public boolean areContentsTheSame(Checkpoint oldItem, Checkpoint newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(Checkpoint item1, Checkpoint item2) {
                return item1.getId().equals(item2.getId());
            }
        });
        adapter.setCheckpoints(checkpoints);
        manager.getCurrentTrip().getCheckpointsManager().attachSortedList(lifecycleOwner, checkpoints);
        checkpoints.addAll(manager.getCurrentTrip().getCheckpointsManager().getList());
        view.setupAdapter(adapter);
        if (checkpoints.size() == 0) {
            view.setUpTimeLineString(null, null);
        } else if (checkpoints.size() > 1) {
            view.setUpTimeLineString(checkpoints.get(0), checkpoints.get(1));
        } else {
            view.setUpTimeLineString(checkpoints.get(0), null);
        }
        initActiveCheckpointListener(lifecycleOwner);
    }

    public void initActiveCheckpointListener(final LifecycleOwner lifecycleOwner) {
        final DocumentsManager.OnListChangedListener visitListener = new DocumentsManager.OnListChangedListener<Visit>() {
            @Override
            public void onItemInserted(int position, Visit data) {
                adapter.notifyItemChanged(checkpoints.indexOf(activeCheckpoint), getUpdateVisitCountPayload(activeCheckpoint));
            }

            @Override
            public void onItemChanged(int position, Visit data) {

            }

            @Override
            public void onItemRemoved(int position, Visit data) {

            }

            @Override
            public void onDataSetChanged(ArrayList<Visit> datas) {
                adapter.notifyItemChanged(checkpoints.indexOf(activeCheckpoint), getUpdateVisitCountPayload(activeCheckpoint));
            }
        };
        manager.getCurrentTrip().attachListener(lifecycleOwner, new Document.OnValueChangedListener<Trip>() {
            @Override
            public void onValueChanged(@NonNull Trip trip) {
                if (!trip.isAvailable()) {
                    view.onNoActiveTrip();
                } else {
                    view.onInTrip();
                    manager.getCurrentTrip().getActiveCheckpoint().addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
                        @Override
                        public void onComplete(@NonNull Task<Checkpoint> task) {
                            Checkpoint newActiveCheckpoint = task.getResult();
                            if (Objects.equals(activeCheckpoint, newActiveCheckpoint)) return;
                            if (activeCheckpoint != null)
                                activeCheckpoint.getVisitsManager().removeOnDatasChangedListener(visitListener);
                            activeCheckpoint = newActiveCheckpoint;
                            if (activeCheckpoint != null) {
                                activeCheckpoint.getVisitsManager().attachListener(lifecycleOwner, visitListener);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onScrollNewPosition(int position) {
        Checkpoint checkpoint = checkpoints.get(position);
        int size = checkpoints.size();
        Log.d(TAG, "new pos = " + position + "/" + size + " " + checkpoint.getId());
        if (position + 1 < size) {
            view.setUpTimeLineString(checkpoint, checkpoints.get(position + 1));
        } else {
            view.setUpTimeLineString(checkpoint, null);
        }
        view.onTargetCheckpoint(checkpoint);
    }

    @Override
    public boolean isActiveCheckpoint(Checkpoint checkpoint) {
        return activeCheckpoint != null && activeCheckpoint.getId().equals(checkpoint.getId());
    }

    @Override
    public boolean isReadyToCheckIn(Checkpoint checkpoint) {
        return manager.isReadyToCheckIn(activeCheckpoint);
    }

    @Override
    public boolean isTripLeader() {
        return manager.isTripLeader();
    }

    @Override
    public Task<Void> setActiveCheckpoint(Context context, @Nullable DocumentReference checkpointRef) {
        return manager.getCurrentTrip().sendUpdate(null, Trip.ACTIVE_CHECKPOINT_REF, checkpointRef);
    }

    @Nullable
    @Override
    public BottomCheckpointsAdapter.UpdateVisitCountPayload getUpdateVisitCountPayload(Checkpoint checkpoint) {
        if (!isActiveCheckpoint(checkpoint)) return null;
        CollectionManager<Visit> visitManager = activeCheckpoint.getVisitsManager();
        boolean isUserCheckedIn = visitManager.contains(manager.getCurrentUser().getId());
        return new BottomCheckpointsAdapter.UpdateVisitCountPayload(isUserCheckedIn, visitManager.getList().size(), manager.getCurrentTrip().getMembers().size());
    }

    @Override
    public Task<Void> sendCheckIn() {
        return manager.sendCheckIn();
    }

    @Override
    public void selectCheckpoint(String checkpointId) {
        manager.getCurrentTrip().getCheckpointsManager().requestGet(checkpointId).addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
            @Override
            public void onComplete(@NonNull Task<Checkpoint> task) {
                if (task.isSuccessful()) {
                    Checkpoint checkpoint = task.getResult();
                    if (checkpoint != null) {
                        activePos = checkpoints.indexOf(checkpoint);
                        view.scrollToPosition(activePos);
                    }
                }
            }
        });
    }

    public Context getContext() {
        return context;
    }

    public Bundle getSavedState() {
        Bundle bundle = new Bundle();
        bundle.putInt(SAVED_ACTIVE_POS, activePos);
        return bundle;
    }
}
