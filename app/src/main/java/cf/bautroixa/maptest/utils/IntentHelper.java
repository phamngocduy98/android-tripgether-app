package cf.bautroixa.maptest.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import cf.bautroixa.maptest.R;

public class IntentHelper {
    public static void sendTripCodeIntent(Context context, String tripCode) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        String url = String.format("https://%s/trips/%s/", context.getString(R.string.server_host), tripCode);
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
}
