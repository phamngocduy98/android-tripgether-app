package cf.bautroixa.maptest.data;

import com.google.android.gms.maps.model.LatLng;

public class SearchResult {
    public static final String PLACE_NAME = "placeName";
    public static final String PLACE_ADDRESS = "placeAddress";
    public static final String COORDINATE = "coordinate";

    private String placeName;
    private String placeAddress;
    private LatLng coordinate;

    public SearchResult(String placeName, String placeAddress, double latitude, double longitude) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.coordinate = new LatLng(latitude, longitude);
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

    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }
}
