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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.data.FcmMessage;
import cf.bautroixa.maptest.data.SearchResult;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.Event;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.MapBackgroundCallbacks;
import cf.bautroixa.maptest.interfaces.MapBackgroundControllable;
import cf.bautroixa.maptest.interfaces.MapBackgroundInterfaces;
import cf.bautroixa.maptest.interfaces.Navigable;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.interfaces.OnAppbarStateChanged;
import cf.bautroixa.maptest.theme.OneAppbarFragment;
import cf.bautroixa.maptest.utils.ShakePhoneHelper;

public class MainActivity extends AppCompatActivity {
    public static final int TAB_TRIP = 0, TAB_MAP = 1, TAB_NOTI = 2, TAB_CHAT = 3;
    private static final String TAG = "MainActivity";
    String[] tabNames = {"Chuyến đi", "Bản đồ", "Thông báo", "Trò chuyện"};
    ShakePhoneHelper shakePhoneHelper;
    // back twice to exit
    Handler backHandler;
    int appbarState = OnAppbarStateChanged.State.EXTENDED;
    Event receivedEvent;
    // listener
    DatasManager.OnDatasChangedListener<Event> onEventsChangedListener;
    Runnable backCallback;
    boolean isBackPressed = false;
    // tab fragment
    MapBackgroundFragment mapBackgroundFragment;
    TabMapFragment tabMapFragment;
    TabTripFragment tabTripFragment;
    TabNotificationFragment tabNotificationFragment;
    TabChatFragment tabChatFragment;
    // Views
    View statusBar;
    ViewPager2 bottomNavPager;
    TabLayout tabLayout;
    BotNavPagerAdapter adapter;
    private MainAppManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = MainAppManager.getInstance();
        onEventsChangedListener = new DatasManager.OnDatasChangedListener<Event>() {
            @Override
            public void onItemInserted(int position, Event data) {
//                Objects.requireNonNull(tabLayout.getTabAt(TAB_NOTI)).getOrCreateBadge().setNumber(position + 1);
            }

            @Override
            public void onItemChanged(int position, Event data) {

            }

            @Override
            public void onItemRemoved(int position, Event data) {

            }

            @Override
            public void onDataSetChanged(ArrayList<Event> datas) {

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
                receivedEvent = manager.getEventsManager().get(eventId);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.getEventsManager().addOnDatasChangedListener(onEventsChangedListener);
        if (receivedEvent != null) handleEvent(receivedEvent);
        shakePhoneHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backHandler != null) backHandler.removeCallbacks(backCallback);
        manager.getEventsManager().removeOnDatasChangedListener(onEventsChangedListener);
        shakePhoneHelper.stop();
    }

    @Override
    public void onBackPressed() {
        if (bottomNavPager.getCurrentItem() == TAB_MAP && tabMapFragment.onBackPressed()) return;
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
    public void onAttachFragment(@NotNull final Fragment fragment) {
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

        if (fragment instanceof Navigable) {
            ((Navigable) fragment).setNavigationInterfaces(new NavigationInterfaces() {
                @Override
                public void navigate(int tab, int state, Object... data) {
                    Objects.requireNonNull(tabLayout.getTabAt(tab)).select();
                    if (tab == TAB_MAP) {
                        if (data.length > 0) {
                            tabMapFragment.handleState(state, data[0]);
                        } else {
                            tabMapFragment.handleState(state, null);
                        }
                    } else {
                        Log.e(TAG, "navigate to tab Not implemented");
                    }
                }
            });
        }
        if (fragment instanceof MapBackgroundControllable) {
            ((MapBackgroundControllable) fragment).setMapBackgroundInterfaces(new MapBackgroundInterfaces() {
                @Override
                public void targetMyLocation() {
                    mapBackgroundFragment.targetMyLocation();
                }

                @Override
                public void target(Object data) {
                    if (data instanceof Checkpoint) {
                        int index = manager.getCheckpointsManager().indexOf(((Checkpoint) data).getId());
                        mapBackgroundFragment.targetCheckpoint((Checkpoint) data, index);
                    } else if (data instanceof User) {
                        mapBackgroundFragment.targetUser((User) data);
                    } else if (data instanceof SearchResult) {
                        mapBackgroundFragment.targetSearchResult((SearchResult) data);
                    } else {
                        Log.e(TAG, "target undefined Object type");
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

                @Override
                public void drawLine(List<LatLng> latlngs) {
                    mapBackgroundFragment.drawRoute(latlngs, tabMapFragment.getSpaceBottomHeight());
                }
            });
        } else if (fragment instanceof MapBackgroundFragment) {
            ((MapBackgroundFragment) fragment).setMapBackgroundCallback(new MapBackgroundCallbacks() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String type = marker.getSnippet(), id = marker.getTitle();
                    Log.d(TAG, "marker click" + type + "id=" + id);
                    Objects.requireNonNull(tabLayout.getTabAt(TAB_MAP)).select();
                    if (type.equals(Collections.CHECKPOINTS)) {
                        tabMapFragment.handleState(TabMapFragment.STATE_CHECKPOINT, manager.getCheckpointsManager().get(id));
                        return true;
                    } else if (type.equals(Collections.USERS)) {
                        tabMapFragment.handleState(TabMapFragment.STATE_MEMBER_STATUS, manager.getMembersManager().get(id));
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

    private void handleEvent(Event event) {
        Objects.requireNonNull(tabLayout.getTabAt(TAB_MAP)).select();
        DocumentReference checkpointRef = event.getCheckpointRef(), userRef = event.getUserRef(), sosRef = event.getSosRef();
        switch (event.getType()) {
            case Event.Type.CHECKPOINT_ROLL_UP_ADDED:
                if (checkpointRef == null) {
                    Log.e(TAG, "invalid CHECKPOINT_ROLL_UP_ADDED event");
                    return;
                }
                manager.getCheckpointsManager().requestGet(checkpointRef.getId()).addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
                    @Override
                    public void onComplete(@NonNull Task<Checkpoint> task) {
                        if (task.isSuccessful()) {
                            Checkpoint checkpoint = task.getResult();
                            tabMapFragment.handleState(TabMapFragment.STATE_CHECKPOINT, checkpoint);
                        }
                    }
                });
                break;
            case Event.Type.USER_SOS_ADDED:
                if (sosRef == null) {
                    Log.e(TAG, "invalid USER_SOS_ADDED event");
                    return;
                }
                manager.getSosRequestsManager().requestGet(sosRef.getId()).addOnCompleteListener(new OnCompleteListener<SosRequest>() {
                    @Override
                    public void onComplete(@NonNull Task<SosRequest> task) {
                        if (task.isSuccessful()) {
                            SosRequest sosRequest = task.getResult();
                            tabMapFragment.handleState(TabMapFragment.STATE_SOS_REQUEST, sosRequest);
                        }
                    }
                });
                break;
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
            switch (position) {
                case 1:
                    tabMapFragment = new TabMapFragment();
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
