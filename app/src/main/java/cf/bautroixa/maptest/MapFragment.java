package cf.bautroixa.maptest;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.Collections;
import cf.bautroixa.maptest.firestore.FireStoreManager;
import cf.bautroixa.maptest.firestore.User;
import cf.bautroixa.maptest.network_io.AppRequest;
import cf.bautroixa.maptest.network_io.HttpRequest;
import cf.bautroixa.maptest.types.MapBoxDirection;
import cf.bautroixa.maptest.utils.CompassHelper;
import cf.bautroixa.maptest.utils.CreateMarker;
import cf.bautroixa.maptest.utils.PixelDPConverter;

import static cf.bautroixa.maptest.utils.CreateMarker.createMarker;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";

    private FirebaseFirestore db;
    private FireStoreManager manager;
    private FusedLocationProviderClient fusedLocationClient;
    OnMapClicked onMapClicked;
    OnMarkerClickedListener onMarkerClickedListener;
    SharedPreferences sharedPref;
    String userName;
    Marker activeMarker;

    int screenWidth, screenHeight;

    // utils service
    private CompassHelper compass;

    // GG map component
    private GoogleMap mMap;
    private Marker myLocationMarker = null, myLocationRotationMarker = null, flagMarker = null;
    private LatLng currentLocation = new LatLng(21.0245, 105.84117);
    private Polyline routeLine = null;

    // Views
    private View posSelectedMarker;

    private boolean isMapLoaded = false, isMarkerLoaded = false, isCheckpointLoaded = false, focusMyLocation = true;

    HashMap<String, User> friends;
    HashMap<String, Checkpoint> checkpoints;

    public MapFragment() {
    }

    public void setOnMapClicked(OnMapClicked onMapClicked) {
        this.onMapClicked = onMapClicked;
    }

    public void setOnMarkerClickedListener(OnMarkerClickedListener onMarkerClickedListener) {
        this.onMarkerClickedListener = onMarkerClickedListener;
    }

    public void sendUpdateCoord(Location location){
        db.collection(Collections.USERS).document(userName).update(User.COORD, new GeoPoint(location.getLatitude(), location.getLongitude())).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "update new coord"+userName);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friends = new HashMap<>();
        checkpoints = new HashMap<>();

        initScreenSize();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        sharedPref = getContext().getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        userName = sharedPref.getString(User.USER_NAME, User.NO_USER);
        db = FirebaseFirestore.getInstance();
        manager = FireStoreManager.getInstance(userName);
        // get user
        db.collection(Collections.USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        user.setUserName(document.getId());
                        friends.put(document.getId(), user);
                    }
                    if (isMapLoaded && !isMarkerLoaded) initFriendMarkers();
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
        // listen user changed
        db.collection(Collections.USERS).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    User user = documentSnapshot.toObject(User.class);
                    String id = documentSnapshot.getId();
                    Marker marker = null;
                    if (friends.get(id) == null || friends.get(id).getMarker() == null) { // newly added
                        marker = createFriendMarker(user);
                    } else { // update position
                        marker = friends.get(id).getMarker();
                        marker.setPosition(user.getLatLng());
                    }
                    user.setMarker(marker);
                    friends.put(id, user);
                }
            }
        });
        checkpoints = manager.getCheckpoints();
        // listen checkpoint added/changed
        if (manager.getCurrentTripRef() != null) {
            db.collection(Collections.checkpoints(manager.getCurrentTripRef().getId())).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Log.d(TAG, "update checkpoint");
                        Checkpoint checkpoint = documentSnapshot.toObject(Checkpoint.class);
                        String id = documentSnapshot.getId();
                        Marker marker = null;
                        if (checkpoints.get(id) == null || checkpoints.get(id).getMarker() == null) { // newly added
                            marker = createCheckpointMarker(checkpoint);
                        } else { // update position
                            marker = checkpoints.get(id).getMarker();
                            marker.setPosition(checkpoint.getLatLng());
                        }
                        checkpoint.setMarker(marker);
                        checkpoints.put(id, checkpoint);
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        compass = new CompassHelper(getContext());
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        // GG map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(this);
        // find view
        posSelectedMarker = (View) view.findViewById(R.id.pos_selected_marker);
        return view;
    }

    void initLocationAndCompass() {
        currentLocation = new LatLng(0,0);
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null){
                    Location res = task.getResult();
                    currentLocation = new LatLng(res.getLatitude(), res.getLongitude());
                }
            }
        });
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest,new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location lastLocation = locationResult.getLastLocation();
                currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
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
                myLocationRotationMarker.setRotation(azimuth);
            }
        });
    }

    public void targetMyLocation() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
        focusMyLocation = true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSetting = mMap.getUiSettings();
        uiSetting.setMapToolbarEnabled(false);
//        uiSetting.setZoomControlsEnabled(false);
//        mMap.setMyLocationEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                isMapLoaded = true;
                initFriendMarkers();
                initMyLocationMarker();
                initLocationAndCompass();
                initCheckpointMarker();
                if (checkpoints.get("BMcuFRQqoL09YmL2v7mH").getMarker() != null){
                    Log.d(TAG, "VALID checkpoint ");
                } else {
                    Log.d(TAG, "INVALID checkpoint ");
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
            }
        });
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (myLocationRotationMarker.equals(marker) || myLocationMarker.equals(marker)) { // click my location marker, do nothing
                    return false;
                }
                if (onMarkerClickedListener != null) onMarkerClickedListener.onMarkerClick(marker.getSnippet(), marker.getTitle());
                return true;
            }
        });
        mMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
            @Override
            public void onCameraMoveCanceled() {
//                groupMarkerInfo.setVisibility(View.GONE);
                focusMyLocation = false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (onMapClicked != null) onMapClicked.onMapClicked(latLng);
            }
        });
    }

    void initMyLocationMarker() {
        myLocationRotationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_direction)));
        myLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(createMarker(getContext(), R.drawable.marker_my_location, 120, 120))));
    }

    void initFriendMarkers() {
        if (!isMapLoaded || isMarkerLoaded) return;
        for (User user : friends.values()) {
            user.setMarker(createFriendMarker(user));
        }
        isMarkerLoaded = true;
    }

    void initCheckpointMarker() {
        if (!isMapLoaded || isCheckpointLoaded) return;
        for (Checkpoint checkpoint : checkpoints.values()) {
            Marker marker = createCheckpointMarker(checkpoint);
            checkpoint.setMarker(marker);
        }
        isCheckpointLoaded = true;
    }

    public Marker createFriendMarker(final User user) {
        if (!isMapLoaded || user == null || user.getLatLng() == null) return null;
        return mMap.addMarker(new MarkerOptions().position(user.getLatLng())
                .title(user.getUserName())
                .snippet(Collections.USERS)
                .icon(BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(getContext(), R.layout.map_marker, new CreateMarker.ILayoutEditor() {
                    @Override
                    public void edit(View view) {
                        ImageView markerImage = (ImageView) view.findViewById(R.id.img_marker_avatar);
                        Picasso.get().load(user.getAvatar()).placeholder(R.drawable.user).into(markerImage);
                    }
                }))));
    }

    public Marker createCheckpointMarker(Checkpoint checkpoint) {
        if (!isMapLoaded || checkpoint == null) return null;
        return mMap.addMarker(new MarkerOptions().position(checkpoint.getLatLng())
                .title(checkpoint.getId()).snippet(Collections.CHECKPOINTS));
    }

    void targetCamera(boolean includeMyLocation, GoogleMap.CancelableCallback cancelableCallback, LatLng... latLngs) {
        if (isMapLoaded) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : latLngs) {
                builder.include(point);
            }
            if (includeMyLocation) builder.include(currentLocation);
            LatLngBounds bounds = builder.build();
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), cancelableCallback); // padding 100
            final int toolbarStatusbarHeight = (int)PixelDPConverter.convertDpToPixel(61+25, getContext());
            // TODO: calculate real bottomSpace height
            final int bottomSpaceHeight = (int)PixelDPConverter.convertDpToPixel(200, getContext());
            final int boundHeight = screenHeight - toolbarStatusbarHeight - bottomSpaceHeight;
            Log.d(TAG, "tbheight = "+toolbarStatusbarHeight);
            Log.d(TAG, "boundHei="+boundHeight);
            Log.d(TAG, "screenHe="+screenHeight);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, screenWidth, boundHeight, 100), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mMap.animateCamera(CameraUpdateFactory.scrollBy(0, (screenHeight-boundHeight)/2f - toolbarStatusbarHeight));
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

    void targetCheckpoint(String checkpointId) {
        if (checkpoints.get("BMcuFRQqoL09YmL2v7mH").getMarker() != null){
            Log.d(TAG, "VALID 2 checkpoint "+isCheckpointLoaded);
        } else {
            Log.d(TAG, "INVALID 2 checkpoint "+isCheckpointLoaded);
        }
        Checkpoint checkpoint = checkpoints.get(checkpointId);
        Log.d(TAG, "target "+(checkpoint != null)+""+(checkpoint.getLatLng() != null)+""+(checkpoint.getMarker() != null));
        if (checkpoint != null && checkpoint.getLatLng() != null) {
            Log.d(TAG, "targeting...");
            targetCamera(true, checkpoint.getLatLng());
            if (activeMarker != null) activeMarker.setIcon(null);
            activeMarker = checkpoint.getMarker();
            if (activeMarker != null) activeMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_marker));
        }
    }

    void drawRoute(@Nullable LatLng fromN, final LatLng to, final GoogleMap.CancelableCallback callback) {
        final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        final LatLng from = (fromN != null) ? fromN : currentLocation;
        bounds.include(from);
        bounds.include(to);
        AppRequest.fetchRoute(getContext(), MapBoxDirection.DRIVING, from, to, new HttpRequest.Callback<MapBoxDirection>() {
            @Override
            public void onResponse(final MapBoxDirection res) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // draw lines
                        PolylineOptions line = new PolylineOptions().clickable(true).add(from).addAll(res.latLngs).add(to);
                        if (routeLine != null) routeLine.remove();
                        routeLine = mMap.addPolyline(line);
                        // add point to bound and apply bound to camera
                        for (LatLng latLng : res.latLngs) {
                            bounds.include(latLng);
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100), callback); // padding 100
                    }
                });
            }

            @Override
            public void onFailure(String reason) {
                Log.d(TAG, "Draw route failed, reason=" + reason);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
                return;
            }
        }
        compass.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        compass.stop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onMapClicked = null;
    }

    public void requestPermissions() {

    }

    private void initScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public interface OnMarkerClickedListener {
        void onMarkerClick(String type, String id);
    }

    public interface OnMapClicked {
        void onMapClicked(LatLng latLng);
    }
}
