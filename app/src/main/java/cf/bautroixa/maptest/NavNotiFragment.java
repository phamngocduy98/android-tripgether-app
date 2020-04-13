package cf.bautroixa.maptest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import cf.bautroixa.maptest.firestore.MainAppManager;

public class NavNotiFragment extends Fragment {
    MainAppManager manager;

    NotificationsTabFragment notificationsTabFragment;
    InfoTabFragment infoTabFragment;

    ViewPager2 pager;
    TabLayout tabLayout;
    Adapter adapter;

    String[] tabNames = {"Portal", "Notifications"};

    public NavNotiFragment() {
        manager = MainAppManager.getInstance();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationsTabFragment = new NotificationsTabFragment();
        infoTabFragment = new InfoTabFragment();
    }

    public void setListener(OnNotificationItemClickedListener mListener) {
        this.notificationsTabFragment.setListener(mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_nav_noti, container, false);
        pager = v.findViewById(R.id.pager_frag_nav_noti);
        tabLayout = v.findViewById(R.id.tab_layout_frag_nav_noti);
        adapter = new Adapter(this);
        pager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabNames[position]);
            }
        }).attach();
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnNotificationItemClickedListener {
        void onNotificationClick(int eventType, String id);
    }

    class Adapter extends FragmentStateAdapter {

        public Adapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return infoTabFragment;
            } else {
                return notificationsTabFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
