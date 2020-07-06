package cf.bautroixa.maptest.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.GeoPoint;

import cf.bautroixa.maptest.model.firestore.objects.Notification;

public interface AlertActivityPresenter {
    void handleIntent(@Nullable Bundle bundle);

    interface View {
        void setUpView(Notification notification);

        void staticMap(GeoPoint myLocation, GeoPoint coordinate);
    }
}
