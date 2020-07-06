package cf.bautroixa.maptest.model.http;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.oldsocketio.JSONParser;
import cf.bautroixa.maptest.model.types.APILocation;
import cf.bautroixa.maptest.model.types.GeocodingResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MapboxHttpService {
    private static final String TAG = "AppRequest";


//    public static void getGeocodingAddress(Context context, Location location, final HttpRequest.Callback<String> callback) {
//        getGeocodingAddress(context, new LatLng(location.getLatitude(), location.getLongitude()), callback);
//    }

//    public static void getGeocodingAddress(Context context, LatLng latLng, final HttpRequest.Callback<String> callback) {
//        String URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + latLng.longitude + "," + latLng.latitude + ".json?access_token=" + context.getString(R.string.config_mapbox_map_api_key);
//        HttpRequest.getInstance().sendGetRequest(URL, new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                e.printStackTrace();
//                callback.onFailure("No internet");
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                ResponseBody resBody = response.body();
//                if (resBody == null) return;
//                String resStr = resBody.string();
//                Log.d(TAG, "res = " + resStr);
//                try {
//                    String address = JSONParser.parseGeocodingAddressString(resStr);
//                    callback.onResponse(address);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    public static Task<GeocodingResult> getGeocodingAddress(Context context, final double latitude, final double longitude) {
        MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                .accessToken(context.getString(R.string.config_mapbox_map_api_key))
                .query(Point.fromLngLat(longitude, latitude))
                .geocodingTypes(GeocodingCriteria.TYPE_POI)
                .build();
        final TaskCompletionSource<GeocodingResult> taskCompletionSource = new TaskCompletionSource<>();
        reverseGeocode.enqueueCall(new retrofit2.Callback<GeocodingResponse>() {
            @Override
            public void onResponse(@NotNull retrofit2.Call<GeocodingResponse> call, @NotNull retrofit2.Response<GeocodingResponse> response) {
                if (response.isSuccessful()) {
                    GeocodingResponse geocodingResponse = response.body();
                    try {
                        String placeNameResponse = Objects.requireNonNull(geocodingResponse).features().get(0).placeName();
                        assert placeNameResponse != null;
                        taskCompletionSource.setResult(new GeocodingResult(new LatLng(latitude, longitude), placeNameResponse));
                    } catch (Exception e) {
                        taskCompletionSource.setException(e);
                    }
                } else {
                    taskCompletionSource.setException(new Exception("Network error"));
                }
            }

            @Override
            public void onFailure(@NotNull retrofit2.Call<GeocodingResponse> call, @NotNull Throwable t) {
                taskCompletionSource.setException(new Exception(t.getMessage()));
            }
        });
        return taskCompletionSource.getTask();
    }

    public static void getGeocodingLatLng(Context context, String location, final HttpRequest.Callback<APILocation> callback) {
        String URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + location + ".json?access_token=" + context.getString(R.string.config_mapbox_map_api_key);
        HttpRequest.getInstance().sendGetRequest(URL, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.onFailure("No internet");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody resBody = response.body();
                if (resBody == null) return;
                String resStr = resBody.string();
                Log.d(TAG, "res = " + resStr);
                try {
                    APILocation location = JSONParser.parseGeocodingLatlngString(resStr);
                    callback.onResponse(location);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static Task<ArrayList<LatLng>> getRouteLines(Context context, double fromLat, double fromLon, double toLat, double toLon) {
        final TaskCompletionSource<ArrayList<LatLng>> taskCompletionSource = new TaskCompletionSource<>();
        NavigationRoute.builder(context)
                .accessToken(context.getString(R.string.config_mapbox_map_api_key))
                .origin(Point.fromLngLat(fromLon, fromLat))
                .destination(Point.fromLngLat(toLon, toLat))
                .build()
                .getRoute(new retrofit2.Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NotNull retrofit2.Call<DirectionsResponse> call, @NotNull retrofit2.Response<DirectionsResponse> response) {
                        if (response.body() != null) {
                            for (DirectionsRoute route : response.body().routes()) {
                                if (route.geometry() != null) {
                                    List<Point> coords = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6).coordinates();
                                    ArrayList<LatLng> latLngs = new ArrayList<>();
                                    for (Point coord : coords) {
                                        latLngs.add(new LatLng(coord.latitude(), coord.longitude()));
                                    }
                                    taskCompletionSource.setResult(latLngs);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull retrofit2.Call<DirectionsResponse> call, @NotNull Throwable t) {
                        Log.d(TAG, "Draw route failed");
                        taskCompletionSource.setException(new Exception(t.getMessage()));
                    }
                });
        return taskCompletionSource.getTask();
    }
}
