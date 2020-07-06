package cf.bautroixa.tripgether.ui.bottomspace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.constant.RequestCodes;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.ui.dialogs.JoinTripDialog;
import cf.bautroixa.tripgether.ui.notifications.NotificationActivity;
import cf.bautroixa.tripgether.ui.trip.CreateTripActivity;
import cf.bautroixa.tripgether.utils.NavigableHelper;

public class BottomCreateJoinTripFragment extends Fragment implements NavigationInterfaceOwner {
    LinearLayout linearCreateTrip, linearJoinTrip, linearNotification;
    ModelManager manager;
    private NavigationInterface navigationInterface;

    public BottomCreateJoinTripFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_create_join_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearCreateTrip = view.findViewById(R.id.linear_create_trip);
        linearJoinTrip = view.findViewById(R.id.linear_join_trip);
        linearNotification = view.findViewById(R.id.linear_notifications);

//        final int[] userNotSeen = {0};
//        final int[] tripNotSeen = {0};
//        manager.getCurrentUser().getUserNotificationsManager().attachOnNotificationCountChangedListener(this, new NotificationsManager.OnNotificationCountChanged() {
//            @Override
//            public void onChanged(int notSeenCount) {
//                userNotSeen[0] = notSeenCount;
//                toolItem.setBadgeNumber(userNotSeen[0]+tripNotSeen[0]);
//            }
//        });
//        if (manager.getCurrentTrip().isAvailable()){
//            manager.getCurrentTrip().getTripNotificationsManager().attachOnNotificationCountChangedListener(this, new NotificationsManager.OnNotificationCountChanged() {
//                @Override
//                public void onChanged(int notSeenCount) {
//                    tripNotSeen[0] = notSeenCount;
//                    toolItem.setBadgeNumber(userNotSeen[0]+tripNotSeen[0]);
//                }
//            });
//        }

        linearCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateTripActivity.class);
                startActivity(intent);
            }
        });

        linearJoinTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JoinTripDialog().show(getChildFragmentManager(), "join trip");
            }
        });

        linearNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(requireContext(), NotificationActivity.class), RequestCodes.TOOL_NOTIFICATION);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.TOOL_NOTIFICATION && data != null) {
            NavigableHelper.handleNavigation(data, navigationInterface);
        }
    }

    @Override
    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }
}
