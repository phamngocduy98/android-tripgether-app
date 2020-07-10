package cf.bautroixa.tripgether.ui.map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Stack;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.MapBackgroundControllable;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Trip;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.types.SearchResult;
import cf.bautroixa.tripgether.model.ui_item.ViewState;
import cf.bautroixa.tripgether.presenter.bottomspace.MapPresenter;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.tripgether.ui.bottomspace.BottomCheckpointsFragment;
import cf.bautroixa.tripgether.ui.bottomspace.BottomCreateJoinTripFragment;
import cf.bautroixa.tripgether.ui.bottomspace.BottomExploreFragment;
import cf.bautroixa.tripgether.ui.bottomspace.BottomMembersFragment;
import cf.bautroixa.tripgether.ui.bottomspace.BottomRouteFragment;
import cf.bautroixa.tripgether.ui.bottomspace.BottomSearchPlaceFragment;
import cf.bautroixa.tripgether.ui.bottomspace.BottomToolsFragment;
import cf.bautroixa.tripgether.ui.bottomspace.BottomWeatherFragment;
import cf.bautroixa.tripgether.ui.dialogs.SearchDialogFragment;
import cf.bautroixa.tripgether.ui.bottomsheet.BottomSheetCheckpointListFragment;
import cf.bautroixa.tripgether.ui.bottomsheet.BottomSheetMemberListFragment;
import cf.bautroixa.tripgether.ui.profile.MyProfileActivity;
import cf.bautroixa.tripgether.ui.theme.RoundedImageView;
import cf.bautroixa.tripgether.ui.theme.ViewAnim;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;


public class TabMapFragment extends Fragment implements NavigationInterfaceOwner, MapBackgroundControllable {
    public static final int STATE_HIDE = -1;
    public static final int STATE_NO_TRIP = 0;
    public static final int STATE_TOOLS = 1;
    public static final int STATE_TOOLS_EXPANDED = 2;
    public static final int STATE_FRIEND_LIST = 3;
    public static final int STATE_FRIEND_LIST_EXPANDED = 4;
    public static final int STATE_CHECKPOINT_LIST = 5;
    public static final int STATE_CHECKPOINT_LIST_EXPANDED = 6;
    public static final int STATE_MEMBER_STATUS = 10;
    public static final int STATE_CHECKPOINT = 21;
    public static final int STATE_SEARCH_RESULT = 22;
    public static final int STATE_ROUTE = 40;
    public static final int STATE_WEATHER = 50;
    public static final int STATE_EXPLORE = 60;

    private static final String TAG = "MapFragment";
    // CHILD FRAGMENT
    BottomSheetMemberListFragment bottomSheetMemberListFragment;
    BottomSheetCheckpointListFragment bottomSheetCheckpointListFragment;
    BottomToolsFragment bottomToolsFragment;
    BottomMembersFragment bottomMembersFragment;
    BottomCheckpointsFragment bottomCheckpointsFragment;
    BottomRouteFragment bottomRouteFragment;
    BottomWeatherFragment bottomWeatherFragment;
    BottomExploreFragment bottomExploreFragment;
    // DATA AND STATE
    private ModelManager manager;
    private Stack<ViewState> stateStack;
    private String avatarUrl;
    private String stateId;
    private User selectedUser = null;
    private SearchResult selectedSearchResult = null;
    // LISTENER
    private MapPresenter.CallableMask mMapBackgroundInterface;
    private NavigationInterface mNavigationInterface;
    // VIEWS
    private SpaceManager spaceManager;
    private View statusBar;
    private RoundedImageView imgAvatar;
    private EditText editSearch;
    private ImageButton btnMyLocation;
    private ConstraintLayout rootSearchToolbar;
    private LinearLayout bottomSpace, bottomSheet;
    private LinearLayout[] spaces = new LinearLayout[2];
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageButton fabMyLocation, btnBack, btnDrawer;
    private BottomCreateJoinTripFragment bottomCreateJoinTripFragment;

    public TabMapFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
        stateStack = new Stack<>();
        stateStack.push(new ViewState(STATE_NO_TRIP, null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fabMyLocation = view.findViewById(R.id.btn_my_location_tab_map);
        btnBack = view.findViewById(R.id.btn_back_tab_map);
        btnDrawer = view.findViewById(R.id.btn_menu_toolbar_search);

        bottomSheet = view.findViewById(R.id.bottom_sheet);
        spaceManager = new SpaceManager(getChildFragmentManager(), view);

        rootSearchToolbar = view.findViewById(R.id.root_toolbar_search);
        editSearch = view.findViewById(R.id.edit_search_location_toolbar_search);
        imgAvatar = view.findViewById(R.id.img_avatar_toolbar_search);
        btnMyLocation = view.findViewById(R.id.btn_my_location_toolbar_search);
        // get fragments
        bottomCreateJoinTripFragment = new BottomCreateJoinTripFragment();
        bottomSheetMemberListFragment = new BottomSheetMemberListFragment();
        bottomSheetCheckpointListFragment = new BottomSheetCheckpointListFragment();
        bottomToolsFragment = new BottomToolsFragment();
        bottomMembersFragment = new BottomMembersFragment();
        bottomCheckpointsFragment = new BottomCheckpointsFragment();
        bottomWeatherFragment = new BottomWeatherFragment();
        bottomExploreFragment = new BottomExploreFragment();
        bottomSheet();
        editSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchDialogFragment searchDialog = new SearchDialogFragment();
                searchDialog.setNavigationInterface(mNavigationInterface);
                searchDialog.show(getChildFragmentManager(), "search dialog");
            }
        });
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MyProfileActivity.class));
            }
        });
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapBackgroundInterface.targetMyLocation();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNavigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_ANY, MainActivityPagerAdapter.Tabs.STATE_OPEN_DRAWER);
            }
        });
        fabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapBackgroundInterface.targetMyLocation();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandleState(stateStack.peek().state, null);
        manager.getCurrentTrip().attachListener(this, new Document.OnValueChangedListener<Trip>() {
            @Override
            public void onValueChanged(@NonNull Trip trip) {
                if (trip.isAvailable()) {
                    if (stateStack.peek().state == STATE_NO_TRIP) {
                        stateStack.clear();
                        stateStack.add(new ViewState(STATE_TOOLS, null));
                        mHandleState(STATE_TOOLS, null);
                    }
                } else {
                    if (stateStack.peek().state != STATE_NO_TRIP) {
                        stateStack.clear();
                        stateStack.add(new ViewState(STATE_NO_TRIP, null));
                        mHandleState(STATE_NO_TRIP, null);
                    }
                }
            }
        });
    }

    @Override
    public void onAttachFragment(@NotNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof NavigationInterfaceOwner) {
            ((NavigationInterfaceOwner) fragment).setNavigationInterface(mNavigationInterface);
        }
        if (fragment instanceof MapBackgroundControllable) {
            ((MapBackgroundControllable) fragment).setMapBackgroundInterface(mMapBackgroundInterface);
        }
        if (fragment instanceof BottomCheckpointsFragment) {
            bottomCheckpointsFragment.getBottomCheckpointsPresenter().selectCheckpoint(stateId);
        }
        if (fragment instanceof BottomMembersFragment) {
            bottomMembersFragment.getBottomMembersPresenter().selectUser(stateId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (manager.isLoggedIn()) {
            if (!Objects.equals(avatarUrl, manager.getCurrentUser().getAvatar())) {
                avatarUrl = manager.getCurrentUser().getAvatar();
                ImageHelper.loadCircleImage(avatarUrl, imgAvatar);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNavigationInterface = null;
        mMapBackgroundInterface = null;
    }


    /**
     * onBackPressed
     *
     * @return true if back is handled
     */
    public boolean onBackPressed() {
        // this.lastState == STATE_HIDE means that no previous state, or can't back to hide state
//        if (this.lastState != STATE_HIDE) {
//            handleState(lastState, null);
//            this.lastState = STATE_HIDE;
//            return true;
//        }
        if (stateStack.size() <= 1) {
            return false;
        } else {
            stateStack.pop();
            ViewState viewState = stateStack.peek();
            mHandleState(viewState.state, viewState.data);
            return true;
        }
    }

    public void pushState(int newState, @Nullable Object data) {
        if (newState == STATE_HIDE) {
            ViewState lastState = stateStack.peek();
            if (lastState.state == STATE_HIDE) {
                // show all
                onBackPressed();
            } else {
                // hide all
                stateStack.push(new ViewState(newState, data));
                toggleSearchToolBar(false);
                spaceManager.selectActiveViewSpace(SpaceManager.SPACE_NONE, null);
            }
        } else {
            ViewState lastState = stateStack.peek();
            if (lastState.state != newState) {
                stateStack.push(new ViewState(newState, data));
            } else {
                lastState.data = data;
            }
            this.mHandleState(newState, data);
        }
    }

    private void mHandleState(int newState, @Nullable Object data) {
        if (mMapBackgroundInterface != null) mMapBackgroundInterface.cleanUpTempMarkerAndRoute();
        toggleSearchToolBar(newState == STATE_TOOLS || newState == STATE_NO_TRIP);
        if (newState == STATE_TOOLS || newState == STATE_FRIEND_LIST || newState == STATE_CHECKPOINT_LIST) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        switch (newState) {
            case STATE_NO_TRIP:
                spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM, bottomCreateJoinTripFragment);
                break;
            case STATE_TOOLS:
            case STATE_TOOLS_EXPANDED:
                spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM_SHEET, bottomToolsFragment);
                break;
            case STATE_FRIEND_LIST:
            case STATE_FRIEND_LIST_EXPANDED:
                spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM_SHEET, bottomSheetMemberListFragment);
                break;
            case STATE_CHECKPOINT_LIST:
            case STATE_CHECKPOINT_LIST_EXPANDED:
                spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM_SHEET, bottomSheetCheckpointListFragment);
                break;
            case STATE_MEMBER_STATUS:
                spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM, bottomMembersFragment);
                if (data instanceof User) {
                    User user = (User) data;
                    stateId = user.getId();
                    if (bottomMembersFragment.isAdded()) {
                        bottomMembersFragment.getBottomMembersPresenter().selectUser(stateId);
                    }
                }
                break;
            case STATE_CHECKPOINT:
                if (data instanceof Checkpoint) {
                    Checkpoint checkpoint = (Checkpoint) data;
                    stateId = checkpoint.getId();
                    if (bottomCheckpointsFragment.isAdded()) {
                        bottomCheckpointsFragment.getBottomCheckpointsPresenter().selectCheckpoint(stateId);
                    }
                }
                spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM, bottomCheckpointsFragment);
                break;
            case STATE_SEARCH_RESULT:
                if (data instanceof SearchResult) {
                    selectedSearchResult = (SearchResult) data;
                    mMapBackgroundInterface.target(selectedSearchResult);
                    mMapBackgroundInterface.createTempMarker(selectedSearchResult.getLatLng(), selectedSearchResult.getPlaceName(), selectedSearchResult.getPlaceAddress());
                    spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM, BottomSearchPlaceFragment.newInstance(selectedSearchResult, mMapBackgroundInterface));
                } else if (data instanceof LatLng) {
                    LatLng latLng = (LatLng) data;
                    mMapBackgroundInterface.target(latLng);
                    spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM, BottomSearchPlaceFragment.newInstance(latLng, mMapBackgroundInterface));
                }
                break;
            case STATE_ROUTE:
                if (data instanceof LatLng) {
                    LatLng latLng = (LatLng) data;
                    bottomRouteFragment = BottomRouteFragment.newInstance(latLng);
                    mMapBackgroundInterface.drawRoute(null, latLng);
                    spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM, bottomRouteFragment);
                }
                break;
            case STATE_WEATHER:
                spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM, bottomWeatherFragment);
                break;
            case STATE_EXPLORE:
                spaceManager.selectActiveViewSpace(SpaceManager.SPACE_BOTTOM, bottomExploreFragment);
                break;
        }
        Log.d(TAG, "new state= " + newState);
    }

    int getSpaceBottomHeight() {
        Log.e(TAG, "height = " + bottomSpace.getHeight());
        Log.e(TAG, "height = " + bottomSpace.getMeasuredHeight());
        return bottomSpace.getHeight();
    }

    void toggleSearchToolBar(boolean show) {
        ViewAnim.toggleHideShow(rootSearchToolbar, show, ViewAnim.HIDE_DIRECTION_UP);
        fabMyLocation.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        btnBack.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    void bottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from((View) (bottomSheet));
        bottomSheetBehavior.setFitToContents(true);
//        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View view, int i) {
//                bottomSheetMemberListFragment.onBottomSheetStateChanged(i);
//            }
//
//            @Override
//            public void onSlide(@NonNull View view, float v) {
//                bottomSheetMemberListFragment.onSlideBottomSheet(v);
//            }
//        });
    }

    @Override
    public void setNavigationInterface(NavigationInterface NavigationInterface) {
        this.mNavigationInterface = NavigationInterface;
    }

    public void setMapBackgroundInterface(MapPresenter.CallableMask mapBackgroundInterface) {
        this.mMapBackgroundInterface = mapBackgroundInterface;
    }
}
