package cf.bautroixa.maptest.presenter.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cf.bautroixa.maptest.interfaces.LatLngOwner;
import cf.bautroixa.maptest.interfaces.NavigationInterface;
import cf.bautroixa.maptest.model.constant.Collections;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.core.Document;
import cf.bautroixa.maptest.model.firestore.core.DocumentsManager;
import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.model.http.MapboxHttpService;
import cf.bautroixa.maptest.model.sharedpref.SharedPrefHelper;
import cf.bautroixa.maptest.presenter.MapPresenter;
import cf.bautroixa.maptest.services.tasks.LocationBaseTask;
import cf.bautroixa.maptest.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.maptest.ui.map.MapBackgroundFragment;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.utils.CompassHelper;
import cf.bautroixa.maptest.utils.LocationHelper;

import static cf.bautroixa.maptest.ui.adapter.pager_adapter.MainActivityPagerAdapter.Tabs.TAB_MAP;

public class MapPresenterImpl implements MapPresenter, MapPresenter.CallableMask, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    protected final String TAG = getClass().getSimpleName();
    private final ModelManager manager;
    private final NavigationInterface navigationInterface;
    private LifecycleOwner lifecycleOwner;
    private Context context;
    private View view;
    private LocationHelper locationHelper;
    private SharedPreferences sharedPref;

    private LatLng currentLocation;
    private ArrayList<User> members;
    private ArrayList<Checkpoint> checkpoints;
    private CompassHelper compass;
    private GoogleMap mMap;

    public MapPresenterImpl(LifecycleOwner lifecycleOwner, Context context, final View view, NavigationInterface navigationInterface) {
        this.lifecycleOwner = lifecycleOwner;
        this.context = context;
        this.view = view;
        this.manager = ModelManager.getInstance(context);
        this.navigationInterface = navigationInterface;
        this.sharedPref = SharedPrefHelper.getSharedPreferences(context);

        this.currentLocation = manager.getCurrentUser().getLatLng();
        this.members = new ArrayList<>();
        this.checkpoints = new ArrayList<>();

        if (manager.getCurrentTrip().isSubManagerAvailable()) {
            assert manager.getCurrentTrip().getMembersManager() != null;
            assert manager.getCurrentTrip().getCheckpointsManager() != null;

            manager.getCurrentTrip().getMembersManager().attachListener(lifecycleOwner, new DocumentsManager.OnListChangedListener<User>() {

                @Override
                public void onItemInserted(int position, User user) {
                    if (user.getId().equals(manager.getCurrentUser().getId()))
                        return; // currentUser has no marker
                    view.createUserMarker(user);
                }

                @Override
                public void onItemChanged(int position, User user) {
                    if (user.getId().equals(manager.getCurrentUser().getId()))
                        return; // currentUser has no marker
                    Marker marker = view.getMarker(user);
//                    if (marker == null) view.createUserMarker(user);
                    if (marker != null && !marker.getPosition().equals(user.getLatLng())) {
                        marker.setPosition(user.getLatLng());
                    }
                    MapBackgroundFragment.MarkerViewHolder markerViewHolder = view.getMarkerView(user);
                    if (markerViewHolder != null) {
                        markerViewHolder.bind(user);
                        view.updateMarker(marker, markerViewHolder);
                    }
                }

                @Override
                public void onItemRemoved(int position, User user) {
                    Marker marker = view.getMarker(user);
                    if (marker != null) marker.remove();
                }

                @Override
                public void onDataSetChanged(ArrayList<User> datas) {
                    members = datas;
                }
            });
            manager.getCurrentTrip().getCheckpointsManager().attachListener(lifecycleOwner, new DocumentsManager.OnListChangedListener<Checkpoint>() {
                @Override
                public void onItemInserted(int position, Checkpoint checkpoint) {
                    view.createCheckpointMarker(checkpoint, position + 1);
                }

                @Override
                public void onItemChanged(int position, Checkpoint checkpoint) {
                    Marker marker = view.getMarker(checkpoint);
                    if (marker == null) view.createCheckpointMarker(checkpoint, position + 1);
                    if (!marker.getPosition().equals(checkpoint.getLatLng())) {
                        marker.setPosition(checkpoint.getLatLng());
                    }
                }

                @Override
                public void onItemRemoved(int position, Checkpoint checkpoint) {
                    view.getMarker(checkpoint).remove();
                    // TODO: remove from HashMap too
                }

                @Override
                public void onDataSetChanged(ArrayList<Checkpoint> datas) {
                    checkpoints = datas;
                }
            });
        }

    }

    @Override
    public void initOnMapLoaded(GoogleMap googleMap) {
        mMap = googleMap;
        initLocationService();
        for (int i = 0; i < checkpoints.size(); i++) {
            Checkpoint checkpoint = checkpoints.get(i);
            view.createCheckpointMarker(checkpoint, i);
        }
        for (User user : members) {
            if (!user.getId().equals(manager.getCurrentUser().getId())) {
                view.createUserMarker(user);
            }
        }
    }

    @Override
    public void initLocationService() {
        this.locationHelper = LocationHelper.getInstance(context);
        locationHelper.attachListener(this.lifecycleOwner, new LocationHelper.OnLocationChangedListener() {
            @Override
            public void newLocation(int state, @Nullable Location lastAccurateLocation) {
                if (lastAccurateLocation == null) return;
                currentLocation = new LatLng(lastAccurateLocation.getLatitude(), lastAccurateLocation.getLongitude());
                LocationBaseTask.onNewLocation(context, sharedPref, lastAccurateLocation);
                view.onMyLocationChanged(currentLocation);
            }
        });
        this.compass = new CompassHelper(context);
        compass.attachLister(this.lifecycleOwner, new CompassHelper.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {
                view.onCompassRotate(azimuth - mMap.getCameraPosition().bearing);
            }
        });
    }

    /**
     * PRIVATE METHOD
     **/
    private void targetCamera(int bottomSpaceHeight, boolean includeMyLocation, LatLng... latLngs) {
        ArrayList<LatLng> latLngArrayList = new ArrayList<>();
        if (includeMyLocation) latLngArrayList.add(currentLocation);
        latLngArrayList.addAll(Arrays.asList(latLngs));
        if (view.isMapLoaded()) {
            if (latLngArrayList.size() == 1) {
                view.targetPoint(latLngArrayList.get(0), bottomSpaceHeight);
            } else {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng point : latLngs) {
                    builder.include(point);
                }
                LatLngBounds bounds = builder.build();
                view.targetCamera(bounds, bottomSpaceHeight);
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_HIDE);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_SEARCH_RESULT, latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (view.isMyLocationMarker(marker)) {
            // click my location marker, do nothing
            onMapClick(marker.getPosition());
            return false;
        }
        String type = marker.getSnippet(), id = marker.getTitle();
        Log.d(TAG, "marker click" + type + "id=" + id);
        if (type.equals(Collections.CHECKPOINTS)) {
            navigationInterface.navigate(TAB_MAP, TabMapFragment.STATE_CHECKPOINT, manager.getCurrentTrip().getCheckpointsManager().get(id));
            return true;
        } else if (type.equals(Collections.USERS)) {
            navigationInterface.navigate(TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, manager.getCurrentTrip().getMembersManager().get(id));
            return true;
        }
        return false; // return false to show marker title and snippet normally
    }

    /**
     * CALLABLE MASK
     **/

    @Override
    public void targetMyLocation() {
        view.lockFocusMyLocation(true);
        CameraPosition cameraPos = mMap.getCameraPosition();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(cameraPos.target).bearing(0).zoom(cameraPos.zoom).build()));
        view.targetPoint(currentLocation, 0);
    }

    @Override
    public void target(Object data) {
        cleanUpTempMarkerAndRoute();
        view.lockFocusMyLocation(false);
        if (data instanceof Document) {
            Marker marker = view.getMarker((Document) data);
            if (marker != null) {
                view.targetMarker(marker);
            }
        }
        if (data instanceof LatLngOwner) {
            targetCamera(0, false, ((LatLngOwner) data).getLatLng());
//            if (data instanceof User){
//                User user = (User) data;
//                Marker marker = view.getMarker(user);
//                MapBackgroundFragment.MarkerViewHolder markerViewHolder = view.getMarkerView(user);
//                markerViewHolder.markerBg.setColorFilter(Color.argb(255, 107, 0, 255), android.graphics.PorterDuff.Mode.MULTIPLY);
//                view.updateMarker(marker, markerViewHolder);
//            }
        } else if (data instanceof LatLng) {
            targetCamera(0, false, (LatLng) data);
        } else {
            Log.e(TAG, "target undefined Object type");
        }
    }

    @Override
    public void createTempMarker(LatLng latLng, String title, String snippet) {
        view.createTempMarker(latLng, title, snippet);
    }

    @Override
    public void cleanUpTempMarkerAndRoute() {
        view.cleanUpTempMarkerAndRoute();
    }

    @Override
    public void drawRoute(@Nullable LatLng fromN, final LatLng to) {
        final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        final LatLng from = (fromN != null) ? fromN : currentLocation;
        bounds.include(from);
        bounds.include(to);
        MapboxHttpService.getRouteLines(this.context, from.latitude, from.longitude, to.latitude, to.longitude).addOnCompleteListener(new OnCompleteListener<ArrayList<LatLng>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<LatLng>> task) {
                if (task.isSuccessful()) {
                    view.drawRoute(task.getResult(), 0);
                }
            }
        });
    }

    @Override
    public void drawLine(List<LatLng> latlngs) {
        view.drawRoute(latlngs, 0);
    }
}
