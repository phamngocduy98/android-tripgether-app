package cf.bautroixa.maptest.model.types;

import com.google.android.gms.maps.model.LatLng;

import cf.bautroixa.maptest.interfaces.LatLngOwner;

public class SearchResult implements LatLngOwner {
    public static final String PLACE_NAME = "placeName";
    public static final String PLACE_ADDRESS = "placeAddress";
    public static final String COORDINATE = "coordinate";

    private String placeName;
    private String placeAddress;
    private LatLng latLng;

    public SearchResult(String placeName, String placeAddress, double latitude, double longitude) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.latLng = new LatLng(latitude, longitude);
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

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
