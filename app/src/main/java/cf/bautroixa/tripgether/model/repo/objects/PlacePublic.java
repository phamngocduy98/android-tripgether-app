package cf.bautroixa.tripgether.model.repo.objects;

import android.os.Parcel;
import android.os.Parcelable;

import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.model.types.GeoPointPublic;

public class PlacePublic implements Parcelable {
    String placeName, placeAddress;
    GeoPointPublic coordinate;

    public PlacePublic(Place place) {
        this.placeName = place.getPlaceName();
        this.placeAddress = place.getPlaceAddress();
        this.coordinate = new GeoPointPublic(place.getCoordinate());
    }

    public PlacePublic(CheckpointPublic checkpointPublic) {
        this.placeName = checkpointPublic.getPlaceName();
        this.placeAddress = checkpointPublic.getLocation();
        this.coordinate = checkpointPublic.getCoordinate();
    }

    public PlacePublic(Parcel in) {
        this.placeName = in.readString();
        this.placeAddress = in.readString();
        this.coordinate._latitude = in.readDouble();
        this.coordinate._longitude = in.readDouble();
    }

    public PlacePublic(String placeName, String placeAddress, GeoPointPublic coordinate) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.coordinate = coordinate;
    }

    public static final Parcelable.Creator<PlacePublic> CREATOR = new Parcelable.Creator<PlacePublic>() {
        public PlacePublic createFromParcel(Parcel in) {
            return new PlacePublic(in);
        }

        public PlacePublic[] newArray(int size) {
            return new PlacePublic[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.placeName);
        dest.writeString(this.placeAddress);
        dest.writeDouble(this.coordinate._latitude);
        dest.writeDouble(this.coordinate._longitude);
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

    public GeoPointPublic getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(GeoPointPublic coordinate) {
        this.coordinate = coordinate;
    }
}
