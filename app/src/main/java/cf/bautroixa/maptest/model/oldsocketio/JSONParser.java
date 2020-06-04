package cf.bautroixa.maptest.model.oldsocketio;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import cf.bautroixa.maptest.model.types.APILocation;
import cf.bautroixa.maptest.model.types.MapBoxDirection;

public class JSONParser {
    public static final String TAG = "JSONParser";

    public static LatLng parseLatLng(JSONObject json) throws JSONException {
        double latitude = json.getDouble("latitude");
        double longitude = json.getDouble("longitude");
        return new LatLng(latitude, longitude);
    }

    public static JSONObject createLatLng(LatLng latLng) throws JSONException {
        JSONObject latlngJson = new JSONObject();
        latlngJson.put("latitude", latLng.latitude);
        latlngJson.put("longitude", latLng.longitude);
        return latlngJson;
    }

//    public static UserStatus parseUserStatus(JSONObject json) throws JSONException {
//        String userId = json.getString("userId");
//        JSONObject latLngJson = json.getJSONObject("latLng");
//        double latitude = latLngJson.getDouble("latitude");
//        double longitude = latLngJson.getDouble("longitude");
//        long speed = json.getLong("speed");
//        int battery = json.getInt("battery");
//        double bearing = json.getInt("bearing");
//        return new UserStatus(userId, latitude, longitude, battery, speed, bearing);
//    }

//    public static JSONObject createUserStatus(UserStatus status) throws JSONException {
//        JSONObject json = new JSONObject();
//        json.put("userId", status.getUserId());
//        json.put("latLng", createLatLng(status.getLatLng()));
//        json.put("speed", status.getSpeed());
//        json.put("battery", status.getBattery());
//        json.put("bearing", status.getBearing());
//        return json;
//    }
//
//    public static User parseUser(JSONObject json) throws JSONException {
//        String userId = json.getString("userId");
//        String fullName = json.getString("fullName");
//        String avatar = json.getString("avatar");
//        return new User(userId, fullName, avatar);
//    }
//
//    public static JSONObject createUser(User user) throws JSONException {
//        JSONObject json = new JSONObject();
//        json.put("fullName", user.getFullName());
//        json.put("avatar", user.getAvatarURI());
//        return json;
//    }

    public static Message parseMessage(JSONObject json) throws JSONException, ParseException {
        String fromId = json.getString("fromId");
        String content = json.getString("content");
        String time = json.getString("time");
        return new Message(fromId, content, time);
    }

    public static JSONObject createMessage(Message message) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("from", message.getFrom());
        json.put("content", message.getContent());
        json.put("time", message.getTimeString());
        return json;
    }

    public static CommonSocketAck parseCommonSocketAck(JSONObject json) {
        boolean success;
        String message;
        try {
            success = json.getBoolean("success");
            message = json.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
            success = false;
            message = "JSON Parse Error";
        }
        return new CommonSocketAck(success, message);
    }

    public static MapBoxDirection parseRoute(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        JSONArray routesJson = json.getJSONArray("routes");
        JSONObject routeJson = routesJson.getJSONObject(0);
        double distance = routeJson.getDouble("distance");
        double duration = routeJson.getDouble("duration");
        Log.d(TAG, "distance" + distance + ", duraction=" + duration);
        JSONArray wayPoint = json.getJSONArray("waypoints");
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (int i = 0; i < wayPoint.length(); i++) {
            JSONObject wp = wayPoint.getJSONObject(i);
            JSONArray locations = wp.getJSONArray("location");
            latLngs.add(new LatLng(locations.getDouble(1), locations.getDouble(0)));
        }
        return new MapBoxDirection(distance, duration, latLngs);
    }

    public static String parseGeocodingAddressString(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        JSONArray featuresJson = json.getJSONArray("features");
        JSONObject localityJson = featuresJson.getJSONObject(0);
        return localityJson.getString("place_name");
    }

    public static APILocation parseGeocodingLatlngString(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        JSONArray featuresJson = json.getJSONArray("features");
        JSONObject localityJson = featuresJson.getJSONObject(0);
        JSONArray latlng = localityJson.getJSONArray("center");
        String address = localityJson.getString("place_name");
        return new APILocation(address, new LatLng(latlng.getDouble(1), latlng.getDouble(0)));
    }
}
