package cf.bautroixa.maptest.presenter;

import com.google.android.gms.tasks.Task;

import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.http.HttpRequest;

public interface MainActivityPresenter {
    void handleTripNotification(String tripNotificationId);

    Checkpoint getCheckpoint(String checkpointId);

    User getUser(String userId);

    Task<HttpRequest.APIResponse> sendLeaveTrip();

    interface View {
        void initTabAdapter();

        void navigate(int tab, int state, Object... data);

        void selectTab(int tabIndex);
    }
}
