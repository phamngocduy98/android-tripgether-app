package cf.bautroixa.maptest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.List;

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
import cf.bautroixa.maptest.interfaces.HasOnGoToMainActivityState;
import cf.bautroixa.maptest.interfaces.OnAppbarStateChanged;
import cf.bautroixa.maptest.interfaces.OnButtonClickedListener;
import cf.bautroixa.maptest.interfaces.OnDataItemSelected;
import cf.bautroixa.maptest.interfaces.OnDrawRouteRequest;
import cf.bautroixa.maptest.interfaces.OnDrawRouteRequestWithPath;
import cf.bautroixa.maptest.interfaces.OnGoToMainActivityState;
import cf.bautroixa.maptest.theme.OneAppbarFragment;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.ShakePhoneHelper;

public class MainActivity extends AppCompatActivity implements TabMapFragment.OnMapClicked, BottomNavigationFragment.OnTabChangedListener {
    private static final String TAG = "MainActivity";

    public static final int STATE_HIDE = -1;

    // MAP TAB
    public static final int STATE_FRIEND_LIST = 0;
    public static final int STATE_FRIEND_LIST_EXPANDED = 1;
    public static final int STATE_MEMBER_STATUS = 10;

    //TRIP TAB
    public static final int STATE_TAB_TRIP = 20;
    public static final int STATE_CHECKPOINT = 21;

    // OTHER TAB
    public static final int STATE_TAB_NOTIFICATION = 30;
    public static final int STATE_TAB_ME = 40;
    public static final int STATE_TAB_CHAT = 50;

    // SPACE
    public static final int SPACE_NONE = -1;
    public static final int SPACE_CENTER = 0;
    public static final int SPACE_BOTTOM = 1;
    public static final int SPACE_BOTTOM_SHEET = 2;

    int state, lastState = 0;
    int appbarState = OnAppbarStateChanged.State.EXTENDED;

    User selectedUser = null;

    // listener
    Data.OnNewValueListener<User> userOnNewValueListener;

    // fragment for tab
    BottomSheetMemberListFragment friendListStatusFragment;
    BottomMembersFragment bottomMembersFragment;
    BottomCheckpointsFragment bottomCheckpointsFragment;
    SearchFragment searchFragment;
    BottomNavigationFragment bottomNavigationFragment;
    TabMapFragment tabMapFragment;
    TabTripFragment tabTripFragment;
    TabNotificationFragment tabNotificationFragment;
    TabProfileFragment tabProfileFragment;
    TabChatFragment tabChatFragment;

    // Views
    View statusBar;
    LinearLayout bottomSpace, bottomSheet, centerSpace;
    LinearLayout[] spaces;
    BottomSheetBehavior bottomSheetBehavior;
    FloatingActionButton fabMyLocation;

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
        fabMyLocation = findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabMapFragment.targetMyLocation();
            }
        });

        bottomSpace = findViewById(R.id.bottom_space);
        centerSpace = findViewById(R.id.center_space);
        bottomSheet = findViewById(R.id.bottom_sheet);
        spaces = new LinearLayout[3];
        spaces[SPACE_CENTER] = centerSpace;
        spaces[SPACE_BOTTOM] = bottomSpace;
        spaces[SPACE_BOTTOM_SHEET] = bottomSheet;

        // get fragment
        tabMapFragment = (TabMapFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map);
        friendListStatusFragment = (BottomSheetMemberListFragment) getSupportFragmentManager().findFragmentById(R.id.frag_friend_list);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.frag_search);
        bottomNavigationFragment = (BottomNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.frag_bottom_navigation);
        bottomMembersFragment = new BottomMembersFragment();
        bottomCheckpointsFragment = new BottomCheckpointsFragment();
        tabNotificationFragment = new TabNotificationFragment();
        tabTripFragment = new TabTripFragment();
        tabProfileFragment = new TabProfileFragment();
        tabChatFragment = new TabChatFragment();

        bottomSheet();

        // lac de diem danh
        shakePhoneHelper = new ShakePhoneHelper(this, new ShakePhoneHelper.OnShakeListener() {
            @Override
            public void onShake() {

            }
        });
        userOnNewValueListener = new Data.OnNewValueListener<User>() {
            @Override
            public void onNewData(User user) {
                if (user.getActiveTrip() != null) {
                    bottomSheet.setVisibility(View.VISIBLE);
                } else {
                    bottomSheet.setVisibility(View.GONE);
                }
            }
        };
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
                tabMapFragment.targetCheckpoint(checkpointId);
                handleState(STATE_CHECKPOINT);
                bottomCheckpointsFragment.selectCheckpoint(checkpointId);
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
        // this.lastState == STATE_HIDE means that no previous state, or can't back to hide state
        if (this.lastState != STATE_HIDE) {
            handleState(this.lastState);
            this.lastState = STATE_HIDE;
        } else {
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

        if (fragment instanceof HasOnGoToMainActivityState) {
            ((HasOnGoToMainActivityState) fragment).setOnGoToMainActivityState(new OnGoToMainActivityState() {
                @Override
                public void newState(int state, Data[] data) {
                    if (data.length > 0) {
                        if (data[0] instanceof SosRequest) {
                            selectedUser = manager.getMembersManager().get(data[0].getId()); // userId == sosId
                            handleState(STATE_MEMBER_STATUS);
                            return;
                        }
                    }
                    handleState(state);
                }
            });
        }

        if (fragment instanceof SearchFragment) {
            ((SearchFragment) fragment).setOnSearchItemClickedListener(new SearchFragment.OnSearchItemClickedListener() {
                @Override
                public void onSearchItemClicked(SearchResult searchResult) {
                    tabMapFragment.targetCamera(false, searchResult.getCoordinate());
                }
            });
            ((SearchFragment) fragment).setOnAvatarClickedListener(new OnButtonClickedListener() {
                @Override
                public void onClick(View source) {
                    bottomNavigationFragment.selectTab(BottomNavigationFragment.TAB_ME);
                    handleState(STATE_TAB_ME);
                }
            });
        } else if (fragment instanceof BottomSheetMemberListFragment) {
            ((BottomSheetMemberListFragment) fragment).setOnFriendItemClickListener(new OnDataItemSelected<User>() {
                @Override
                public void selectItem(User user) {
                    selectedUser = user;
                    handleState(STATE_MEMBER_STATUS);
                }
            });
        } else if (fragment instanceof BottomMembersFragment) {
            ((BottomMembersFragment) fragment).setOnDrawRouteButtonClickedListener(new OnDrawRouteRequest() {
                @Override
                public void drawRouteTo(LatLng target) {
                    tabMapFragment.drawRoute(null, target, null);
                }
            });
            ((BottomMembersFragment) fragment).setOnUserChangedListener(new OnDataItemSelected<User>() {
                @Override
                public void selectItem(User user) {
                    tabMapFragment.clearRoute();
                    tabMapFragment.targetUser(user.getId());
                }
            });
        } else if (fragment instanceof BottomCheckpointsFragment) {
            ((BottomCheckpointsFragment) fragment).setOnDrawRouteRequestWithPathListener(new OnDrawRouteRequestWithPath() {
                @Override
                public void drawRoute(List<LatLng> latlngs) {
                    tabMapFragment.drawRoute(latlngs);
                }
            });
            ((BottomCheckpointsFragment) fragment).setOnCheckpointChanged(new OnDataItemSelected<Checkpoint>() {
                @Override
                public void selectItem(Checkpoint checkpoint) {
                    tabMapFragment.clearRoute();
                    tabMapFragment.targetCheckpoint(checkpoint.getId());
                }
            });
        } else if (fragment instanceof TabMapFragment) {
            ((TabMapFragment) fragment).setOnMapClicked(this);
            ((TabMapFragment) fragment).setOnMarkerClickedListener(new TabMapFragment.OnMarkerClickedListener() {
                @Override
                public void onMarkerClick(String type, String id) {
                    Log.d(TAG, "marker click" + type + "id=" + id);
                    if (type.equals(Collections.CHECKPOINTS)) {
                        handleState(STATE_CHECKPOINT);
                        bottomCheckpointsFragment.selectCheckpoint(id);
                    } else if (type.equals(Collections.USERS)) {
                        selectedUser = manager.getMembersManager().get(id);
                        handleState(STATE_MEMBER_STATUS);
                    }
                }
            });
        } else if (fragment instanceof BottomNavigationFragment) {
            ((BottomNavigationFragment) fragment).setOnTabChangedListener(this);
        } else if (fragment instanceof TabTripFragment) {
            ((TabTripFragment) fragment).setOnCheckpointItemSelected(new OnDataItemSelected<Checkpoint>() {
                @Override
                public void selectItem(Checkpoint checkpoint) {
                    tabMapFragment.clearRoute();
                    tabMapFragment.targetCheckpoint(checkpoint.getId());
                    bottomCheckpointsFragment.selectCheckpoint(checkpoint.getId());
                    handleState(STATE_CHECKPOINT);
                }
            });
        }
    }

    private void handleState(int newState) {
        state = newState;
        tabMapFragment.clearRoute();
        switch (state) {
            case STATE_FRIEND_LIST:
            case STATE_FRIEND_LIST_EXPANDED:
                if (state == STATE_FRIEND_LIST) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                selectActiveViewSpace(SPACE_BOTTOM_SHEET);
                break;
            case STATE_MEMBER_STATUS:
                selectActiveViewSpace(SPACE_BOTTOM);
                replaceBottomSpace(bottomMembersFragment);
                bottomMembersFragment.selectUser(selectedUser.getId());
                if (tabMapFragment != null)
                    tabMapFragment.targetCamera(true, selectedUser.getLatLng());
                break;
            case STATE_CHECKPOINT:
                selectActiveViewSpace(SPACE_BOTTOM);
                replaceBottomSpace(bottomCheckpointsFragment);
                break;
            case STATE_TAB_TRIP:
                selectActiveViewSpace(SPACE_CENTER);
                replaceCenterSpace(tabTripFragment);
                break;
            case STATE_TAB_NOTIFICATION:
                selectActiveViewSpace(SPACE_CENTER);
                replaceCenterSpace(tabNotificationFragment);
                break;
            case STATE_TAB_ME:
                selectActiveViewSpace(SPACE_CENTER);
                replaceCenterSpace(tabProfileFragment);
                break;
            case STATE_TAB_CHAT:
                selectActiveViewSpace(SPACE_CENTER);
                replaceCenterSpace(tabChatFragment);
                break;
        }
        Log.d(TAG, "new state= " + state);
    }

    private void selectActiveViewSpace(int viewSpace) {
        for (int i = 0; i < spaces.length; i++) {
            ViewAnim.toggleHideShow(spaces[i], viewSpace == i, ViewAnim.DIRECTION_DOWN);
        }
        if (viewSpace == SPACE_CENTER) {
            toggleStatusBar(true);
            toggleSearchBar(false);
        } else {
            toggleSearchBar(true);
        }
    }

    private void replaceBottomSpace(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.bottom_space, fragment);
        ft.commit();
    }

    void replaceCenterSpace(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.center_space, fragment);
        ft.commit();
    }

    void toggleStatusBar(boolean show) {
        ViewAnim.toggleHideShow(statusBar, show, ViewAnim.DIRECTION_UP);
    }

    void toggleToolbar(boolean show) {
        searchFragment.showHideToolbar(show);
    }

    void toggleSearchBar(boolean show) {
        searchFragment.showHideCompletely(show);
    }

    void bottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from((View) (bottomSheet));
        bottomSheetBehavior.setFitToContents(true);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                friendListStatusFragment.onBottomSheetStateChanged(i);
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                friendListStatusFragment.onSlideBottomSheet(v);
            }
        });
    }

    @Override
    public void onMapClicked(LatLng latLng) {
        Log.d(TAG, "last state= " + lastState);
        if (state != STATE_HIDE) {
            // hide all
            toggleToolbar(false);
            toggleStatusBar(false);
            selectActiveViewSpace(SPACE_NONE);
            lastState = state;
            state = STATE_HIDE;
        } else {
            // show all
            toggleToolbar(true);
            toggleStatusBar(true);
            handleState(lastState);
        }
        Log.d(TAG, "click new state= " + state);
    }

    @Override
    public void onTabChanged(int tabId) {
        switch (tabId) {
            case BottomNavigationFragment.TAB_MAP:
                handleState(STATE_FRIEND_LIST);
                break;
            case BottomNavigationFragment.TAB_TRIP:
                handleState(STATE_TAB_TRIP);
                break;
            case BottomNavigationFragment.TAB_NOTIFICATIONS:
                handleState(STATE_TAB_NOTIFICATION);
                break;
            case BottomNavigationFragment.TAB_ME:
                handleState(STATE_TAB_ME);
                break;
            case BottomNavigationFragment.TAB_NOTES:
                handleState(STATE_TAB_CHAT);
                break;
            default:
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
}
