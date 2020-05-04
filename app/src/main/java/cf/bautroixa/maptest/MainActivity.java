package cf.bautroixa.maptest;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import cf.bautroixa.maptest.data.FcmMessage;
import cf.bautroixa.maptest.data.SearchResult;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.DataItemsSelectable;
import cf.bautroixa.maptest.interfaces.MapBackgroundCallbacks;
import cf.bautroixa.maptest.interfaces.MapBackgroundInterfaces;
import cf.bautroixa.maptest.interfaces.NavigableToMainTab;
import cf.bautroixa.maptest.interfaces.OnAppbarStateChanged;
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.interfaces.OnNavigationToMainTab;
import cf.bautroixa.maptest.theme.OneAppbarFragment;
import cf.bautroixa.maptest.utils.ShakePhoneHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int TAB_TRIP = 0, TAB_MAP = 1, TAB_NOTI = 2, TAB_CHAT = 3;
    String[] tabNames = {"Chuyến đi", "Bản đồ", "Thông báo", "Trò chuyện"};
    ShakePhoneHelper shakePhoneHelper;
    // back twice to exit
    Handler backHandler;
    int appbarState = OnAppbarStateChanged.State.EXTENDED;
    // listener
    DatasManager.OnItemInsertedListener<Event> onEventInsertedListener;
    Runnable backCallback;
    boolean isBackPressed = false;
    DatasManager.OnDataSetChangedListener<Event> onEventDataSetChangedListener;
    // tab fragment
    MapBackgroundFragment mapBackgroundFragment;
    TabMainFragment tabMapFragment;
    private MainAppManager manager;
    TabTripFragment tabTripFragment;
    TabNotificationFragment tabNotificationFragment;
    TabChatFragment tabChatFragment;

    // Views
    View statusBar;
    ViewPager2 bottomNavPager;
    TabLayout tabLayout;

    BotNavPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = MainAppManager.getInstance();
        onEventInsertedListener = new DatasManager.OnItemInsertedListener<Event>() {
            @Override
            public void onItemInserted(int position, Event data) {
//                Objects.requireNonNull(tabLayout.getTabAt(TAB_NOTI)).getOrCreateBadge().setNumber(position + 1);
            }
        };
        backHandler = new Handler();
        backCallback = new Runnable() {
            @Override
            public void run() {
                isBackPressed = false;
            }
        };

        // bind view
        initStatusBar();
        bottomNavPager = findViewById(R.id.bot_nav_pager);
        tabLayout = findViewById(R.id.bottom_navigation);
        mapBackgroundFragment = (MapBackgroundFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map_background);

        adapter = new BotNavPagerAdapter(this);
        bottomNavPager.setAdapter(adapter);
        bottomNavPager.setSaveEnabled(false);
        bottomNavPager.setUserInputEnabled(false);
        bottomNavPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        new TabLayoutMediator(tabLayout, bottomNavPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabNames[position]);
            }
        }).attach();

        if (manager.getCurrentTripRef() != null) {
            Objects.requireNonNull(tabLayout.getTabAt(TAB_MAP)).select();
        }

        // lac de diem danh
        shakePhoneHelper = new ShakePhoneHelper(this, new ShakePhoneHelper.OnShakeListener() {
            @Override
            public void onShake() {

            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String eventId = bundle.getString(FcmMessage.EVENT_ID, null);
            String eventTime = bundle.getString(FcmMessage.EVENT_TIME, null);
            int eventType = Integer.parseInt(bundle.getString(FcmMessage.EVENT_TYPE, "0"));
            String priority = bundle.getString(FcmMessage.EVENT_PRIORITY, "low");
            if (eventId != null) {
                // TODO: handle event here
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.getEventsManager().addOnItemInsertedListener(onEventInsertedListener).addOnDataSetChangedListener(onEventDataSetChangedListener);
        shakePhoneHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backHandler != null) backHandler.removeCallbacks(backCallback);
        manager.getEventsManager().removeOnItemInsertedListener(onEventInsertedListener).addOnDataSetChangedListener(onEventDataSetChangedListener);
        shakePhoneHelper.stop();
    }

    @Override
    public void onBackPressed() {
        if (bottomNavPager.getCurrentItem() == 0 && tabMapFragment.onBackPressed()) return;
        if (!isBackPressed) {
            isBackPressed = true;
            Toast.makeText(MainActivity.this, R.string.toast_back_again_to_exit, Toast.LENGTH_SHORT).show();
            backHandler.postDelayed(backCallback, 3000);
        } else {
            if (backHandler != null) backHandler.removeCallbacks(backCallback);
            super.onBackPressed();
        }
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

        if (fragment instanceof NavigableToMainTab) {
            ((NavigableToMainTab) fragment).setOnNavigationToMainTab(new OnNavigationToMainTab() {
                @Override
                public void navigate(int tab, int state, Data... data) {
                    if (tab == TAB_MAP) {
                        if (data.length > 0) {
                            if (data[0] instanceof SosRequest) {
                                User user = manager.getMembersManager().get(data[0].getId()); // userId == sosId
                                tabMapFragment.handleState(TabMainFragment.STATE_MEMBER_STATUS, user);
                                Objects.requireNonNull(tabLayout.getTabAt(TAB_MAP)).select();
                            }
                        } else {
                            tabMapFragment.handleState(state, data[0]);
                            Objects.requireNonNull(tabLayout.getTabAt(TAB_MAP)).select();
                        }
                    } else {
                        Log.w(TAG, "navigate to tab Not implemented");
                    }
                }
            });
        }
        if (fragment instanceof DataItemsSelectable) {
            ((DataItemsSelectable) fragment).setOnDataItemSelected(new OnDataItemSelected() {
                @Override
                public void selectItem(Data data) {
                    if (data instanceof Event) {
                        Event event = (Event) data;
                        int type = event.getType();
                        if (type == Event.Type.CHECKPOINT_ADDED && event.getCheckpointRef() != null) {
                            tabMapFragment.handleState(TabMainFragment.STATE_CHECKPOINT, manager.getCheckpointsManager().get(event.getId()));
                            Objects.requireNonNull(tabLayout.getTabAt(TAB_MAP)).select();
                        }
                        if ((type == Event.Type.USER_ADDED || type == Event.Type.USER_SOS_ADDED) && event.getUserRef() != null) {
                            tabMapFragment.handleState(TabMainFragment.STATE_MEMBER_STATUS, manager.getMembersManager().get(event.getId()));
                        }
                    }
                }
            });
        }
        if (fragment instanceof TabMainFragment) {
            ((TabMainFragment) fragment).setMapBackgroundInterfaces(new MapBackgroundInterfaces() {
                @Override
                public void targetMyLocation() {
                    mapBackgroundFragment.targetMyLocation();
                }

                @Override
                public void target(Object data) {
                    if (data instanceof Checkpoint) {
                        mapBackgroundFragment.targetCheckpoint((Checkpoint) data, manager.getCheckpointsManager().indexOf(((Checkpoint) data).getId()));
                    } else if (data instanceof User) {
                        mapBackgroundFragment.targetUser((User) data);
                    } else if (data instanceof SearchResult) {
                        mapBackgroundFragment.targetSearchResult((SearchResult) data);
                    } else {
                        Log.w(TAG, "target undefined Object type");
                    }
                }

                @Override
                public void cleanUpTempMarkerAndRoute() {
                    mapBackgroundFragment.clearRoute();
                    mapBackgroundFragment.clearTempMarker();
                }

                @Override
                public void drawRoute(@Nullable LatLng fromN, LatLng to) {
                    mapBackgroundFragment.drawRoute(fromN, to);
                }
            });
        } else if (fragment instanceof MapBackgroundFragment) {
            ((MapBackgroundFragment) fragment).setMapBackgroundCallback(new MapBackgroundCallbacks() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String type = marker.getSnippet(), id = marker.getTitle();
                    Log.d(TAG, "marker click" + type + "id=" + id);
                    if (type.equals(Collections.CHECKPOINTS)) {
                        tabMapFragment.handleState(TabMainFragment.STATE_CHECKPOINT, manager.getCheckpointsManager().get(id));
                        Objects.requireNonNull(tabLayout.getTabAt(TAB_MAP)).select();
                        return true;
                    } else if (type.equals(Collections.USERS)) {
                        tabMapFragment.handleState(TabMainFragment.STATE_MEMBER_STATUS, manager.getMembersManager().get(id));
                        Objects.requireNonNull(tabLayout.getTabAt(TAB_MAP)).select();
                        return true;
                    }
                    return false; // return false to show marker title and snippet normally
                }

                @Override
                public void onMapClick(LatLng latLng) {
                    tabMapFragment.onMapClick(latLng);
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

        public BotNavPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position){
                case 1:
                    tabMapFragment = new TabMainFragment();
                    return tabMapFragment;
                case 2:
                    tabNotificationFragment = new TabNotificationFragment();
                    return tabNotificationFragment;
                case 3:
                    tabChatFragment = new TabChatFragment();
                    return tabChatFragment;
                default:
                    tabTripFragment = new TabTripFragment();
                    return tabTripFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
