package cf.bautroixa.maptest;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import cf.bautroixa.maptest.data.SearchResult;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.MapBackgroundControllable;
import cf.bautroixa.maptest.interfaces.MapBackgroundInterfaces;
import cf.bautroixa.maptest.interfaces.Navigable;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.theme.ViewAnim;


public class TabMapFragment extends Fragment implements Navigable, MapBackgroundControllable {
    private static final String TAG = "MapFragment";

    public static final int STATE_HIDE = -1;
    public static final int STATE_FRIEND_LIST = 0;
    public static final int STATE_FRIEND_LIST_EXPANDED = 1;
    public static final int STATE_MEMBER_STATUS = 10;
    public static final int STATE_CHECKPOINT = 21;
    public static final int STATE_SEARCH_RESULT = 22;
    public static final int STATE_SOS_REQUEST = 30;

    public static final int SPACE_NONE = -1;
    public static final int SPACE_BOTTOM = 0;
    public static final int SPACE_BOTTOM_SHEET = 1;

    // DATA AND STATE
    private MainAppManager manager;
    private int state, lastState = 0;
    private User selectedUser = null;
    private SearchResult selectedSearchResult = null;

    // LISTENER
    private MapBackgroundInterfaces mapBackgroundInterfaces;
    private NavigationInterfaces navigationInterfaces;
    private Data.OnNewValueListener<User> userOnNewValueListener;

    // VIEWS
    private View statusBar;
    private LinearLayout bottomSpace, bottomSheet;
    private LinearLayout[] spaces = new LinearLayout[2];
    private BottomSheetBehavior bottomSheetBehavior;
    private FloatingActionButton fabMyLocation;


    // CHILD FRAGMENT
    BottomSheetMemberListFragment friendListStatusFragment;
    BottomMembersFragment bottomMembersFragment;
    BottomCheckpointsFragment bottomCheckpointsFragment;
    SearchFragment searchFragment;

    public TabMapFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = MainAppManager.getInstance();
        userOnNewValueListener = new Data.OnNewValueListener<User>() {
            @Override
            public void onNewData(@Nullable User user) {
                if (user != null && user.getActiveTrip() != null) {
                    bottomSheet.setVisibility(View.VISIBLE);
                } else {
                    bottomSheet.setVisibility(View.GONE);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_map, container, false);
        fabMyLocation = view.findViewById(R.id.fab_my_location);
        bottomSpace = view.findViewById(R.id.bottom_space);
        bottomSheet = view.findViewById(R.id.bottom_sheet);
        spaces[SPACE_BOTTOM] = bottomSpace;
        spaces[SPACE_BOTTOM_SHEET] = bottomSheet;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // get fragments
        friendListStatusFragment = (BottomSheetMemberListFragment) getChildFragmentManager().findFragmentById(R.id.frag_friend_list);
        searchFragment = (SearchFragment) getChildFragmentManager().findFragmentById(R.id.frag_search);
        bottomMembersFragment = new BottomMembersFragment();
        bottomCheckpointsFragment = new BottomCheckpointsFragment();
        bottomSheet();
        fabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapBackgroundInterfaces.targetMyLocation();
            }
        });
    }

    @Override
    public void onAttachFragment(@NotNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof SearchFragment) {
            ((SearchFragment) fragment).setNavigationInterfaces(navigationInterfaces);
        } else if (fragment instanceof BottomSheetMemberListFragment) {
            ((BottomSheetMemberListFragment) fragment).setNavigationInterfaces(navigationInterfaces);
        } else if (fragment instanceof BottomMembersFragment) {
            ((BottomMembersFragment) fragment).setNavigationInterfaces(navigationInterfaces);
            ((BottomMembersFragment) fragment).setMapBackgroundInterfaces(mapBackgroundInterfaces);
        } else if (fragment instanceof BottomCheckpointsFragment) {
            ((BottomCheckpointsFragment) fragment).setMapBackgroundInterfaces(mapBackgroundInterfaces);
            ((BottomCheckpointsFragment) fragment).setNavigationInterfaces(navigationInterfaces);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getCurrentUser().addOnNewValueListener(userOnNewValueListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.getCurrentUser().removeOnNewValueListener(userOnNewValueListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationInterfaces = null;
    }


    /**
     * onBackPressed
     * @return true if back is handled
     */
    public boolean onBackPressed() {
        // this.lastState == STATE_HIDE means that no previous state, or can't back to hide state
//        if (this.lastState != STATE_HIDE) {
//            handleState(lastState, null);
//            this.lastState = STATE_HIDE;
//            return true;
//        }
        if (this.state != STATE_FRIEND_LIST) {
            handleState(STATE_FRIEND_LIST, null);
            return true;
        }
        return false;
    }

    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "last state= " + lastState);
        if (state != STATE_HIDE) {
            // hide all
            toggleToolbar(false);
            selectActiveViewSpace(SPACE_NONE);
            lastState = state;
            state = STATE_HIDE;
        } else {
            // show all
            toggleToolbar(true);
            handleState(lastState, null);
        }
        Log.d(TAG, "click new state= " + state);
    }

    void handleState(int newState, @Nullable Object data) {
        state = newState;
        mapBackgroundInterfaces.cleanUpTempMarkerAndRoute();
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
                if (data instanceof User) selectedUser = (User) data;
                if (data instanceof SosRequest) {
                    SosRequest sosRequest = (SosRequest) data;
                    selectedUser = manager.getMembersManager().get(sosRequest.getId()); // userId == sosId
                }
                mapBackgroundInterfaces.target(selectedUser);
                bottomMembersFragment.selectUser(selectedUser.getId());
                break;
            case STATE_CHECKPOINT:
                selectActiveViewSpace(SPACE_BOTTOM);
                replaceBottomSpace(bottomCheckpointsFragment);
                if (data instanceof Checkpoint) {
                    Checkpoint checkpoint = (Checkpoint) data;
                    bottomCheckpointsFragment.selectCheckpoint(checkpoint.getId());
                    mapBackgroundInterfaces.target(data);
                }
                break;
            case STATE_SEARCH_RESULT:
                selectActiveViewSpace(SPACE_BOTTOM);
                selectedSearchResult = (SearchResult) data;
                mapBackgroundInterfaces.target(selectedSearchResult);
                replaceBottomSpace(BottomSearchPlaceFragment.newInstance(selectedSearchResult, mapBackgroundInterfaces));
                break;
        }
        Log.d(TAG, "new state= " + state);
    }

    int getSpaceBottomHeight() {
        Log.e(TAG, "height = " + bottomSpace.getHeight());
        Log.e(TAG, "height = " + bottomSpace.getMeasuredHeight());
        return bottomSpace.getHeight();
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

    private void replaceBottomSpace(Fragment fragment) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.bottom_space, fragment);
        ft.commit();
    }

    private void selectActiveViewSpace(int viewSpace) {
        for (int i = 0; i < spaces.length; i++) {
            ViewAnim.toggleHideShow(spaces[i], viewSpace == i, ViewAnim.HIDE_DIRECTION_DOWN);
        }
        toggleSearchBar(true);
    }

    @Override
    public void setNavigationInterfaces(NavigationInterfaces NavigationInterfaces) {
        this.navigationInterfaces = NavigationInterfaces;
    }

    public void setMapBackgroundInterfaces(MapBackgroundInterfaces mapBackgroundInterfaces) {
        this.mapBackgroundInterfaces = mapBackgroundInterfaces;
    }
}
