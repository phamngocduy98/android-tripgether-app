package cf.bautroixa.maptest.ui.map.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterface;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.ui.theme.ViewAnim;

public class BottomSheetFragment extends Fragment implements NavigationInterfaceOwner {
    private static final String TAG = "FriendListStatusFrag";

    // data and state
    private ModelManager manager;
    private NavigationInterface navigationInterface;
    // view
    private TextView dragMark;
    private TabLayout tabLayout;
    private ViewPager2 pager;
    private String[] tabNames = {"Thành viên", "Địa điểm"};

    public BottomSheetFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dragMark = view.findViewById(R.id.drag_mark_bottom_sheet);
        tabLayout = view.findViewById(R.id.tab_layout_bottom_sheet);
        pager = view.findViewById(R.id.pager_bottom_sheet);

        pager.setAdapter(new TabAdapter(this));
        pager.setSaveEnabled(false);
        new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabNames[position]);
            }
        }).attach();

        ViewAnim.toggleHideShow(dragMark, true, ViewAnim.HIDE_DIRECTION_UP);
        ViewAnim.toggleHideShow(tabLayout, true, ViewAnim.HIDE_DIRECTION_UP);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onBottomSheetStateChanged(int newState) {
        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            // hide Lite list
            ViewAnim.toggleHideShow(tabLayout, true, ViewAnim.HIDE_DIRECTION_UP);
        }
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            // show lite list
            ViewAnim.toggleHideShow(tabLayout, false, ViewAnim.HIDE_DIRECTION_UP);
        }
    }

    public void onSlideBottomSheet(float percent) {
        tabLayout.setTranslationY(-tabLayout.getHeight() * (1 - percent));
        tabLayout.setAlpha(percent);
        pager.setTranslationY(-tabLayout.getHeight() * (1 - percent));
//        pager.setAlpha(percent);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof NavigationInterfaceOwner) {
            ((NavigationInterfaceOwner) childFragment).setNavigationInterface(navigationInterface);
        }
    }

    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }

    static class TabAdapter extends FragmentStateAdapter {
        TabAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new BottomSheetMemberListFragment();
            }
            return new BottomSheetCheckpointListFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
