package cf.bautroixa.maptest.model.types;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class APILocation implements Serializable {
    String address;
    LatLng latLng;

    public APILocation(String address, LatLng latLng) {
        this.address = address;
        this.latLng = latLng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}