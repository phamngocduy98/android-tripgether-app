package cf.bautroixa.maptest.model.types;

import com.google.android.gms.maps.model.LatLng;

public class GeocodingResult {
    LatLng latLng;
    String fullPlaceName;
    String placeName;
    String placeAddress;

    public GeocodingResult(LatLng latLng, String fullPlaceName) {
        this.latLng = latLng;
        this.fullPlaceName = fullPlaceName;
        String[] placeNames = fullPlaceName.split(",");
        this.placeName = placeNames[0];
        this.placeAddress = placeNames.length == 1 ? "" : fullPlaceName.substring(placeName.length() + 2);
    }

    public GeocodingResult(double latitude, double longitude) {
        this.latLng = new LatLng(latitude, longitude);
    }

    public GeocodingResult(double latitude, double longitude, String placeName, String placeAddress) {
        this.latLng = new LatLng(latitude, longitude);
        this.placeName = placeName;
        this.placeAddress = placeAddress;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getFullPlaceName() {
        return fullPlaceName;
    }

    public void setFullPlaceName(String fullPlaceName) {
        this.fullPlaceName = fullPlaceName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }
}
