package cf.bautroixa.tripgether.presenter;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.adapter.BottomMembersAdapter;

public interface BottomMembersPresent {
    void initAdapter(LifecycleOwner lifecycleOwner, NavigationInterface navigationInterface, FragmentManager fm);

    boolean isReadyToCheckIn();

    void selectUser(String userId);

    void onScrollNewPosition(int position);

    interface View {

        void setupAdapter(BottomMembersAdapter adapter);

        void setUpNoString(int i, int size);

        void scrollToPosition(int activePos);

        void onTargetUser(User user);
    }
}
