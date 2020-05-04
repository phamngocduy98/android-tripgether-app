package cf.bautroixa.maptest.utils;

import android.content.Context;
import android.content.Intent;

public class IntentHelper {
    public static void sendTripCodeIntent(Context context, String tripCode) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, "Mã tham gia nhóm Tripgether của tôi là: " + tripCode);
        context.startActivity(Intent.createChooser(share, "Chia sẻ mã tham gia"));
    }
}
