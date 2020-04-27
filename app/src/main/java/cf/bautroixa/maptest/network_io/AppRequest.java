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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class AppRequest {
    private static final String TAG = "AppRequest";

    public static void getGeocodingAddress(Context context, Location location, final HttpRequest.Callback<String> callback) {
        getGeocodingAddress(context, new LatLng(location.getLatitude(), location.getLongitude()), callback);
    }

    public static void getGeocodingAddress(Context context, LatLng latLng, final HttpRequest.Callback<String> callback) {
        String URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + latLng.longitude + "," + latLng.latitude + ".json?access_token=" + context.getString(R.string.config_mapbox_map_api_key);
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
                    String address = JSONParser.parseGeocodingAddressString(resStr);
                    callback.onResponse(address);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
}
