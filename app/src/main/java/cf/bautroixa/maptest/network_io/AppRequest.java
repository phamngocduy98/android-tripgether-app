package cf.bautroixa.maptest.network_io;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.types.APILocation;
import cf.bautroixa.maptest.types.MapBoxDirection;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppRequest {
    private static final String TAG = "AppRequest";

    public static void fetchRoute(Context context, String transportMode, LatLng from, LatLng to, final HttpRequest.Callback<MapBoxDirection> callback){
        String URL = "https://api.mapbox.com/directions/v5/mapbox/" + transportMode+"?access_token="+context.getString(R.string.config_mapbox_map_api_key);
        String coordinates = from.longitude + "," + from.latitude + ";" + to.longitude + "," + to.latitude;
        RequestBody formBody = new FormBody.Builder().add("coordinates", coordinates).build();
        HttpRequest.getInstance().sendPostFormRequest(URL, formBody, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.onFailure("No internet");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String resStr =response.body().string();
                Log.d(TAG, "res = "+resStr);
                try {
                    MapBoxDirection direction = JSONParser.parseRoute(resStr);
                    callback.onResponse(direction);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void getGeocodingAddress(Context context, Location location, final HttpRequest.Callback<String> callback) {
        getGeocodingAddress(context, new LatLng(location.getLatitude(), location.getLongitude()), callback);
    }

    public static void getGeocodingAddress(Context context, LatLng latLng, final HttpRequest.Callback<String> callback) {
        String URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + latLng.longitude+","+latLng.latitude+".json?access_token="+context.getString(R.string.config_mapbox_map_api_key);
        HttpRequest.getInstance().sendGetRequest(URL, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.onFailure("No internet");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String resStr =response.body().string();
                Log.d(TAG, "res = "+resStr);
                try {
                    String address = JSONParser.parseGeocodingAddressString(resStr);
                    callback.onResponse(address);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void getGeocodingLatLng(Context context, String location, final HttpRequest.Callback<APILocation> callback) {
        String URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + location+".json?access_token="+context.getString(R.string.config_mapbox_map_api_key);
        HttpRequest.getInstance().sendGetRequest(URL, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                callback.onFailure("No internet");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String resStr =response.body().string();
                Log.d(TAG, "res = "+resStr);
                try {
                    APILocation location = JSONParser.parseGeocodingLatlngString(resStr);
                    callback.onResponse(location);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
