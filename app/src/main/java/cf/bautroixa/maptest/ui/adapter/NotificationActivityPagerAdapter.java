package cf.bautroixa.maptest.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import cf.bautroixa.maptest.ui.notifications.TripNotificationFragment;
import cf.bautroixa.maptest.ui.notifications.UserNotificationFragment;

public class NotificationActivityPagerAdapter extends FragmentStateAdapter {
    UserNotificationFragment userNotificationFragment;
    TripNotificationFragment tripNotificationFragment;

    public NotificationActivityPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case Tabs.TAB_USER_NOTI:
                userNotificationFragment = new UserNotificationFragment();
                return userNotificationFragment;
            case Tabs.TAB_TRIP_NOTI:
                tripNotificationFragment = new TripNotificationFragment();
                return tripNotificationFragment;
        }
        return new Fragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public interface Tabs {
        int TAB_USER_NOTI = 0, TAB_TRIP_NOTI = 1;
        String[] names = {"Cá nhân", "Chuyến đi"};
    }
}
