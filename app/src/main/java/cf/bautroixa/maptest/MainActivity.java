package cf.bautroixa.maptest;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import cf.bautroixa.maptest.data.SearchResult;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.Trip;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.DataItemsSelectable;
import cf.bautroixa.maptest.interfaces.NavigableToState;
import cf.bautroixa.maptest.interfaces.HasOnGoToMainActivityState;
import cf.bautroixa.maptest.interfaces.MapBackgroundCallbacks;
import cf.bautroixa.maptest.interfaces.MapBackgroundInterfaces;
import cf.bautroixa.maptest.interfaces.OnAppbarStateChanged;
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.interfaces.OnDrawRouteRequest;
import cf.bautroixa.maptest.interfaces.OnDrawRouteRequestWithPath;
import cf.bautroixa.maptest.interfaces.OnNavigationToState;
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

    // back
    Handler backHandler;
    Runnable backCallback;
    boolean isBackPressed = false;

    // listener
    Data.OnNewValueListener<User> userOnNewValueListener;

    // fragment for tab
    MapBackgroundFragment mapBackgroundFragment;
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
        mapBackgroundFragment = (MapBackgroundFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map_background);
        initStatusBar();
        bottomNavPager = findViewById(R.id.bot_nav_pager);
        tabLayout = findViewById(R.id.bottom_navigation);

        adapter = new BotNavPagerAdapter(getSupportFragmentManager(), getLifecycle());
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
                Objects.requireNonNull(tabLayout.getTabAt(0)).select();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.getCurrentTrip().addOnNewValueListener(tripOnNewValueListener);
        manager.getEventsManager().addOnItemInsertedListener(onEventInsertedListener);
        shakePhoneHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.getCurrentTrip().removeOnNewValueListener(tripOnNewValueListener);
        manager.getEventsManager().removeOnItemInsertedListener(onEventInsertedListener);
        shakePhoneHelper.stop();
    }

    @Override
    public void onBackPressed() {
        if (bottomNavPager.getCurrentItem() == 0 && tabMapFragment.onBackPressed()) return;
        if (!isBackPressed) {
            isBackPressed = true;
            Toast.makeText(MainActivity.this, R.string.toast_back_again_to_exit, Toast.LENGTH_SHORT).show();
            backHandler = new Handler();
            backCallback = new Runnable() {
                @Override
                public void run() {
                    isBackPressed = false;
                }
            };
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

        if (fragment instanceof NavigableToState) {
            ((NavigableToState) fragment).setOnNavigationToState(new OnNavigationToState() {
                @Override
                public void newState(int state, Data[] data) {
                    if (data.length > 0) {
                        if (data[0] instanceof SosRequest) {
                            User user = manager.getMembersManager().get(data[0].getId()); // userId == sosId
                            tabMapFragment.handleState(TabMapFragment.STATE_MEMBER_STATUS, user);
                            Objects.requireNonNull(tabLayout.getTabAt(0)).select();
                            return;
                        }
                    }
                    tabMapFragment.handleState(state, data[0]);
                    Objects.requireNonNull(tabLayout.getTabAt(0)).select();
                }
            });
        }
        if (fragment instanceof DataItemsSelectable) {
            ((DataItemsSelectable) fragment).setOnDataItemSelected(new OnDataItemSelected() {
                @Override
                public void selectItem(Data data) {
                    if (data instanceof Event) {
                        int type = ((Event) data).getType();
                        if (type == Event.Type.CHECKPOINT_ADDED && ((Event) data).getCheckpointRef() != null) {
                            tabMapFragment.handleState(TabMapFragment.STATE_CHECKPOINT, checkpoint);
                            Objects.requireNonNull(tabLayout.getTabAt(0)).select();
                        }
                        if ((type == Event.Type.USER_ADDED || type == Event.Type.USER_SOS_ADDED) && ((Event) data).getUserRef() != null) {
                            tabMapFragment.handleState(TabMapFragment.STATE_MEMBER_STATUS, user);
//                            bottomMembersFragment.selectUser(((Event) data).getUserRef().getId());
                        }
                    }
                }
            });
        }
        if (fragment instanceof TabTripFragment) {
            ((TabTripFragment) fragment).setOnCheckpointItemSelected(new OnDataItemSelected<Checkpoint>() {
                @Override
                public void selectItem(Checkpoint checkpoint) {
                    tabMapFragment.handleState(TabMapFragment.STATE_CHECKPOINT, checkpoint);
                    Objects.requireNonNull(tabLayout.getTabAt(0)).select();
                }
            });
        } else if (fragment instanceof TabMapFragment) {
            ((TabMapFragment) fragment).setMapBackgroundInterfaces(new MapBackgroundInterfaces() {
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
                        tabMapFragment.handleState(TabMapFragment.STATE_CHECKPOINT, manager.getCheckpointsManager().get(id));
                        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
                        return true;
                    } else if (type.equals(Collections.USERS)) {
                        tabMapFragment.handleState(TabMapFragment.STATE_MEMBER_STATUS, manager.getMembersManager().get(id));
                        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
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

        public BotNavPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            switch (position){
                case 1:
                    tabTripFragment = new TabTripFragment();
                    return tabTripFragment;
                case 2:
                    tabNotificationFragment = new TabNotificationFragment();
                    return tabNotificationFragment;
                case 3:
                    tabChatFragment = new TabChatFragment();
                    return tabChatFragment;
                default:
                    tabMapFragment = new TabMapFragment();
                    return tabMapFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
