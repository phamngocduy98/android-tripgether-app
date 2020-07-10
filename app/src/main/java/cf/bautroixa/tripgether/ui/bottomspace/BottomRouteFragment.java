package cf.bautroixa.tripgether.ui.bottomspace;

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

import java.util.List;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.MapBackgroundControllable;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomRoutePresenter;
import cf.bautroixa.tripgether.presenter.bottomspace.MapPresenter;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomRoutePresenterImpl;
import cf.bautroixa.tripgether.utils.IntentHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class BottomRouteFragment extends Fragment implements BottomRoutePresenter.View, MapBackgroundControllable {
    public static final String ARG_LAT = "latitude";
    public static final String ARG_LON = "longitude";
    TextView tvDistance, tvDuration;
    Button btnNavigateByGGMap;
    private MapPresenter.CallableMask mapBackgroundInterfaces;
    private BottomRoutePresenterImpl bottomRoutePresenter;

    public BottomRouteFragment() {
        // Required empty public constructor
    }

    public static BottomRouteFragment newInstance(LatLng latLng) {
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, latLng.latitude);
        args.putDouble(ARG_LON, latLng.longitude);
        BottomRouteFragment fragment = new BottomRouteFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDistance = view.findViewById(R.id.tv_distance_frag_bottom_route);
        tvDuration = view.findViewById(R.id.tv_drive_duration_frag_bottom_route);
        btnNavigateByGGMap = view.findViewById(R.id.tv_navigate_gg_map_bottom_route);
        bottomRoutePresenter = new BottomRoutePresenterImpl(requireContext(), this);
        Bundle arg = getArguments();
        if (arg != null) {
            double latitude = arg.getDouble(ARG_LAT, 0), longitude = arg.getDouble(ARG_LON, 0);
            bottomRoutePresenter.getDirectionTo(latitude, longitude);
        }
    }

    @Override
    public void setUpView(String routeDistance, String routeDuration, final List<LatLng> latLngs) {
        tvDistance.setText(String.format("Khoảng cách %s", routeDistance));
        tvDuration.setText(routeDuration);
        btnNavigateByGGMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentHelper.navigateByGoogleMap(requireContext(), latLngs.get(latLngs.size() - 1));
            }
        });
        mapBackgroundInterfaces.drawLine(latLngs);
    }

    @Override
    public void onLoading() {
        tvDistance.setText("Loading...");
    }

    @Override
    public void onLoadingFailed() {
        tvDistance.setText("Kiểm tra kết nối internet");
    }

    @Override
    public void setMapBackgroundInterface(MapPresenter.CallableMask mapBackgroundInterface) {
        this.mapBackgroundInterfaces = mapBackgroundInterface;
    }
}
