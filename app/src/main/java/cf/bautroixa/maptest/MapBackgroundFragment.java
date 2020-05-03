package cf.bautroixa.maptest;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import cf.bautroixa.maptest.firestore.DatasManager;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.interfaces.MapBackgroundCallbacks;
import cf.bautroixa.maptest.utils.CompassHelper;
import cf.bautroixa.maptest.utils.CreateMarker;
import cf.bautroixa.maptest.utils.ImageHelper;
import cf.bautroixa.maptest.utils.LatLngDistance;
import cf.bautroixa.maptest.utils.PixelDPConverter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cf.bautroixa.maptest.utils.CreateMarker.createMarker;

public class MapBackgroundFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveCanceledListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMapClickListener {
    private static final String TAG = "MapBackgroundFragment";
    protected LatLng currentLocation = new LatLng(0, 0);
    int screenWidth, screenHeight;
    MapBackgroundCallbacks mapBackgroundCallbacks;
    DatasManager.OnItemInsertedListener<User> onUserInsertedListener;
    DatasManager.OnItemChangedListener<User> onUserChangedListener;
    DatasManager.OnItemRemovedListener<User> onUserRemovedListener;
    DatasManager.OnItemInsertedListener<Checkpoint> onCheckpointInsertedListener;
    float cameraBearing = 0;
    protected boolean isMapLoaded = false;
    protected boolean isMarkerLoaded = false;
    protected boolean isCheckpointLoaded = false;
    protected boolean focusMyLocation = true;
    DatasManager.OnItemChangedListener<Checkpoint> onCheckpointChangedListener;
    DatasManager.OnItemRemovedListener<Checkpoint> onCheckpointRemovedListener;
    Marker activeMarker, tempMarker;
    private MainAppManager manager;
    private FusedLocationProviderClient fusedLocationClient;
    private CompassHelper compass;
    private GoogleMap mMap;
    // DATAs AND STATEs
    private ArrayList<User> members;
    private ArrayList<Checkpoint> checkpoints;
    // Views
    private Marker myLocationMarker = null, myLocationRotationMarker = null, flagMarker = null;
    private Polyline routeLine = null;
    private SupportMapFragment ggMapFragment;

    public MapBackgroundFragment() {
        manager = MainAppManager.getInstance();
        members = manager.getMembers();
        checkpoints = manager.getCheckpoints();

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_background, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initScreenSize();

        ggMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frag_map);
        Objects.requireNonNull(ggMapFragment).getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));
        compass = new CompassHelper(Objects.requireNonNull(getContext()));

        members = manager.getMembers();
        checkpoints = manager.getCheckpoints();
    }

    @Override
    public void onResume() {
        super.onResume();
        compass.start();
        manager.getMembersManager().addOnItemInsertedListener(onUserInsertedListener).addOnItemRemovedListener(onUserRemovedListener);
        manager.getCheckpointsManager().addOnItemInsertedListener(onCheckpointInsertedListener).addOnItemRemovedListener(onCheckpointRemovedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        compass.stop();
        manager.getMembersManager().removeOnItemInsertedListener(onUserInsertedListener).removeOnItemRemovedListener(onUserRemovedListener);
        manager.getCheckpointsManager().removeOnItemInsertedListener(onCheckpointInsertedListener).removeOnItemRemovedListener(onCheckpointRemovedListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSetting = mMap.getUiSettings();
        uiSetting.setMapToolbarEnabled(false);
//        uiSetting.setZoomControlsEnabled(false);
//        mMap.setMyLocationEnabled(true);
        try {
            int nightModeFlags = Objects.requireNonNull(getContext()).getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                if (!googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.maps_night))) {
                    Log.e(TAG, "Style parsing failed.");
                }
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
//                if (flagMarker == null) {
//                    flagMarker = mMap.addMarker(new MarkerOptions().position(latLng));
//                } else {
//                    flagMarker.setPosition(latLng);
//                }
            }
        });
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraMoveCanceledListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapLoaded() {
        isMapLoaded = true;
        Log.d(TAG, "map loaded");
        initFriendMarkers();
        initMyLocationMarker();
        initLocationAndCompass();
        initCheckpointMarker();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (myLocationRotationMarker.equals(marker) || myLocationMarker.equals(marker)) { // click my location marker, do nothing
            onMapClick(mMap.getCameraPosition().target);
            return true;
        }
        return mapBackgroundCallbacks.onMarkerClick(marker);
    }

    public void targetMyLocation() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
        focusMyLocation = true;
    }

    private void initScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    @Nullable
    protected Marker createFriendMarker(final User user) {
        if (!isMapLoaded || user == null || user.getLatLng() == null) return null;
        if (user.getMarker() == null) {
            user.setMarker(mMap.addMarker(new MarkerOptions().position(user.getLatLng())
                    .title(user.getId())
                    .snippet(Collections.USERS)
                    .icon(BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(Objects.requireNonNull(getContext()), R.layout.map_marker_user, new CreateMarker.ILayoutEditor() {
                        @Override
                        public void edit(View view) {
                            ImageView markerImage = view.findViewById(R.id.img_avatar_map_marker_user);
                            TextView tvName = view.findViewById(R.id.tv_name_map_marker_user);
                            if (user.getAvatar() == null || user.getAvatar().equals(User.DEFAULT_AVATAR)) {
                                markerImage.setVisibility(View.INVISIBLE);
                                tvName.setText(user.getShortName());
                            } else {
                                ImageHelper.loadImage(user.getAvatar(), markerImage);
                            }
                        }
                    })))));
        }
        return user.getMarker();
    }

    @Nullable
    protected Marker createCheckpointMarker(Checkpoint checkpoint) {
        if (!isMapLoaded || checkpoint == null || checkpoint.getLatLng() == null) return null;
        if (checkpoint.getMarker() == null) {
            checkpoint.setMarker(mMap.addMarker(new MarkerOptions().position(checkpoint.getLatLng())
                    .title(checkpoint.getId()).snippet(Collections.CHECKPOINTS)));
        }
        return checkpoint.getMarker();
    }

    void targetCamera(boolean includeMyLocation, GoogleMap.CancelableCallback cancelableCallback, LatLng... latLngs) {
        if (isMapLoaded) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : latLngs) {
                builder.include(point);
            }
            if (includeMyLocation) builder.include(currentLocation);
            LatLngBounds bounds = builder.build();
            targetCamera(bounds, cancelableCallback);
        }
    }

    void targetCamera(LatLngBounds bounds, GoogleMap.CancelableCallback cancelableCallback) {
        if (isMapLoaded) {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), cancelableCallback); // padding 100
            final int toolbarStatusbarHeight = (int) PixelDPConverter.convertDpToPixel(61 + 25, Objects.requireNonNull(getContext()));
            // TODO: calculate real bottomSpace height
            final int bottomSpaceHeight = (int) PixelDPConverter.convertDpToPixel(200, getContext());
            final int boundHeight = screenHeight - toolbarStatusbarHeight - bottomSpaceHeight;
            Log.d(TAG, "tbheight = " + toolbarStatusbarHeight);
            Log.d(TAG, "boundHei=" + boundHeight);
            Log.d(TAG, "screenHe=" + screenHeight);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, screenWidth, boundHeight, 100), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mMap.animateCamera(CameraUpdateFactory.scrollBy(0, (screenHeight - boundHeight) / 2f - toolbarStatusbarHeight));
                }

                @Override
                public void onCancel() {

                }
            });

        }
    }

    void targetCamera(boolean includeMyLocation, LatLng... latLngs) {
        targetCamera(includeMyLocation, null, latLngs);
    }

    void targetSearchResult(SearchResult searchResult) {
        tempMarker = mMap.addMarker(new MarkerOptions().position(searchResult.getCoordinate())
                .title(searchResult.getPlaceName())
                .snippet(searchResult.getPlaceAddress())
        );
        targetCamera(false, searchResult.getCoordinate());
    }

    void targetCheckpoint(Checkpoint checkpoint, final int checkpointIndex) {
        if (checkpoint != null && checkpoint.getLatLng() != null && getContext() != null) {
            Log.d(TAG, "targeting...");
            targetCamera(true, checkpoint.getLatLng());
            if (activeMarker != null) activeMarker.setIcon(null);
            activeMarker = checkpoint.getMarker();
            // TODO: ( java.lang.IllegalArgumentException: Unmanaged descriptor ) bellow this line : setIcon on removed marker
            if (activeMarker != null)
                activeMarker.setIcon((BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(getContext(), R.layout.map_marker_checkpoint_selected, new CreateMarker.ILayoutEditor() {
                    @Override
                    public void edit(View view) {
                        TextView tvName = view.findViewById(R.id.tv_name_map_marker_checkpoint_selected);
                        tvName.setText(String.valueOf(checkpointIndex));
                    }
                }))));
        }
    }

    void targetUser(User user) {
        if (user != null && user.getLatLng() != null && getContext() != null) {
            Log.d(TAG, "targeting...");
            targetCamera(true, user.getLatLng());
        }
    }

    void drawRoute(List<LatLng> latLngs) {
        final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        PolylineOptions line = new PolylineOptions().clickable(true).addAll(latLngs);
        clearRoute();
        routeLine = mMap.addPolyline(line);
        // add point to bound and apply bound to camera
        for (LatLng latLng : latLngs) {
            bounds.include(latLng);
        }
        targetCamera(bounds.build(), null);
    }

    public void clearRoute() {
        if (routeLine != null) routeLine.remove();
    }

    public void clearTempMarker() {
        if (tempMarker != null) tempMarker.remove();
    }

    void drawRoute(@Nullable LatLng fromN, final LatLng to) {
        final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        final LatLng from = (fromN != null) ? fromN : currentLocation;
        bounds.include(from);
        bounds.include(to);
        NavigationRoute.builder(getContext())
                .accessToken(getString(R.string.config_mapbox_map_api_key))
                .origin(Point.fromLngLat(from.longitude, from.latitude))
                .destination(Point.fromLngLat(to.longitude, to.latitude))
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
                        if (response.body() != null) {
                            for (DirectionsRoute route : response.body().routes()) {
                                if (route.geometry() != null) {
                                    List<Point> coords = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6).coordinates();
                                    ArrayList<LatLng> latLngs = new ArrayList<>();
                                    for (Point coord : coords) {
                                        latLngs.add(new LatLng(coord.latitude(), coord.longitude()));
                                    }
                                    drawRoute(latLngs);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable t) {
                        Log.d(TAG, "Draw route failed");
                    }
                });
    }

    @Override
    public void onCameraMoveCanceled() {
        focusMyLocation = false;
        cameraBearing = mMap.getCameraPosition().bearing;
    }

    @Override
    public void onCameraIdle() {
        focusMyLocation = false;
        cameraBearing = mMap.getCameraPosition().bearing;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mapBackgroundCallbacks.onMapClick(latLng);
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

    public void setMapBackgroundCallback(MapBackgroundCallbacks mapBackgroundCallbacks) {
        this.mapBackgroundCallbacks = mapBackgroundCallbacks;
    }
}
