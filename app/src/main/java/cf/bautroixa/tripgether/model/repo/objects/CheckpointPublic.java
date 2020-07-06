package cf.bautroixa.tripgether.model.repo.objects;

import android.os.Parcel;
import android.os.Parcelable;

import cf.bautroixa.tripgether.model.types.GeoPointPublic;
import cf.bautroixa.tripgether.model.types.SearchResult;
import cf.bautroixa.tripgether.model.types.TimestampPublic;

public class CheckpointPublic implements Parcelable {
    String name;
    GeoPointPublic coordinate;
    String location;
    String placeName;
    TimestampPublic time;

    public CheckpointPublic() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPointPublic getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(GeoPointPublic coordinate) {
        this.coordinate = coordinate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public TimestampPublic getTime() {
        return time;
    }

    public void setTime(TimestampPublic time) {
        this.time = time;
    }

    public SearchResult toSearchResult() {
        return new SearchResult(placeName, location, coordinate._latitude, coordinate._longitude);
    }

    public CheckpointPublic(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<CheckpointPublic> CREATOR = new Parcelable.Creator<CheckpointPublic>() {
        public CheckpointPublic createFromParcel(Parcel in) {
            return new CheckpointPublic(in);
        }

        public CheckpointPublic[] newArray(int size) {
            return new CheckpointPublic[size];
        }

    };

    public void readFromParcel(Parcel in) {
        this.name = in.readString();
        this.coordinate._latitude = in.readDouble();
        this.coordinate._longitude = in.readDouble();
        this.location = in.readString();
        this.placeName = in.readString();
        this.time._seconds = in.readLong();
        this.time._nanoseconds = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeDouble(this.coordinate._latitude);
        dest.writeDouble(this.coordinate._longitude);
        dest.writeString(this.location);
        dest.writeString(this.placeName);
        dest.writeLong(this.time._seconds);
        dest.writeInt(this.time._nanoseconds);
    }
}
