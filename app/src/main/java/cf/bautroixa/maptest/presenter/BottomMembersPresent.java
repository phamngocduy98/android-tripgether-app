package cf.bautroixa.maptest.presenter;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.ui.adapter.BottomMembersAdapter;

public interface BottomMembersPresent {
    void initAdapter(LifecycleOwner lifecycleOwner, NavigationInterfaces navigationInterfaces, FragmentManager fm);

    boolean isReadyToCheckIn();

    void selectUser(String userId);

    void onScrollNewPosition(int position);

    interface View {
        void updateView(User selectedUser);

        void setupAdapter(BottomMembersAdapter adapter);

        void setUpNoString(int i, int size);

        void onNoActiveTrip();

        void onInTrip();

        void scrollToPosition(int activePos);

        void onTargetUser(User user);
    }
}
