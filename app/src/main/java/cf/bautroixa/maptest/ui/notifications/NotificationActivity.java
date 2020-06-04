package cf.bautroixa.maptest.ui.notifications;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.Document;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.ui.adapter.NotificationActivityPagerAdapter;
import cf.bautroixa.maptest.ui.theme.OneAppbarActivity;
import cf.bautroixa.maptest.utils.NavigableHelper;

public class NotificationActivity extends OneAppbarActivity {
    private static final String TAG = NotificationActivity.class.getSimpleName();
    private ModelManager manager;
    private ViewPager2 pager;
    private TabLayout tabLayout;

    public NotificationActivity() {
        manager = ModelManager.getInstance();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tab_notification);
        setTitle("Thông báo");

        tabLayout = findViewById(R.id.tab_layout_activity_notification);
        pager = findViewById(R.id.pager_activity_notification);
        pager.setAdapter(new NotificationActivityPagerAdapter(this));
        pager.setSaveEnabled(false);
        pager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                String tabName = NotificationActivityPagerAdapter.Tabs.names[position];
                tab.setText(NotificationActivityPagerAdapter.Tabs.names[position]);
            }
        }).attach();
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                String tabName = NotificationActivityPagerAdapter.Tabs.names[position];
                int unSeenCount = 0;
                if (position == NotificationActivityPagerAdapter.Tabs.TAB_USER_NOTI) {
                    unSeenCount = manager.getCurrentUser().getUserNotificationsManager().getNotSeenCount();
                } else {
                    if (manager.getCurrentTrip().getTripNotificationsManager() != null) {
                        unSeenCount = manager.getCurrentTrip().getTripNotificationsManager().getNotSeenCount();
                    }
                }
                setSubtitle(String.format("%d thông báo %s chưa đọc", unSeenCount, tabName.toLowerCase()));
            }
        });
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ActivityNavigationInterfaceOwner) {
            ((ActivityNavigationInterfaceOwner) fragment).setActivityNavigationInterface(new ActivityNavigationInterface() {
                @Override
                public void navigate(int tab, int state, Document data) {
                    // TODO: do testing to make sure it works
                    setResult(Activity.RESULT_OK, NavigableHelper.getNavigableResultIntent(tab, state, data));
                    NavigableHelper.navigate(NotificationActivity.this, tab, state, data);
                    finish();
                }
            });
        }
    }


    public interface ActivityNavigationInterface {
        void navigate(int tab, int state, Document data);
    }

    public interface ActivityNavigationInterfaceOwner {
        void setActivityNavigationInterface(ActivityNavigationInterface activityNavigationInterface);
    }
}



