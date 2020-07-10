package cf.bautroixa.tripgether.presenter.bottomspace;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.SortedList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Trip;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomMembersPresent;
import cf.bautroixa.tripgether.ui.adapter.BottomMembersAdapter;
import cf.bautroixa.tripgether.ui.sortedlist.UserSortedListAdapterCallback;

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
        manager = ModelManager.getInstance(context);
        initActiveCheckpoint(lifecycleOwner);
    }

    @Override
    public void initAdapter(LifecycleOwner lifecycleOwner, NavigationInterface navigationInterface, FragmentManager fm) {
        this.adapter = new BottomMembersAdapter(this, navigationInterface, fm);
        this.members = new SortedList<>(User.class, new UserSortedListAdapterCallback(adapter) {
            @Override
            public int compare(User o1, User o2) {
                if (o1.getId().equals(o2.getId())) return 0;
                if (o1.getId().equals(manager.getCurrentUserRef().getId())) return 1;
                if (o2.getId().equals(manager.getCurrentUserRef().getId())) return -1;
                return super.compare(o1, o2);
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
                if (trip.isAvailable()) {
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
                        if (activePos != -1) view.scrollToPosition(activePos);
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
        adapter.notifyItemChanged(position, BottomMembersAdapter.Payload.UPDATE_HEIGHT);
        view.onTargetUser(user);
    }
}
