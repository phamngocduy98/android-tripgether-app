package cf.bautroixa.tripgether.ui.bottomspace;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.MapBackgroundControllable;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.types.GeocodingResult;
import cf.bautroixa.tripgether.model.types.SearchResult;
import cf.bautroixa.tripgether.presenter.BottomSearchPlacePresenter;
import cf.bautroixa.tripgether.presenter.MapPresenter;
import cf.bautroixa.tripgether.presenter.impl.BottomSearchPlacePresenterImpl;
import cf.bautroixa.tripgether.ui.dialogs.CheckpointEditDialogFragment;

public class BottomSearchPlaceFragment extends Fragment implements BottomSearchPlacePresenter.View, MapBackgroundControllable {
    public static final String ARG_LATITUDE = "lat";
    public static final String ARG_LONGITUDE = "lon";

    ModelManager manager;
    BottomSearchPlacePresenterImpl bottomSearchPlacePresenter;
    MapPresenter.CallableMask mapBackgroundInterfaces;
    GeoPoint coord;
    String placeName, placeAddress;

    ShimmerFrameLayout loadingLayout;
    HorizontalScrollView scrollButtons;
    TextView tvPlaceName, tvPlaceAddress;
    Button btnAddCheckpoint, btnGetDirection;

    public BottomSearchPlaceFragment() {
    }

    public static BottomSearchPlaceFragment newInstance(SearchResult searchResult, MapPresenter.CallableMask mapBackgroundInterfaces) {
        Bundle args = new Bundle();
        args.putString(SearchResult.PLACE_NAME, searchResult.getPlaceName());
        args.putString(SearchResult.PLACE_ADDRESS, searchResult.getPlaceAddress());
        args.putDouble(ARG_LATITUDE, searchResult.getLatLng().latitude);
        args.putDouble(ARG_LONGITUDE, searchResult.getLatLng().longitude);
        BottomSearchPlaceFragment fragment = new BottomSearchPlaceFragment();
        fragment.setArguments(args);
        fragment.setMapBackgroundInterface(mapBackgroundInterfaces);
        return fragment;
    }

    public static BottomSearchPlaceFragment newInstance(LatLng latLng, MapPresenter.CallableMask mapBackgroundInterfaces) {
        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, latLng.latitude);
        args.putDouble(ARG_LONGITUDE, latLng.longitude);
        BottomSearchPlaceFragment fragment = new BottomSearchPlaceFragment();
        fragment.setArguments(args);
        fragment.setMapBackgroundInterface(mapBackgroundInterfaces);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
        bottomSearchPlacePresenter = new BottomSearchPlacePresenterImpl(context, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_search_place, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingLayout = view.findViewById(R.id.shimmer_view_container);
        tvPlaceName = view.findViewById(R.id.tv_place_name_frag_bot_search_place);
        tvPlaceAddress = view.findViewById(R.id.tv_place_location_frag_bot_search_place);
        scrollButtons = view.findViewById(R.id.scroll_buttons_frag_bot_search_place);
        btnAddCheckpoint = view.findViewById(R.id.btn_add_checkpoint_frag_bot_search_place);
        btnGetDirection = view.findViewById(R.id.btn_get_direction_frag_bot_search_place);

        Bundle arg = getArguments();
        bottomSearchPlacePresenter.handleBundle(arg);
    }

    @Override
    public void setMapBackgroundInterface(MapPresenter.CallableMask mapBackgroundInterface) {
        this.mapBackgroundInterfaces = mapBackgroundInterface;
    }

    @Override
    public void onLoading() {
        scrollButtons.setVisibility(android.view.View.INVISIBLE);
        loadingLayout.startShimmer();
    }

    @Override
    public void onStopLoading() {
        loadingLayout.stopShimmer();
        scrollButtons.setVisibility(android.view.View.VISIBLE);
        loadingLayout.setVisibility(android.view.View.INVISIBLE);
    }

    @Override
    public void onLoadFailed() {
        tvPlaceName.setText("Không thể kết nối!");
        tvPlaceAddress.setText("Kiểm tra kết nối internet của bạn!");
    }

    @Override
    public void updateView(GeocodingResult geocodingResult) {
        placeName = geocodingResult.getPlaceName();
        placeAddress = geocodingResult.getPlaceAddress();
        tvPlaceName.setText(placeName);
        tvPlaceAddress.setText(placeAddress);
    }

    @Override
    public void setUpButtons(final GeocodingResult geocodingResult, boolean isUserTripLeader) {
        if (isUserTripLeader) {
            btnAddCheckpoint.setVisibility(android.view.View.VISIBLE);
            btnAddCheckpoint.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View view) {
                    CheckpointEditDialogFragment.newInstance(new Checkpoint("", geocodingResult.getLatLng().latitude, geocodingResult.getLatLng().longitude, geocodingResult.getPlaceName(), new Timestamp(Calendar.getInstance().getTime())), new CheckpointEditDialogFragment.OnCheckpointSetListener() {
                        @Override
                        public void onCheckpointSet(Checkpoint checkpoint) {
                            bottomSearchPlacePresenter.addCheckpoint(checkpoint);
                        }
                    }, new CheckpointEditDialogFragment.OnDeleteCheckpointListener() {
                        @Override
                        public void onCheckpointDeleted() {
                            // do nothing
                        }
                    }).show(getChildFragmentManager(), "add checkpoint");
                }
            });
        } else {
            btnAddCheckpoint.setVisibility(android.view.View.GONE);
        }
        btnGetDirection.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                mapBackgroundInterfaces.drawRoute(null, geocodingResult.getLatLng());
            }
        });
    }
}
