package cf.bautroixa.maptest.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.ActivityNavigationInterface;
import cf.bautroixa.maptest.interfaces.ActivityNavigationInterfaceOwner;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.objects.UserNotification;
import cf.bautroixa.maptest.ui.adapter.UserNotificationAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserNotificationFragment extends Fragment implements ActivityNavigationInterfaceOwner {
    private ModelManager manager;
    private RecyclerView rvNotifications;
    private UserNotificationAdapter adapter;
    private SortedList<UserNotification> userNotifications;
    private ActivityNavigationInterface activityNavigationInterface;

    public UserNotificationFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvNotifications = view.findViewById(R.id.rv_notifications);
        rvNotifications.setAdapter(adapter);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    }

    // Called On Attach
    @Override
    public void setActivityNavigationInterface(ActivityNavigationInterface activityNavigationInterface) {
        this.activityNavigationInterface = activityNavigationInterface;
        adapter = new UserNotificationAdapter(requireContext(), activityNavigationInterface);
        userNotifications = new SortedList<>(UserNotification.class, new SortedListAdapterCallback<UserNotification>(adapter) {
            @Override
            public int compare(UserNotification o1, UserNotification o2) {
                return -o1.getTime().compareTo(o2.getTime());
            }

            @Override
            public boolean areContentsTheSame(UserNotification oldItem, UserNotification newItem) {
                // always the same because UserNotifications are not changed once created;
                return Objects.equals(oldItem.getId(), newItem.getId()) && Objects.equals(oldItem.isSeen(), newItem.isSeen());
            }

            @Override
            public boolean areItemsTheSame(UserNotification item1, UserNotification item2) {
                return item1.getId().equals(item2.getId());
            }
        });
        adapter.setUserNotifications(userNotifications);
        manager.getCurrentUser().getUserNotificationsManager().attachSortedList(this, userNotifications);
    }
}
