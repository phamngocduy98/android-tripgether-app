package cf.bautroixa.tripgether.presenter.bottomspace;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
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

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.presenter.bottomspace.BottomRoutePresenter;
import cf.bautroixa.tripgether.utils.ui_utils.Formater;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomRoutePresenterImpl implements BottomRoutePresenter {
    private final ModelManager manager;
    Context context;
    View view;

    public BottomRoutePresenterImpl(Context context, View view) {
        this.context = context;
        this.view = view;
        this.manager = ModelManager.getInstance(context);
    }

    @Override
    public void getDirectionTo(final double latitude, final double longitude) {
        view.onLoading();
        final GeoPoint from = manager.getCurrentUser().getCurrentCoord();
        NavigationRoute.builder(context)
                .accessToken(context.getString(R.string.config_mapbox_map_api_key))
                .origin(Point.fromLngLat(from.getLongitude(), from.getLatitude()))
                .destination(Point.fromLngLat(longitude, latitude))
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
                        // TODO: null body
                        if (response.isSuccessful() && response.body() != null)
                            for (DirectionsRoute route : response.body().routes()) {
                                String routeGeometry = route.geometry();
                                Double routeDistance = route.distance();
                                Double routeDuration = route.duration();
                                if (routeGeometry == null || routeDistance == null || routeDuration == null)
                                    continue;
                                List<Point> coords = LineString.fromPolyline(routeGeometry, Constants.PRECISION_6).coordinates();
                                ArrayList<LatLng> latLngs = new ArrayList<>();
                                latLngs.add(new LatLng(from.getLatitude(), from.getLongitude()));
                                for (Point coord : coords) {
                                    latLngs.add(new LatLng(coord.latitude(), coord.longitude()));
                                }
                                latLngs.add(new LatLng(latitude, longitude));
                                view.setUpView(Formater.formatDistance(routeDistance), Formater.formatTime(routeDuration), latLngs);
                            }
                    }

                    @Override
                    public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable t) {
                        view.onLoadingFailed();
                        t.printStackTrace();
                    }
                });
    }
}
