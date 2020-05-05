package cf.bautroixa.maptest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;

import cf.bautroixa.maptest.data.SearchResult;
import cf.bautroixa.maptest.dialogs.DialogCheckpointEditFragment;
import cf.bautroixa.maptest.firestore.Checkpoint;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.interfaces.MapBackgroundControllable;
import cf.bautroixa.maptest.interfaces.MapBackgroundInterfaces;

public class BottomSearchPlaceFragment extends Fragment implements MapBackgroundControllable {
    private static final String ARG_LATITUDE = "lat";
    private static final String ARG_LONGITUDE = "lon";

    MainAppManager manager;
    MapBackgroundInterfaces mapBackgroundInterfaces;

    TextView tvPlaceName, tvPlaceAddress;
    Button btnAddCheckpoint, btnGetDirection;

    public BottomSearchPlaceFragment() {
        manager = MainAppManager.getInstance();
    }

    public static BottomSearchPlaceFragment newInstance(SearchResult searchResult, MapBackgroundInterfaces mapBackgroundInterfaces) {
        Bundle args = new Bundle();
        args.putString(SearchResult.PLACE_NAME, searchResult.getPlaceName());
        args.putString(SearchResult.PLACE_ADDRESS, searchResult.getPlaceAddress());
        args.putDouble(ARG_LATITUDE, searchResult.getCoordinate().latitude);
        args.putDouble(ARG_LONGITUDE, searchResult.getCoordinate().longitude);
        BottomSearchPlaceFragment fragment = new BottomSearchPlaceFragment();
        fragment.setArguments(args);
        fragment.setMapBackgroundInterfaces(mapBackgroundInterfaces);
        return fragment;
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
        tvPlaceName = view.findViewById(R.id.tv_place_name_frag_bot_search_place);
        tvPlaceAddress = view.findViewById(R.id.tv_place_location_frag_bot_search_place);
        btnAddCheckpoint = view.findViewById(R.id.btn_add_checkpoint_frag_bot_search_place);
        btnGetDirection = view.findViewById(R.id.btn_get_direction_frag_bot_search_place);

        Bundle arg = getArguments();
        if (arg != null) {
            final String placeName = arg.getString(SearchResult.PLACE_NAME, "ERROR_NO_PLACE_NAME");
            tvPlaceName.setText(placeName);
            tvPlaceAddress.setText(arg.getString(SearchResult.PLACE_ADDRESS, "ERROR_NO_PLACE_ADDRESS"));
            final GeoPoint coord = new GeoPoint(arg.getDouble(ARG_LATITUDE, 0f), arg.getDouble(ARG_LONGITUDE, 0f));
            if (manager.isTripLeader()){
                btnAddCheckpoint.setVisibility(View.VISIBLE);
                btnAddCheckpoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogCheckpointEditFragment.newInstance(new Checkpoint("", coord, placeName, new Timestamp(Calendar.getInstance().getTime())), new DialogCheckpointEditFragment.OnCheckpointSetListener() {
                            @Override
                            public void onCheckpointSet(Checkpoint checkpoint) {
                                manager.getCheckpointsManager().create(null, checkpoint);
                            }
                        }, new DialogCheckpointEditFragment.OnDeleteCheckpointListener() {
                            @Override
                            public void onCheckpointDeleted() {
                                // do nothing
                            }
                        }).show(getChildFragmentManager(), "add checkpoint");
                    }
                });
            } else {
                btnAddCheckpoint.setVisibility(View.GONE);
            }
            btnGetDirection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mapBackgroundInterfaces.drawRoute(null, new LatLng(coord.getLatitude(), coord.getLongitude()));
                }
            });
        }
    }

    @Override
    public void setMapBackgroundInterfaces(MapBackgroundInterfaces mapBackgroundInterfaces) {
        this.mapBackgroundInterfaces = mapBackgroundInterfaces;
    }
}
