package cf.bautroixa.maptest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.data.SearchResult;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.Data;
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.SosRequest;
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
import cf.bautroixa.maptest.utils.CompassHelper;
import cf.bautroixa.maptest.utils.CreateMarker;
import cf.bautroixa.maptest.utils.ImageHelper;
import cf.bautroixa.maptest.utils.LatLngDistance;
import cf.bautroixa.maptest.utils.PixelDPConverter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cf.bautroixa.maptest.utils.CreateMarker.createMarker;


public class TabMapFragment extends MapInterfaceFragment implements HasOnGoToMainActivityState {
    private static final String TAG = "MapFragment";

    public static final int STATE_HIDE = -1;
    public static final int STATE_FRIEND_LIST = 0;
    public static final int STATE_FRIEND_LIST_EXPANDED = 1;
    public static final int STATE_MEMBER_STATUS = 10;
    public static final int STATE_CHECKPOINT = 21;
    public static final int STATE_SEARCH_RESULT = 22;

    public static final int SPACE_NONE = -1;
    public static final int SPACE_BOTTOM = 1;
    public static final int SPACE_BOTTOM_SHEET = 2;

    // DATA AND STATE
    private MainAppManager manager;
    private FusedLocationProviderClient fusedLocationClient;
    private ArrayList<User> members;
    private ArrayList<Checkpoint> checkpoints;
    private int state, lastState = 0;
    private User selectedUser = null;
    private SearchResult selectedSearchResult = null;
    // utils service
    private CompassHelper compass;


    // LISTENER
    private OnGoToMainActivityState onNavigate;
    private Data.OnNewValueListener<User> userOnNewValueListener;

    DatasManager.OnItemInsertedListener<User> onUserInsertedListener;
    DatasManager.OnItemChangedListener<User> onUserChangedListener;
    DatasManager.OnItemRemovedListener<User> onUserRemovedListener;
    DatasManager.OnItemInsertedListener<Checkpoint> onCheckpointInsertedListener;
    DatasManager.OnItemChangedListener<Checkpoint> onCheckpointChangedListener;
    DatasManager.OnItemRemovedListener<Checkpoint> onCheckpointRemovedListener;

    // VIEWS
    private View statusBar;
    private LinearLayout bottomSpace, bottomSheet;
    private LinearLayout[] spaces = new LinearLayout[2];
    private BottomSheetBehavior bottomSheetBehavior;
    private FloatingActionButton fabMyLocation;
    private Marker myLocationMarker = null, myLocationRotationMarker = null, flagMarker = null;

    // CHILD FRAGMENT
    BottomSheetMemberListFragment friendListStatusFragment;
    BottomMembersFragment bottomMembersFragment;
    BottomCheckpointsFragment bottomCheckpointsFragment;
    SearchFragment searchFragment;
    BottomNavigationFragment bottomNavigationFragment;

    public TabMapFragment() {
        manager = MainAppManager.getInstance();
        members = manager.getMembers();
        checkpoints = manager.getCheckpoints();

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
        onUserInsertedListener = new DatasManager.OnItemInsertedListener<User>() {
            @Override
            public void onItemInserted(int position, User user) {
                user.setMarker(createFriendMarker(user));
            }
        };
        onUserRemovedListener = new DatasManager.OnItemRemovedListener<User>() {
            @Override
            public void onItemRemoved(int position, User user) {
                if (activeMarker != null && activeMarker.equals(user.getMarker()))
                    activeMarker = null;
            }
        };
        onCheckpointInsertedListener = new DatasManager.OnItemInsertedListener<Checkpoint>() {
            @Override
            public void onItemInserted(int position, Checkpoint checkpoint) {
                checkpoint.setMarker(createCheckpointMarker(checkpoint));
            }
        };
        onCheckpointRemovedListener = new DatasManager.OnItemRemovedListener<Checkpoint>() {
            @Override
            public void onItemRemoved(int position, Checkpoint checkpoint) {
                if (checkpoint.getMarker().equals(activeMarker)) activeMarker = null;
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));
        compass = new CompassHelper(Objects.requireNonNull(getContext()));

        members = manager.getMembers();
        checkpoints = manager.getCheckpoints();
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
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_main);

        Objects.requireNonNull(mapFragment).getMapAsync(this);
        bottomSheet();
        fabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetMyLocation();
            }
        });
    }

    @Override
    public void onAttachFragment(@NotNull Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof SearchFragment) {
            ((SearchFragment) fragment).setOnSearchItemClickedListener(new SearchFragment.OnSearchItemClickedListener() {
                @Override
                public void onSearchItemClicked(SearchResult searchResult) {
                    selectedSearchResult = searchResult;
                    handleState(STATE_SEARCH_RESULT, null);
                    targetSearchResult(searchResult);
                }
            });
            ((SearchFragment) fragment).setOnAvatarClickedListener(new OnButtonClickedListener() {
                @Override
                public void onClick(View source) {
                    onNavigate.newState(MainActivity.STATE_TAB_ME);
                }
            });
        } else if (fragment instanceof BottomSheetMemberListFragment) {
            ((BottomSheetMemberListFragment) fragment).setOnFriendItemClickListener(new OnDataItemSelected<User>() {
                @Override
                public void selectItem(User user) {
                    selectedUser = user;
                    handleState(STATE_MEMBER_STATUS, selectedUser);
                }
            });
        } else if (fragment instanceof BottomMembersFragment) {
            ((BottomMembersFragment) fragment).setOnDrawRouteButtonClickedListener(new OnDrawRouteRequest() {
                @Override
                public void drawRouteTo(LatLng target) {
                    drawRoute(null, target, null);
                }
            });
            ((BottomMembersFragment) fragment).setOnUserChangedListener(new OnDataItemSelected<User>() {
                @Override
                public void selectItem(User user) {
                    clearRoute();
                    targetUser(user);
                }
            });
        } else if (fragment instanceof BottomCheckpointsFragment) {
            ((BottomCheckpointsFragment) fragment).setOnDrawRouteRequestWithPathListener(new OnDrawRouteRequestWithPath() {
                @Override
                public void drawRoute(List<LatLng> latlngs) {
                    drawRoute(latlngs);
                }
            });
            ((BottomCheckpointsFragment) fragment).setOnCheckpointChanged(new OnDataItemSelected<Checkpoint>() {
                @Override
                public void selectItem(Checkpoint checkpoint) {
                    clearRoute();
                    targetCheckpoint(checkpoint, manager.getCheckpointsManager().indexOf(checkpoint.getId()));
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.getMembersManager().addOnItemInsertedListener(onUserInsertedListener).addOnItemRemovedListener(onUserRemovedListener);
        manager.getCheckpointsManager().addOnItemInsertedListener(onCheckpointInsertedListener).addOnItemRemovedListener(onCheckpointRemovedListener);
        compass.start();
    }

    @Override
    public void onMapLoaded() {
        super.onMapLoaded();
        initFriendMarkers();
        initMyLocationMarker();
        initLocationAndCompass();
        initCheckpointMarker();
    }

    @Override
    public void onMapClick(LatLng latLng) {
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
            handleState(lastState, null);
        }
        Log.d(TAG, "click new state= " + state);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (myLocationRotationMarker.equals(marker) || myLocationMarker.equals(marker)) { // click my location marker, do nothing
            onMapClick(mMap.getCameraPosition().target);
            return true;
        }
        String type = marker.getSnippet(), id = marker.getTitle();
        Log.d(TAG, "marker click" + type + "id=" + id);
        if (type.equals(Collections.CHECKPOINTS)) {
            handleState(STATE_CHECKPOINT, null);
            bottomCheckpointsFragment.selectCheckpoint(id);
        } else if (type.equals(Collections.USERS)) {
            selectedUser = manager.getMembersManager().get(id);
            handleState(STATE_MEMBER_STATUS, null);
        }
        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onNavigate = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        compass.stop();
        manager.getMembersManager().removeOnItemInsertedListener(onUserInsertedListener).removeOnItemRemovedListener(onUserRemovedListener);
        manager.getCheckpointsManager().removeOnItemInsertedListener(onCheckpointInsertedListener).removeOnItemRemovedListener(onCheckpointRemovedListener);
    }

    void handleState(int newState, @Nullable Data data) {
        state = newState;
        clearRoute();
        clearTempMarker();
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
                    selectedUser = manager.getMembersManager().get(data.getId()); // userId == sosId
                }
                targetUser(selectedUser);
                bottomMembersFragment.selectUser(selectedUser.getId());
                targetCamera(true, selectedUser.getLatLng());
            case STATE_CHECKPOINT:
                selectActiveViewSpace(SPACE_BOTTOM);
                replaceBottomSpace(bottomCheckpointsFragment);
                if (data instanceof Checkpoint) {
                    bottomCheckpointsFragment.selectCheckpoint(data.getId());
                    targetCheckpoint((Checkpoint) data, manager.getCheckpointsManager().indexOf(data.getId()));
                }
                break;
            case STATE_SEARCH_RESULT:
                selectActiveViewSpace(SPACE_BOTTOM);
                targetSearchResult(selectedSearchResult);
                replaceBottomSpace(BottomSearchPlaceFragment.newInstance(selectedSearchResult));
                break;
        }
        Log.d(TAG, "new state= " + state);
    }

    void initMyLocationMarker() {
        myLocationRotationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_direction)));
        myLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(createMarker(Objects.requireNonNull(getContext()), R.drawable.marker_my_location, 120, 120))));
    }

    void initFriendMarkers() {
        if (!isMapLoaded) return;
        for (User user : members) {
            if (user.getMarker() != null) {
                user.getMarker().remove();
            }
            user.setMarker(createFriendMarker(user));
        }
    }

    void initCheckpointMarker() {
        if (!isMapLoaded) return;
        for (Checkpoint checkpoint : checkpoints) {
            if (checkpoint.getMarker() != null) {
                checkpoint.getMarker().remove();
            }
            checkpoint.setMarker(createCheckpointMarker(checkpoint));
        }
    }

    void initLocationAndCompass() {
        currentLocation = new LatLng(0, 0);
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location res = task.getResult();
                    currentLocation = new LatLng(res.getLatitude(), res.getLongitude());
                    if (isMapLoaded)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
                }
            }
        });
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location lastLocation = locationResult.getLastLocation();
                currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                // TODO: if distance > currentUser.speed*10 (update every 10s) || distance > 50;
                if (LatLngDistance.measureDistance(currentLocation, manager.getCurrentUser().getLatLng()) > 10) {
                    manager.getCurrentUser().sendUpdate(null,
                            User.COORD, new GeoPoint(currentLocation.latitude, currentLocation.longitude),
                            User.LAST_UPDATE, FieldValue.serverTimestamp()
                    );
                }
                myLocationMarker.setPosition(currentLocation);
                if (focusMyLocation) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
                }
                myLocationRotationMarker.setPosition(currentLocation);
                super.onLocationResult(locationResult);
            }
        }, Looper.getMainLooper());
        compass.setListener(new CompassHelper.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {
                myLocationRotationMarker.setRotation(azimuth - cameraBearing);
            }
        });
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

    private void replaceBottomSpace(Fragment fragment) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.bottom_space, fragment);
        ft.commit();
    }

    private void selectActiveViewSpace(int viewSpace) {
        for (int i = 0; i < spaces.length; i++) {
            ViewAnim.toggleHideShow(spaces[i], viewSpace == i, ViewAnim.DIRECTION_DOWN);
        }
        toggleSearchBar(true);
    }

    @Override
    public void setOnGoToMainActivityState(OnGoToMainActivityState onGoToMainActivityState) {
        this.onNavigate = onGoToMainActivityState;
    }
}
