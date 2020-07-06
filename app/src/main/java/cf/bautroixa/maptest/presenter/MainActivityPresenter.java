package cf.bautroixa.maptest.presenter;

import com.google.android.gms.tasks.Task;

import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.model.http.HttpService;

public interface MainActivityPresenter {
    Checkpoint getCheckpoint(String checkpointId);

    User getUser(String userId);

    Task<HttpService.APIResponse> sendLeaveTrip();

    interface View {
        void initTabAdapter();

        void navigate(int tab, int state, Object... data);

        void selectTab(int tabIndex);
    }
}
