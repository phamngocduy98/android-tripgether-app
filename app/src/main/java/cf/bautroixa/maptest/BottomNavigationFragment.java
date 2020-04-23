package cf.bautroixa.maptest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import cf.bautroixa.maptest.theme.ViewAnim;

public class BottomNavigationFragment extends Fragment {
    public static final int TAB_MAP = 0;
    public static final int TAB_TRIP = 1;
    public static final int TAB_NOTES = 4;
    public static final int TAB_NOTIFICATIONS = 2;
    public static final int TAB_ME = 3;

    OnTabChangedListener onTabChangedListener;

    TabLayout tabLayout;

    public BottomNavigationFragment() {
    }

    public void setOnTabChangedListener(OnTabChangedListener onTabChangedListener) {
        this.onTabChangedListener = onTabChangedListener;
    }

    public void hide(boolean isHidden) {
        ViewAnim.toggleHideShow(tabLayout, !isHidden, ViewAnim.DIRECTION_DOWN);
    }

    public void selectTab(int tabId) {
        TabLayout.Tab tab = tabLayout.getTabAt(tabId);
        if (tab != null) tab.select();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bottom_navigation, container, false);
        tabLayout = v.findViewById(R.id.bottom_navigation);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (onTabChangedListener != null)
                    onTabChangedListener.onTabChanged(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (onTabChangedListener != null)
                    onTabChangedListener.onTabChanged(tab.getPosition());
            }
        });
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onTabChangedListener = null;
    }

    public interface OnTabChangedListener {
        void onTabChanged(int tabId);
    }
}
