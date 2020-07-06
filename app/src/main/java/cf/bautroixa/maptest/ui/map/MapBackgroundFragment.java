package cf.bautroixa.maptest.ui.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterface;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.model.constant.Collections;
import cf.bautroixa.maptest.model.firestore.core.Document;
import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.model.sharedpref.SPMapStyle;
import cf.bautroixa.maptest.model.sharedpref.SharedPrefHelper;
import cf.bautroixa.maptest.presenter.MapPresenter;
import cf.bautroixa.maptest.presenter.impl.MapPresenterImpl;
import cf.bautroixa.maptest.utils.LocationHelper;
import cf.bautroixa.maptest.utils.calculation.PixelDPConverter;
import cf.bautroixa.maptest.utils.ui_utils.CreateMarker;
import cf.bautroixa.maptest.utils.ui_utils.ImageHelper;

public class MapBackgroundFragment extends Fragment implements MapPresenter.View, NavigationInterfaceOwner, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private static final String TAG = "MapBackgroundFragment";
    private static final String SAVED_STATE_MAP_FRAGMENT = "state_map_fragment";

    private MapPresenterImpl mapPresenter;
    private SharedPreferences sharedPreferences;
    private LocationHelper locationHelper;
    private NavigationInterface navigationInterface;

    private boolean isMapLoaded = false, focusMyLocation = true;
    private int screenWidth, screenHeight, markerZIndex = 1;
    // Views
    private HashMap<String, MarkerViewHolder> markerViewHolders;
    private HashMap<String, Marker> markers;

    private SupportMapFragment ggMapFragment;
    private GoogleMap mMap;
    private Marker myLocationMarker = null, myLocationRotationMarker = null, tempMarker = null;
    private Polyline routeLine = null;


    public MapBackgroundFragment() {
        this.markerViewHolders = new HashMap<>();
        this.markers = new HashMap<>();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //SavedState ggMapFragmentStates = getChildFragmentManager().saveFragmentInstanceState(ggMapFragment);
        //outState.putParcelable(SAVED_STATE_MAP_FRAGMENT, ggMapFragmentStates);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_background, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        locationHelper = LocationHelper.getInstance(requireContext());
        sharedPreferences = SharedPrefHelper.getSharedPreferences(requireContext());
//        if (savedInstanceState != null) {
//            ggMapFragment.setInitialSavedState((SavedState) savedInstanceState.getParcelable(SAVED_STATE_MAP_FRAGMENT));
//        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapPresenter = new MapPresenterImpl(this, requireContext(), this, navigationInterface);
        initScreenSize();
        ggMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frag_map);
        Objects.requireNonNull(ggMapFragment).getMapAsync(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        locationHelper.setInBackground(false);
        if (mMap != null) setMapType();
    }


    @Override
    public void onPause() {
        super.onPause();
        locationHelper.setInBackground(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSetting = mMap.getUiSettings();
        uiSetting.setMapToolbarEnabled(false);
        int nightModeFlags = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) setMapNightMode(googleMap);
        setMapType();
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMapLongClickListener(mapPresenter);
        mMap.setOnMarkerClickListener(mapPresenter);
//        mMap.setOnCameraMoveCanceledListener(mapPresenter);
//        mMap.setOnCameraIdleListener(mapPresenter);
        mMap.setOnMapClickListener(mapPresenter);
    }

    public void setMapType() {
        if (mMap == null) return;
        if (SPMapStyle.getMapStyle(sharedPreferences) == SPMapStyle.MapStyle.SATELLITE && mMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (mMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    @Override
    public void setMapNightMode(GoogleMap googleMap) {
        try {
            if (!googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.maps_night))) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    @Override
    public void onMapLoaded() {
        isMapLoaded = true;
        // Vietnam : 16.0868824,105.2290659,6z
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.0868824, 105.2290659), 6));
        mapPresenter.initOnMapLoaded(mMap);
    }

    /*
     * Create marker
     */

    @Nullable
    public Marker createUserMarker(final User user) {
        final Context context = requireContext();
        if (!isMapLoaded || user == null || user.getLatLng() == null) return null;
        final View markerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_marker_user, null);
        MarkerViewHolder markerViewHolder = new MarkerViewHolder(markerView);
        markerViewHolders.put(getMarkerId(user), markerViewHolder);

        markerViewHolder.bind(user);
        final Marker marker = mMap.addMarker(new MarkerOptions().position(user.getLatLng())
                .title(user.getId())
                .snippet(Collections.USERS)
                .icon(BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(context, markerView, 52))));
        if (user.getAvatar() != null && !user.getAvatar().equals(User.DEFAULT_AVATAR)) {
            ImageHelper.loadCircleImage(user.getAvatar(), new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    markerViewHolder.imgSos.setImageBitmap(bitmap);
                    Log.d(TAG, "loaded marker image completed");
                    updateMarker(marker, markerViewHolder);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Log.d(TAG, "loaded marker image failed");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });
        }
        markers.put(getMarkerId(user), marker);
        return marker;
    }

    @Override
    public void updateMarker(Marker marker, MarkerViewHolder markerViewHolder) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "loaded", Toast.LENGTH_SHORT).show();
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(requireContext(), markerViewHolder.markerView, 52)));
            }
        }, 2000);
    }

    @Nullable
    @Override
    public Marker createCheckpointMarker(final Checkpoint checkpoint, final int checkpointIndex) {
        if (!isMapLoaded || checkpoint == null || checkpoint.getLatLng() == null) return null;
        Marker marker = mMap.addMarker(new MarkerOptions().position(checkpoint.getLatLng())
                .title(checkpoint.getId()).snippet(Collections.CHECKPOINTS).icon((BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(requireContext(), R.layout.map_marker_checkpoint_selected, 30, new CreateMarker.ILayoutEditor() {
                    @Override
                    public void edit(View view) {
                        TextView tvName = view.findViewById(R.id.tv_name_map_marker_checkpoint_selected);
                        tvName.setText(String.valueOf(checkpointIndex + 1));
                    }
                })))));
        markers.put(getMarkerId(checkpoint), marker);
        return marker;
    }

    public Marker createTempMarker(LatLng latLng, String title, String snippet) {
        tempMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                .title(title)
                .snippet(snippet)
        );
        return tempMarker;
    }

    @Override
    public boolean isMapLoaded() {
        return isMapLoaded;
    }

    @Override
    public boolean isMyLocationMarker(Marker marker) {
        return myLocationRotationMarker.equals(marker) || myLocationMarker.equals(marker);
    }

    /**
     * Target camera
     *
     * @param bounds
     * @param bottomSpaceHeight set = 0 to use default 300dp
     */
    @Override
    public void targetCamera(LatLngBounds bounds, int bottomSpaceHeight) {
        if (isMapLoaded) {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), cancelableCallback); // padding 100
            final int toolbarStatusbarHeight = (int) PixelDPConverter.convertDpToPixel(61 + 25, requireContext());
            final int bottomSpaceHeightFinal = bottomSpaceHeight > 0 ? bottomSpaceHeight : (int) PixelDPConverter.convertDpToPixel(300, requireContext());
            final int boundHeight = screenHeight - toolbarStatusbarHeight - bottomSpaceHeightFinal;
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

    @Override
    public void targetPoint(LatLng latLng, int bottomSpaceHeight) {
        if (isMapLoaded) {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), cancelableCallback); // padding 100
            final int toolbarStatusbarHeight = (int) PixelDPConverter.convertDpToPixel(61 + 25, requireContext());
            final int bottomSpaceHeightFinal = bottomSpaceHeight > 0 ? bottomSpaceHeight : (int) PixelDPConverter.convertDpToPixel(300, requireContext());
            final int boundHeight = screenHeight - toolbarStatusbarHeight - bottomSpaceHeightFinal;
            Log.d(TAG, "tbheight = " + toolbarStatusbarHeight);
            Log.d(TAG, "boundHei=" + boundHeight);
            Log.d(TAG, "screenHe=" + screenHeight);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16), new GoogleMap.CancelableCallback() {
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

    @Override
    public void targetMarker(@NonNull Marker marker) {
        marker.setZIndex(markerZIndex++);
    }

    @Override
    public void drawRoute(List<LatLng> latLngs, int bottomSpaceHeight) {
        final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        PolylineOptions line = new PolylineOptions().clickable(true).addAll(latLngs);
        if (routeLine != null) routeLine.remove();
        routeLine = mMap.addPolyline(line);
        // add point to bound and apply bound to camera
        for (LatLng latLng : latLngs) {
            bounds.include(latLng);
        }
        targetCamera(bounds.build(), bottomSpaceHeight);
    }

    @Override
    public void cleanUpTempMarkerAndRoute() {
        if (routeLine != null) routeLine.remove();
        if (tempMarker != null) tempMarker.remove();
    }

    @Override
    public void onMyLocationChanged(LatLng latLng) {
        if (myLocationRotationMarker == null) {
            myLocationRotationMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_direction)));
        } else {
            myLocationRotationMarker.setPosition(latLng);
        }
        if (myLocationMarker == null) {
            myLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(CreateMarker.createScaledMarker(requireContext(), R.drawable.marker_my_location, 120, 120))));
        } else {
            myLocationMarker.setPosition(latLng);
        }

        if (focusMyLocation) {
            targetPoint(latLng, 0);
        }
    }

    @Override
    public void onCompassRotate(float azimuth) {
        if (myLocationMarker != null) {
            myLocationRotationMarker.setRotation(azimuth);
        }
    }


    private void initScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    @Override
    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }

    @Override
    public void lockFocusMyLocation(boolean lock) {
        focusMyLocation = lock;
    }

    public MapPresenterImpl getMapPresenter() {
        return mapPresenter;
    }

    @Override
    public MarkerViewHolder getMarkerView(Document document) {
        return markerViewHolders.get(getMarkerId(document));
    }

    @Override
    @Nullable
    public Marker getMarker(Document document) {
        return markers.get(getMarkerId(document));
    }

    public String getMarkerId(Document document) {
        return document.getClass().getSimpleName() + document.getId();
    }

    public class MarkerViewHolder {
        public View markerView;
        public ImageView markerBg, markerImage, imgSos;
        public TextView tvName;

        public MarkerViewHolder(View markerView) {
            this.markerView = markerView;
            markerBg = markerView.findViewById(R.id.img_bg_map_marker_user);
            markerImage = markerView.findViewById(R.id.img_avatar_map_marker_user);
            imgSos = markerView.findViewById(R.id.img_sos_map_marker_user);
            tvName = markerView.findViewById(R.id.tv_name_map_marker_user);
        }

        public void bind(User user) {
            boolean hasSos = user.getSosRequest() != null && !user.getSosRequest().isResolved();
            imgSos.setVisibility(hasSos ? View.VISIBLE : View.INVISIBLE);
            tvName.setText(user.getShortName());
        }
    }
}
