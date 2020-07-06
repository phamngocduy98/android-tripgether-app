package cf.bautroixa.maptest.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.objects.Trip;

public class UrlParser {
    private static final String TAG = "UrlParser";

    public static String[] parseTripCode(Context context, String url) {
        Uri uri = Uri.parse(url);
        Log.d(TAG, "host=" + uri.getHost());
        if (Objects.equals(uri.getHost(), context.getString(R.string.server_host))) {
            List<String> paths = uri.getPathSegments();
            for (String path : paths) {
                Log.d(TAG, "path=" + path);
            }
            if (paths.size() >= 3 && "trip".equals(paths.get(1))) {
                if (paths.size() == 3) return new String[]{paths.get(2), ""};
                if (paths.size() >= 5 && "join".equals(paths.get(3))) {
                    return new String[]{paths.get(2), paths.get(4)};
                }
            }
        }
        return new String[]{Trip.NO_TRIP, ""};
    }
}
