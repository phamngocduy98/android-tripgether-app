package cf.bautroixa.tripgether.ui.map;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.constant.Collections;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.sharedpref.SPMapStyle;
import cf.bautroixa.tripgether.model.sharedpref.SharedPrefHelper;
import cf.bautroixa.tripgether.presenter.bottomspace.MapPresenter;
import cf.bautroixa.tripgether.presenter.bottomspace.MapPresenterImpl;
import cf.bautroixa.tripgether.utils.LocationHelper;
import cf.bautroixa.tripgether.utils.calculation.PixelDPConverter;
import cf.bautroixa.tripgether.utils.ui_utils.CreateMarker;
import cf.bautroixa.ui.helpers.ImageHelper;
import timber.log.Timber;

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
    private final HashMap<String, UserMarkerViewHolder> userMarkerViews;
    private final HashMap<String, CheckpointMarkerViewHolder> checkpointMarkerViews;

    private SupportMapFragment ggMapFragment;
    private GoogleMap mMap;
    private Marker myLocationMarker = null, myLocationRotationMarker = null, tempMarker = null;
    private Polyline routeLine = null;


    public MapBackgroundFragment() {
        this.userMarkerViews = new HashMap<>();
        this.checkpointMarkerViews = new HashMap<>();
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
    public UserMarkerViewHolder createUserMarker(final User user) {
        final Context context = requireContext();
        if (!isMapLoaded || user == null || user.getLatLng() == null) return null;
        final View markerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_marker_user, null);
        UserMarkerViewHolder userMarkerViewHolder = new UserMarkerViewHolder(markerView);
        userMarkerViewHolder.bind(user);
        final Marker marker = mMap.addMarker(new MarkerOptions().position(user.getLatLng())
                .title(user.getId())
                .snippet(Collections.USERS)
                .icon(BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(context, markerView, 52))));
        userMarkerViewHolder.setMarker(marker);
        userMarkerViewHolder.updateAvatar(user);
        userMarkerViews.put(user.getId(), userMarkerViewHolder);
        return userMarkerViewHolder;
    }

    @Nullable
    @Override
    public CheckpointMarkerViewHolder createCheckpointMarker(final Checkpoint checkpoint, final int checkpointIndex) {
        if (!isMapLoaded || checkpoint == null || checkpoint.getLatLng() == null) return null;
        final Context context = requireContext();
        final View markerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_marker_checkpoint_selected, null);
        CheckpointMarkerViewHolder checkpointMarkerViewHolder = new CheckpointMarkerViewHolder(markerView);
        checkpointMarkerViewHolder.bind(checkpointIndex);
        Marker marker = mMap.addMarker(new MarkerOptions().position(checkpoint.getLatLng())
                .title(checkpoint.getId()).snippet(Collections.CHECKPOINTS)
                .icon((BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(context, markerView, 52)))));
        checkpointMarkerViewHolder.setMarker(marker);
        checkpointMarkerViews.put(checkpoint.getId(), checkpointMarkerViewHolder);
        return checkpointMarkerViewHolder;
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
    public UserMarkerViewHolder getUserMarkerView(User user) {
        return userMarkerViews.get(user.getId());
    }

    @Override
    public CheckpointMarkerViewHolder getCheckpointMarkerView(Checkpoint checkpoint) {
        return checkpointMarkerViews.get(checkpoint.getId());
    }

    @Override
    public void clearAllUserMarker() {
        UserMarkerViewHolder[] userMarkerViewValues = new UserMarkerViewHolder[userMarkerViews.size()];
        userMarkerViews.values().toArray(userMarkerViewValues);
        for (int i = 0; i < userMarkerViewValues.length; i++) {
            try {
                userMarkerViewValues[i].marker.remove();
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    @Override
    public void clearAllCheckpointMarker() {
        CheckpointMarkerViewHolder[] checkpointMarkerViewHolders = new CheckpointMarkerViewHolder[checkpointMarkerViews.size()];
        checkpointMarkerViews.values().toArray(checkpointMarkerViewHolders);
        for (int i = 0; i < checkpointMarkerViewHolders.length; i++) {
            try {
                checkpointMarkerViewHolders[i].marker.remove();
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public class UserMarkerViewHolder extends RecyclerView.ViewHolder{
        public ImageView markerBg, markerImage, imgSos;
        public TextView tvName;
        @Nullable
        public Marker marker;
        private User savedUser;

        public UserMarkerViewHolder(View itemView) {
            super(itemView);
            markerBg = itemView.findViewById(R.id.img_bg_map_marker_user);
            markerImage = itemView.findViewById(R.id.img_avatar_map_marker_user);
            imgSos = itemView.findViewById(R.id.img_sos_map_marker_user);
            tvName = itemView.findViewById(R.id.tv_name_map_marker_user);
        }

        public void bind(User user) {
            boolean hasSos = user.getSosRequest() != null && !user.getSosRequest().isResolved();
            imgSos.setVisibility(hasSos ? View.VISIBLE : View.INVISIBLE);
            tvName.setText(user.getShortName());
            savedUser = user;
        }

        public void setMarker(@NonNull Marker marker) {
            this.marker = marker;
        }

        public void updateAvatar(User user){
            if (user.getAvatar() != null && !user.getAvatar().equals(User.DEFAULT_AVATAR)) {
                if (savedUser != null && Objects.equals(savedUser.getAvatar(), user.getAvatar())) return;
                ImageHelper.loadCircleImage(user.getAvatar(), new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        markerImage.setImageBitmap(bitmap);
                        Log.d(TAG, "loaded marker image completed");
                        updateMarker();
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
        }

        public void updateMarker() {
            if (marker == null) {
                Timber.e("try to update null marker");
                return;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(requireContext(), itemView, 52)));
                    } catch (Exception e) {
                        Timber.e(e, "marker#setIcon of removed marker (not null)");
                    }
                }
            }, 2000);
        }
    }

    public class CheckpointMarkerViewHolder extends RecyclerView.ViewHolder {
        public View markerView;
        public TextView tvName;
        public Marker marker;

        public CheckpointMarkerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name_map_marker_checkpoint_selected);
        }

        public void bind(int checkpointIndex) {
            tvName.setText(String.valueOf(checkpointIndex + 1));
        }

        public void setMarker(Marker marker) {
            this.marker = marker;
        }

        public void updateMarker() {
            if (marker == null) {
                Timber.e("try to update null marker");
                return;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(CreateMarker.createBitmapFromLayout(requireContext(), markerView, 52)));
                    } catch (Exception e) {
                        Timber.e(e, "marker#setIcon of removed marker (not null)");
                    }
                }
            }, 2000);
        }
    }
}
