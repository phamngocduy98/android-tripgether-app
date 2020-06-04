package cf.bautroixa.maptest.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import cf.bautroixa.maptest.model.firestore.Notification;

public interface AlertActivityPresenter {
    void handleIntent(@Nullable Bundle bundle);

    interface View {
        void setUpView(Notification notification);
    }
}
