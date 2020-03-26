package cf.bautroixa.maptest.data;

public interface DBConstant {
    String DB_NAME = "tripgether";
    int DB_VERSION = 1;

    // TABLE STATUS:
    //    String userId;
    //    User user;
    //    LatLng latLng;
    //    int speed; // km/s
    //    int battery; // percent
    //    String place; // Gần ngõ 16 Phan Văn Trường
    //    Marker marker;
    String TABLE_FRIEND_STATUS = "status";
    String COL_ID = "id";
    String COL_USERID = "userid";
    String COL_LATITUDE = "lat";
    String COL_LONGITUDE = "lng";
    String COL_PLACE = "place";
    String COL_SPEED = "speed";
    String COL_BEARING = "bearing";
    String COL_BATTERY = "battery";

    // TABLE USER
    //    String userId;
    //    String fullName;
    //    String avatar;
    String TABLE_USER = "user";
    //String COL_ID = "id";
    //String COL_USERID = "userid";
    String COL_FULLNAME = "fullname";
    String COL_AVATAR = "avatar";
}
