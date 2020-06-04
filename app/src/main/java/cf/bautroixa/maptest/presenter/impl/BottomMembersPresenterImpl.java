package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.Document;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.SosRequest;
import cf.bautroixa.maptest.model.firestore.Trip;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.presenter.BottomMembersPresent;
import cf.bautroixa.maptest.ui.adapter.BottomMembersAdapter;

public class BottomMembersPresenterImpl implements BottomMembersPresent {
    private static final String TAG = "BottomMembersPresenterImpl";
    private final ModelManager manager;
    Context context;
    View view;
    private SortedList<User> members;
    private BottomMembersAdapter adapter;
    private Checkpoint activeCheckpoint;
    private int activePos;

    public BottomMembersPresenterImpl(LifecycleOwner lifecycleOwner, Context context, View view) {
        this.context = context;
        this.view = view;
        manager = ModelManager.getInstance();
        initActiveCheckpoint(lifecycleOwner);
    }

    @Override
    public void initAdapter(LifecycleOwner lifecycleOwner, NavigationInterfaces navigationInterfaces, FragmentManager fm) {
        this.adapter = new BottomMembersAdapter(this, navigationInterfaces, fm);
        this.members = new SortedList<>(User.class, new SortedListAdapterCallback<User>(adapter) {
            @Override
            public int compare(User o1, User o2) {
                SosRequest sos1 = o1.getSosRequest(), sos2 = o2.getSosRequest();
                if ((sos1 != null) ^ (sos2 != null)) {
                    return sos2 != null ? -1 : 1;
                }
                if (sos1 == null) return 0;
                if (sos1.isResolved() ^ sos2.isResolved()) {
                    return sos1.isResolved() ? -1 : 1;
                }
                if (sos1.getLever() != sos2.getLever()) {
                    return sos1.getLever() < sos2.getLever() ? -1 : 1;
                }
                return sos1.getTime().compareTo(sos1.getTime());
            }

            @Override
            public boolean areContentsTheSame(User oldItem, User newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(User item1, User item2) {
                return item1.getId().equals(item2.getId());
            }
        });
        adapter.setMembers(members);
        manager.getCurrentTrip().getMembersManager().attachSortedList(lifecycleOwner, members);
        members.addAll(manager.getCurrentTrip().getMembersManager().getList());
        view.setupAdapter(adapter);
        view.setUpNoString(1, members.size());
    }

    private void initActiveCheckpoint(LifecycleOwner lifecycleOwner) {
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
                            activeCheckpoint = newActiveCheckpoint;
                            adapter.setActiveCheckpoint(activeCheckpoint);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean isReadyToCheckIn() {
        return manager.isReadyToCheckIn(activeCheckpoint);
    }

    @Override
    public void selectUser(String userId) {
        manager.getCurrentTrip().getMembersManager().requestGet(userId).addOnCompleteListener(new OnCompleteListener<User>() {
            @Override
            public void onComplete(@NonNull Task<User> task) {
                if (task.isSuccessful()) {
                    User user = task.getResult();
                    if (user != null) {
                        activePos = members.indexOf(user);
                        view.scrollToPosition(activePos);
                    }
                }
            }
        });
    }

    @Override
    public void onScrollNewPosition(int position) {
        User user = members.get(position);
        int size = members.size();
        Log.d(TAG, "new pos = " + position + "/" + size + " " + user.getId());
        view.setUpNoString(position + 1, members.size());
        view.updateView(user);
        adapter.notifyItemChanged(position, BottomMembersAdapter.Payload.UPDATE_HEIGHT);
        view.onTargetUser(user);
    }
}
