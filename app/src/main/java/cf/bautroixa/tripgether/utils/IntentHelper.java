package cf.bautroixa.tripgether.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import cf.bautroixa.tripgether.R;

public class IntentHelper {
    public static void sendPost(Context context, String postId) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        String url = String.format("https://%s/share/post/%s/", context.getString(R.string.server_host), postId);
        share.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(share, "Chia sẻ bài viết"));
    }
    public static void sendTripCodeIntent(Context context, String tripCode) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        String url = String.format("https://%s/share/trip/%s/", context.getString(R.string.server_host), tripCode);
        share.putExtra(Intent.EXTRA_TEXT, "Mã tham gia nhóm Tripgether của tôi là: " + tripCode + "\n Tham gia ngay tại " + url);
        context.startActivity(Intent.createChooser(share, "Chia sẻ mã tham gia"));
    }

    public static void sendSms(Context context, String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + phoneNumber));
        intent.putExtra("sms_body", message);
        context.startActivity(intent);
    }

    public static void navigateByGoogleMap(Context context, LatLng latLng) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latLng.latitude + "," + latLng.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (context != null && mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            Toast.makeText(context, "Bạn chưa cài đặt Google Maps", Toast.LENGTH_LONG).show();
        }
    }

    public static void openNotificationSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        //for Android 5-7
        intent.putExtra("app_package", context.getPackageName());
        intent.putExtra("app_uid", context.getApplicationInfo().uid);
        // for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        context.startActivity(intent);
    }
}
