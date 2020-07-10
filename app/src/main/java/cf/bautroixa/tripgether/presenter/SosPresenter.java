package cf.bautroixa.tripgether.presenter;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;

import cf.bautroixa.tripgether.model.firestore.objects.SosRequest;

public interface SosPresenter {
    Task<Uri> uploadImage(@Nullable Uri uri, Bitmap bitmap);

    void resolveSos();

    void updateSos(boolean createNew, int lever, String desc, Uri imageUri, Bitmap imageBitmap);

    interface View {
        void updateView(SosRequest sosRequest);

        void staticMap(GeoPoint myLocation);

        void onSending(String text);

        void onSuccess();

        void onFailed(String reason);
    }
}
