package cf.bautroixa.maptest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.HasOnGoToMainActivityState;
import cf.bautroixa.maptest.interfaces.OnAppbarStateChanged;
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.interfaces.OnGoToMainActivityState;
import cf.bautroixa.maptest.theme.OneAppbarFragment;
import cf.bautroixa.maptest.utils.ShakePhoneHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final int STATE_TAB_MAP = 0;
    public static final int STATE_TAB_TRIP = 1;
    public static final int STATE_TAB_NOTIFICATION = 2;
    public static final int STATE_TAB_ME = 3;
    public static final int STATE_TAB_CHAT = 4;

    // SPACE
    public static final int SPACE_NONE = -1;
    public static final int SPACE_CENTER = 0;

    int state, lastState = 0;
    int appbarState = OnAppbarStateChanged.State.EXTENDED;

    String[] tabNames = {"Bản đồ", "Chuyến đi", "Thông báo", "Trò chuyện"};

    // listener
    Data.OnNewValueListener<User> userOnNewValueListener;

    // fragment for tab

    TabMapFragment tabMapFragment;
    TabTripFragment tabTripFragment;
    TabNotificationFragment tabNotificationFragment;
    TabProfileFragment tabProfileFragment;
    TabChatFragment tabChatFragment;

    // Views
    View statusBar;
    ViewPager2 bottomNavPager;
    TabLayout tabLayout;

    BotNavPagerAdapter adapter;

    // Utils / Helper
    ShakePhoneHelper shakePhoneHelper;
    Data.OnNewValueListener<Trip> tripOnNewValueListener;
    DatasManager.OnItemInsertedListener<Event> onEventInsertedListener;
    private MainAppManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = MainAppManager.getInstance();

        // bind view
        initStatusBar();
        bottomNavPager = findViewById(R.id.bot_nav_pager);
        tabLayout = findViewById(R.id.bottom_navigation);

        adapter = new BotNavPagerAdapter(getSupportFragmentManager(), getLifecycle());
        bottomNavPager.setAdapter(adapter);
        bottomNavPager.setSaveEnabled(false);
        new TabLayoutMediator(tabLayout, bottomNavPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabNames[position]);
            }
        }).attach();

        // lac de diem danh
        shakePhoneHelper = new ShakePhoneHelper(this, new ShakePhoneHelper.OnShakeListener() {
            @Override
            public void onShake() {

            }
        });

        tripOnNewValueListener = new Data.OnNewValueListener<Trip>() {
            @Override
            public void onNewData(Trip trip) {
//                tvTitle.setText(trip.getName());
            }
        };
        onEventInsertedListener = new DatasManager.OnItemInsertedListener<Event>() {
            @Override
            public void onItemInserted(int position, Event data) {
//                bottomNavigationView.getOrCreateBadge(R.id.nav_notification_tab).setNumber(position + 1);
            }
        };

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String checkpointId = bundle.getString("checkpointId", null);
            if (checkpointId != null) {
                tabMapFragment.handleState(TabMapFragment.STATE_CHECKPOINT, manager.getCheckpointsManager().get(checkpointId));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (manager.isLoggedIn()) {
            userOnNewValueListener.onNewData(manager.getCurrentUser());
        }
        manager.getCurrentUser().addOnNewValueListener(userOnNewValueListener);
        manager.getCurrentTrip().addOnNewValueListener(tripOnNewValueListener);
        manager.getEventsManager().addOnItemInsertedListener(onEventInsertedListener);
        shakePhoneHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.getCurrentUser().removeOnNewValueListener(userOnNewValueListener);
        manager.getCurrentTrip().removeOnNewValueListener(tripOnNewValueListener);
        manager.getEventsManager().removeOnItemInsertedListener(onEventInsertedListener);
        shakePhoneHelper.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // this.lastState == STATE_HIDE means that no previous state, or can't back to hide state
        // TODO: FIX here
//        if (this.lastState != STATE_HIDE) {
//            handleState(this.lastState);
//            this.lastState = STATE_HIDE;
//        } else {
//            super.onBackPressed();
//        }
    }

    @Override
    public void onAttachFragment(@NotNull Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof OneAppbarFragment) {
            ((OneAppbarFragment) fragment).setAppbarState(appbarState);
            ((OneAppbarFragment) fragment).setOnAppbarStateChanged(new OnAppbarStateChanged() {
                @Override
                public void newState(int state) {
                    appbarState = state;
                }
            });
        }

        if (fragment instanceof HasOnGoToMainActivityState) {
            ((HasOnGoToMainActivityState) fragment).setOnGoToMainActivityState(new OnGoToMainActivityState() {
                @Override
                public void newState(int state, Data[] data) {
                    if (data.length > 0) {
                        if (data[0] instanceof SosRequest) {
                            User user = manager.getMembersManager().get(data[0].getId()); // userId == sosId
                            tabMapFragment.handleState(TabMapFragment.STATE_MEMBER_STATUS, user);
                            return;
                        }
                    }
                    tabMapFragment.handleState(state, data[0]);
                }
            });
        } else if (fragment instanceof TabTripFragment) {
            ((TabTripFragment) fragment).setOnCheckpointItemSelected(new OnDataItemSelected<Checkpoint>() {
                @Override
                public void selectItem(Checkpoint checkpoint) {
                    tabMapFragment.clearRoute();
                    tabMapFragment.handleState(TabMapFragment.STATE_CHECKPOINT, checkpoint);
                }
            });
        }
    }

    private void initStatusBar() {
        // status bar
        statusBar = findViewById(R.id.view_status_bar_activity_main);
        statusBar.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                statusBar.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, insets.getSystemWindowInsetTop()));
                return insets;
            }
        });
    }

    class BotNavPagerAdapter extends FragmentStateAdapter {

        public BotNavPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            switch (position){
                case 1: return new TabTripFragment();
                case 2: return new TabNotificationFragment();
                case 3: return new TabChatFragment();
                default: return new TabMapFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
