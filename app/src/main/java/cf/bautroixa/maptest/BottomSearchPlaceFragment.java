package cf.bautroixa.maptest;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cf.bautroixa.maptest.data.SearchResult;
import cf.bautroixa.maptest.firestore.MainAppManager;

public class BottomSearchPlaceFragment extends Fragment {
    private static final String ARG_LATITUDE = "lat";
    private static final String ARG_LONGITUDE = "lon";

    MainAppManager manager;

    TextView tvPlaceName, tvPlaceAddress;
    Button btnAddCheckpoint, btnGetDirection;

    public BottomSearchPlaceFragment() {
        manager = MainAppManager.getInstance();
    }

    public static BottomSearchPlaceFragment newInstance(SearchResult searchResult) {
        Bundle args = new Bundle();
        args.putString(SearchResult.PLACE_NAME, searchResult.getPlaceName());
        args.putString(SearchResult.PLACE_ADDRESS, searchResult.getPlaceAddress());
        args.putDouble(ARG_LATITUDE, searchResult.getCoordinate().latitude);
        args.putDouble(ARG_LONGITUDE, searchResult.getCoordinate().longitude);
        BottomSearchPlaceFragment fragment = new BottomSearchPlaceFragment();
        fragment.setArguments(args);
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
            tvPlaceName.setText(arg.getString(SearchResult.PLACE_NAME, "ERROR_NO_PLACE_NAME"));
            tvPlaceAddress.setText(arg.getString(SearchResult.PLACE_ADDRESS, "ERROR_NO_PLACE_ADDRESS"));
            if (manager.isTripLeader()){
                btnAddCheckpoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
            btnGetDirection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
