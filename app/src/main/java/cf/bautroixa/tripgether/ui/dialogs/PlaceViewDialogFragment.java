package cf.bautroixa.tripgether.ui.dialogs;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.GeoPoint;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation;
import com.mapbox.geojson.Point;
import com.squareup.picasso.Picasso;

import java.util.Collections;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.model.repo.objects.CheckpointPublic;
import cf.bautroixa.tripgether.model.repo.objects.PlacePublic;
import cf.bautroixa.tripgether.ui.theme.OneDialogBase;
import cf.bautroixa.tripgether.ui.theme.RoundedImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaceViewDialogFragment extends OneDialogBase {
    private static final String ARG_PLACE = "place";

    RoundedImageView imgImage;
    TextView tvPlaceName, tvPlaceAddress;
    Button btnClose;

    public PlaceViewDialogFragment() {
        // Required empty public constructor
    }

    public static PlaceViewDialogFragment newInstance(Place place) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLACE, new PlacePublic(place));
        PlaceViewDialogFragment fragment = new PlaceViewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static PlaceViewDialogFragment newInstance(CheckpointPublic checkpoint) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLACE, new PlacePublic(checkpoint));
        PlaceViewDialogFragment fragment = new PlaceViewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_place_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgImage = view.findViewById(R.id.img_images_dialog_place_view);
        tvPlaceName = view.findViewById(R.id.tv_place_name_dialog_place_view);
        tvPlaceAddress = view.findViewById(R.id.tv_place_address_dialog_place_view);
        btnClose = view.findViewById(R.id.btn_close_dialog_place_view);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            PlacePublic placePublic = args.getParcelable(ARG_PLACE);
            tvPlaceName.setText(placePublic.getPlaceName());
            tvPlaceAddress.setText(placePublic.getPlaceAddress());
            staticMap(placePublic.getCoordinate().toFirebaseGeoPoint());
        } else {
            dismiss();
        }
    }

    public void staticMap(GeoPoint userLocation) {
        Point myLocationPoint = Point.fromLngLat(userLocation.getLongitude(), userLocation.getLatitude());
        StaticMarkerAnnotation myLocationMarker = StaticMarkerAnnotation.builder().lnglat(myLocationPoint).name(StaticMapCriteria.SMALL_PIN).color(255, 0, 0).build();

        int nightModeFlags = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        imgImage.post(new Runnable() {
            @Override
            public void run() {
                MapboxStaticMap staticImage = MapboxStaticMap.builder()
                        .accessToken(getString(R.string.config_mapbox_map_api_key))
                        .styleId((nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ? StaticMapCriteria.DARK_STYLE : StaticMapCriteria.LIGHT_STYLE)
                        .cameraPoint(myLocationPoint)
                        .cameraZoom(14)
                        .width(imgImage.getMeasuredWidth() / 2)
                        .height(imgImage.getMeasuredHeight() / 2)
                        .staticMarkerAnnotations(Collections.singletonList(myLocationMarker))
                        .build();

                String url = staticImage.url().toString();
                Picasso.get().load(url).placeholder(R.drawable.ic_photo).into(imgImage);
            }
        });
    }
}
