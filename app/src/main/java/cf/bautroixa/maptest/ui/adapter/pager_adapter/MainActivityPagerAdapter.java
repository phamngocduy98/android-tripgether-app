package cf.bautroixa.maptest.ui.adapter.pager_adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import cf.bautroixa.maptest.ui.chat.TabChatListFragment;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.ui.posts.TabExploreFragment;

public class MainActivityPagerAdapter extends FragmentStateAdapter {
    private TabMapFragment tabMapFragment;
    private TabChatListFragment tabChatListFragment;
    private TabExploreFragment tabExploreFragment;

    public MainActivityPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                tabMapFragment = new TabMapFragment();
                return tabMapFragment;
            case 1:
                tabChatListFragment = new TabChatListFragment();
                return tabChatListFragment;
            case 2:
                tabExploreFragment = new TabExploreFragment();
                return tabExploreFragment;
//            default:
//                tabTripFragment = new TabTripFragment();
//                return tabTripFragment;

        }
        return new Fragment();
    }

    public TabMapFragment getTabMapFragment() {
        return tabMapFragment;
    }

    public TabChatListFragment getTabChatListFragment() {
        return tabChatListFragment;
    }

    public TabExploreFragment getTabExploreFragment() {
        return tabExploreFragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public interface Tabs {
        String[] tabNames = {"Bản đồ", "Trò chuyện", "Khám phá"}; //"Chuyến đi", "Thông báo"
        int TAB_ANY = -1, TAB_MAP = 0, TAB_CHAT = 1, TAB_EXPLORE = 2; //TAB_NOTI = 1, TAB_TRIP = -1
        int STATE_OPEN_DRAWER = 1;
    }
}
