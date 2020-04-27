package cf.bautroixa.maptest.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.firestore.Trip;

public class UrlParser {
    private static final String TAG = "UrlParser";

    public static String parseTripCode(Context context, String url){
        Uri uri = Uri.parse(url);
        Log.d(TAG, "host="+uri.getHost());
        if (Objects.equals(uri.getHost(), context.getString(R.string.server_host))) {
            List<String> paths = uri.getPathSegments();
            for (String path: paths){
                Log.d(TAG, "path="+path);
            }
            if (paths.size() >= 2 && "trips".equals(paths.get(0))){
                return paths.get(1);
            }
        }
        return Trip.NO_TRIP;
    }
}
